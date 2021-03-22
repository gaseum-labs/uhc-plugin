package com.codeland.uhc.quirk.quirks.classes

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.Summoner
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.data.type.Switch
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.random.Random.Default.nextInt

class Classes(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	override fun onEnable() {
		var currentTick = 0

		timerId = SchedulerUtil.everyTick {
			fun <T> cleanupList(
				list: MutableList<T>, creatorClass: QuirkClass, shouldBe: Material,
				radiusX: Int, radiusY: Int, radiusZ: Int,
				getBlock: (T) -> Block, setBack: (T) -> Material
			) {
				list.removeIf { li ->
					val listBlock = getBlock(li)

					listBlock.type != shouldBe ||

					(Bukkit.getOnlinePlayers().none { player ->
						val playerData = PlayerData.getPlayerData(player.uniqueId)
						val playerBlock = player.location.block

						playerData.participating
							&& getClass(playerData) == creatorClass
							&& abs(playerBlock.x - listBlock.x) <= radiusX
							&& abs(playerBlock.y - listBlock.y) <= radiusY
							&& abs(playerBlock.z - listBlock.z) <= radiusZ

					} && run { listBlock.setType(setBack(li), true); true })
				}
			}

			cleanupList(obsidianifiedLava, QuirkClass.LAVACASTER, Material.OBSIDIAN, 3, 3, 3, { it.block }, { if (it.flowing) Material.AIR else Material.LAVA })

			cleanupList(grindedStone, QuirkClass.ENCHANTER, Material.GRINDSTONE, 1, 3, 1, { it }, { Material.STONE })

			fun regenDurability(itemStack: ItemStack?) {
				if (itemStack == null) return

				if (itemStack.type.maxDurability != 0.toShort()) {
					val meta = itemStack.itemMeta as Damageable
					meta.damage -= 10
					itemStack.itemMeta = meta as ItemMeta
				}
			}

			/* enchanted passive durability regen */
			if (currentTick % 20 == 0) {
				Bukkit.getOnlinePlayers().forEach { player ->
					val playerData = PlayerData.getPlayerData(player.uniqueId)

					if (getClass(playerData) == QuirkClass.ENCHANTER) {
						val slot = (Math.random() * 5).toInt()

						if (slot < 4)
							regenDurability(player.inventory.armorContents[slot])
						else
							regenDurability(player.inventory.itemInOffHand)
					}
				}
			}

			Bukkit.getOnlinePlayers()
				.filter { PlayerData.getPlayerData(it.uniqueId).alive }
				.forEach { player ->
					val uuid = player.uniqueId
					if (inHandMap[uuid] == null) {
						inHandMap[uuid] = InHandItem(player.inventory.itemInMainHand, 1)
					} else {
						val inHand = inHandMap[uuid]!!
						if (inHand.item == player.inventory.itemInMainHand) {
							inHand.ticks++
						} else {
							inHand.item = player.inventory.itemInMainHand
							inHand.ticks = 1
						}
						if (inHand.item.type == Material.BUCKET && inHand.ticks >= 60) {
							if (getClass(uuid) == QuirkClass.LAVACASTER) {
								inHand.item.type = Material.LAVA_BUCKET
								player.playSound(player.location, Sound.ITEM_BUCKET_FILL, 1.0f, 1.0f)
							} else if (getClass(uuid) == QuirkClass.DIVER) {
								inHand.item.type = Material.WATER_BUCKET
								player.playSound(player.location, Sound.ITEM_BUCKET_FILL, 1.0f, 1.0f)
							}
						}
					}
				}

			Bukkit.getOnlinePlayers()
					.filter { PlayerData.getPlayerData(it.uniqueId).alive }
					.forEach { player ->
						if (getClass(player.uniqueId) == QuirkClass.MINER) {
							val timePassed = System.currentTimeMillis() - superBreakMap[player.uniqueId]!!
							val (percent, overflow) = when {
								timePassed < 30 * 1000 -> Pair(timePassed / (30.0 * 1000), false)
								timePassed < 60 * 1000 -> Pair(timePassed / (30.0 * 1000) - 1, true)
								else -> Pair(1.0, true)
							}
							player.sendActionBar(generateSuperbreakMessage(percent, overflow))
							if (player.location.block.lightLevel < 7) {
//								val r = Random()
//								var dx = r.nextInt(10) - 5
//								var dy = 0
//								var dz = r.nextInt(10) - 5
//								var n = 0
//								var makeTorch = true
//								while (!player.location.block.getRelative(dx, dy, dz).type.isAir) {
//									dx = r.nextInt(10) - 5
//									dz = r.nextInt(10) - 5
//									n++
//									if (n > 100) {
//										makeTorch = false
//										break
//									}
//								}
//								while (!player.location.block.getRelative(dx, dy - 1, dz).type.isSolid) {
//									dy--
//									if (dy < 0) {
//										makeTorch = false
//										break
//									}
//								}
//								println("adding torch! $dx $dy $dz $makeTorch")
								// todo make this work
								player.location.block.type = Material.TORCH
							} else {
//								println(player.location.block.lightLevel)
							}
						}
					}

			++currentTick
		}
	}

	override fun onDisable() {
		Bukkit.getScheduler().cancelTask(timerId)
	}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			val quirkClass = getClass(uuid)

			if (quirkClass != QuirkClass.NO_CLASS) startAsClass(player, quirkClass, QuirkClass.NO_CLASS)
		}
	}

	override fun onEnd(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> removeHead(player) }
		val playerData = PlayerData.getPlayerData(uuid)

		PlayerData.getQuirkDataHolder(playerData, QuirkType.CLASSES).data = QuirkClass.NO_CLASS
	}

	private fun generateSuperbreakMessage(percent: Double, overflow: Boolean): String {
		val message = StringBuilder("${ChatColor.GRAY}Superbreak - ")

		for (i in 0 until floor(10 * percent).toInt())
			message.append("${if (overflow) ChatColor.LIGHT_PURPLE else ChatColor.GREEN}▮")

		for (i in 0 until 10 - floor(10 * percent).toInt())
			message.append("${if (overflow) ChatColor.GREEN else ChatColor.GRAY}▮")

		return message.toString()
	}

	override val representation: ItemStack
		get() = ItemStack(Material.LEATHER_HELMET)

	override fun defaultData(): Any {
		return QuirkClass.NO_CLASS
	}

	override fun modifyEntityDrops(entity: Entity, killer: Player?, drops: MutableList<ItemStack>): Boolean {
		if (killer != null && getClass(killer.uniqueId) == QuirkClass.HUNTER) {
			return if (entity.getMetadata(HUNTER_SPAWN_META).isNotEmpty()) {
				drops.clear()
				true
			} else {
				drops.addAll((0..0).mapNotNull { Summoner.getSpawnEgg(entity.type, true, false) }.map { ItemStack(it) })
				false
			}
		}

		return false
	}

	data class ObsidianifiedLava(val block: Block, val flowing: Boolean)

	data class InHandItem(var item: ItemStack, var ticks: Int)

	data class RemoteControl(var item: ItemStack, val block: Block, var displayName: String)

	companion object {
		val HUNTER_SPAWN_META = "CLASSES_HUNTER_SPAWN"

		var obsidianifiedLava: MutableList<ObsidianifiedLava> = mutableListOf()

		var grindedStone: MutableList<Block> = mutableListOf()

		val inHandMap = mutableMapOf<UUID, InHandItem>()
		val lastShiftMap = mutableMapOf<UUID, Long>()

		val remoteControls = mutableListOf<RemoteControl>()

		val superBreakMap: MutableMap<UUID, Long> = mutableMapOf()

		fun updateRemoteControl(control: RemoteControl): ItemStack {
			val lever = control.block
			val leverData = lever.blockData as? Switch
			val statusColor =
					when {
						leverData == null -> {
							ChatColor.BLUE
						}
						leverData.isPowered -> {
							ChatColor.GREEN
						}
						else -> {
							ChatColor.RED
						}
					}
			val currentStatus =
					when {
						leverData == null -> {
							"DESTROYED"
						}
						leverData.isPowered -> {
							"POWERED"
						}
						else -> {
							"UNPOWERED"
						}
					}
			val newItem = ItemStack(Material.REDSTONE_TORCH)
			val meta = newItem.itemMeta
			meta.setDisplayName("${ChatColor.RESET}${statusColor}${control.displayName}")
			meta.lore = mutableListOf("${ChatColor.GRAY}Controlling the lever at (" +
					ChatColor.GOLD + lever.x + ChatColor.GRAY + ", " +
					ChatColor.GOLD + lever.y + ChatColor.GRAY + ", " +
					ChatColor.GOLD + lever.z + ChatColor.GRAY + ").",
					"${ChatColor.GRAY}Current status: $statusColor$currentStatus"
			)
			newItem.itemMeta = meta
			control.item = newItem
			return newItem
		}

		fun setClass(playerData: PlayerData, quirkClass: QuirkClass) {
			PlayerData.getQuirkDataHolder(playerData, QuirkType.CLASSES).data = quirkClass
		}

		fun setClass(uuid: UUID, quirkClass: QuirkClass) {
			setClass(PlayerData.getPlayerData(uuid), quirkClass)
		}

		fun startAsClass(player: Player, quirkClass: QuirkClass, oldClass: QuirkClass) {
			if (oldClass != QuirkClass.NO_CLASS) oldClass.onEnd(player)

			giveClassHead(player, quirkClass)
			quirkClass.onStart(player)
		}

		fun getClass(playerData: PlayerData): QuirkClass {
			return PlayerData.getQuirkData(playerData, QuirkType.CLASSES)
		}

		fun getClass(uuid: UUID): QuirkClass {
			return getClass(PlayerData.getPlayerData(uuid))
		}

		fun giveClassHead(player: Player, quirkClass: QuirkClass) {
			val headItem = ItemStack(quirkClass.headBlock)

			val meta = headItem.itemMeta
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
			if (quirkClass == QuirkClass.DIVER) {
				meta.addEnchant(Enchantment.WATER_WORKER, 1, true)
			}
			headItem.itemMeta = meta

			player.inventory.helmet = headItem
		}

		fun removeHead(player: Player) {
			player.inventory.helmet = null
		}

		private var timerId: Int = 0

	}
}

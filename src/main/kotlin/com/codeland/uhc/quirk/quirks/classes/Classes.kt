package com.codeland.uhc.quirk.quirks.classes

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import kotlin.math.abs

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

	override val representation: ItemStack
		get() = ItemStack(Material.LEATHER_HELMET)

	override fun defaultData(): Any {
		return QuirkClass.NO_CLASS
	}

	data class ObsidianifiedLava(val block: Block, val flowing: Boolean)

	companion object {
		var obsidianifiedLava: MutableList<ObsidianifiedLava> = mutableListOf()
		var grindedStone: MutableList<Block> = mutableListOf()

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
			headItem.itemMeta = meta

			player.inventory.helmet = headItem
		}

		fun removeHead(player: Player) {
			player.inventory.helmet = null
		}

		private var timerId: Int = 0
	}
}

package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.Summoner
import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.Classes.Companion.HUNTER_SPAWN_META
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.ChatColor.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.Grindstone
import org.bukkit.block.data.type.Switch
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

class ClassesEvents : Listener {
	private fun surface(block: Block): Boolean {
		return BlockFace.values().take(6).any { d ->
			block.getRelative(d).type.isAir
		}
	}

	fun <T> auraReplacer(player: Player, list: MutableList<T>, from: Material, to: Material, radiusX: Int, lowY: Int, highY: Int, radiusZ: Int, produce: (Block) -> T, modify: (Block) -> Unit) {
		for (dx in -radiusX..radiusX)
			for (dy in lowY..highY)
				for (dz in -radiusZ..radiusZ) {
					val block = player.location.block.getRelative(dx, dy, dz)

					if (block.type === from && surface(block)) {
						list.add(produce(block))
						block.setType(to, true)
						modify(block)
					}
				}
	}

	@EventHandler
	fun playerMove(event: PlayerMoveEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			val player = event.player

			when (Classes.getClass(player.uniqueId)) {
				QuirkClass.LAVACASTER -> {
					auraReplacer(player, Classes.obsidianifiedLava, Material.LAVA, Material.OBSIDIAN, 3, -2, -1, 3, { block ->
						Classes.ObsidianifiedLava(block, (block.blockData as Levelled).level != 0)
					}, {})
				}
				QuirkClass.ENCHANTER -> {
					auraReplacer(player, Classes.grindedStone, Material.STONE, Material.GRINDSTONE, 1, -2, -1, 1, { it }, { block ->
						val data = block.blockData as Grindstone
						data.attachedFace = FaceAttachable.AttachedFace.FLOOR
						block.setBlockData(data, false)
					})
				}
				QuirkClass.DIVER -> {
					val velocity = player.velocity.length()

					val motion = event.to.toVector().subtract(event.from.toVector())
					val horz = motion.clone().setY(0)

					if (player.isSwimming) {
						player.velocity = player.location.direction.multiply((1.5 - velocity) * 0.1 + velocity)
					}

					val from = event.from.block.type
					val to = event.to.block.type

					if (from === Material.WATER && to.isAir) {
						player.velocity = motion.clone().multiply(3)

					} else if (from.isAir && to === Material.WATER) {
						player.velocity = motion.clone().multiply(3)
					}
				}
			}
		}
	}

	@EventHandler
	fun playerDamage(event: EntityDamageEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			val player = if (event.entity is Player) event.entity as Player else return

			if (Classes.getClass(player.uniqueId) === QuirkClass.DIVER && event.cause === EntityDamageEvent.DamageCause.FALL && player.world.environment !== World.Environment.NETHER) {
				event.isCancelled = true
				val block = player.location.block
				if (!block.type.isAir) block.breakNaturally()
				block.type = Material.WATER
				block.world.playSound(block.location, Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f)
				SchedulerUtil.later(10) {
					if (block.type == Material.WATER) {
						block.type = Material.AIR
						block.world.playSound(block.location, Sound.ITEM_BUCKET_FILL, 1.0f, 1.0f)
					}
				}
			}
			if (Classes.getClass(player.uniqueId) == QuirkClass.TRAPPER && event.cause == EntityDamageEvent.DamageCause.FALL) {
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun entityDamageEntity(event: EntityDamageByEntityEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			val player = event.damager as? Player ?: return

			when (Classes.getClass(player.uniqueId)) {
				QuirkClass.LAVACASTER -> {
					event.entity.fireTicks = 80
				}
				QuirkClass.ENCHANTER -> {
					val hurtPlayer = event.entity as? Player ?: return

					val enchantColors = arrayOf(RED, GOLD, YELLOW, GREEN, AQUA, BLUE, LIGHT_PURPLE)
					fun enchantColor(index: Int) = enchantColors[index % enchantColors.size]

					fun tellItem(itemStack: ItemStack) {
						val meta = itemStack.itemMeta

						player.sendActionBar(
							meta.enchants.asIterable().mapIndexed { index, (enchant, level) ->
								"${enchantColor(index)}$BOLD${enchant.key.key} ${enchantColor(index)}$level"
							}.joinToString(", ", "$WHITE${hurtPlayer.name}'s $WHITE$BOLD${itemStack.type.name.toLowerCase()}: ")
						)
					}

					fun validItem(itemStack: ItemStack?) = itemStack != null &&
						itemStack.itemMeta?.enchants?.isNotEmpty() == true

					fun tellSlot(slot: Int): Boolean {
						val item = if (slot == 3)
							hurtPlayer.inventory.itemInMainHand
						else
							hurtPlayer.inventory.armorContents[slot]

						return if (validItem(item)) {
							tellItem(item); true
						} else false
					}

					val offset = (Math.random() * 4).toInt()
					(0..3).any { slot -> tellSlot((slot + offset) % 4) }
				}
			}
		}
	}

	@EventHandler
	fun onXP(event: PlayerExpChangeEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			if (Classes.getClass(event.player.uniqueId) == QuirkClass.ENCHANTER) {
				event.amount = event.amount * 2
			}
		}
	}

	@EventHandler
	fun onEnchant(event: EnchantItemEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			if (Classes.getClass(event.enchanter.uniqueId) == QuirkClass.ENCHANTER) {
				val inventory = event.view.topInventory as EnchantingInventory

				val lapis = inventory.secondary

				if (lapis == null)
					inventory.secondary = ItemStack(Material.LAPIS_LAZULI)
				else
					++lapis.amount
			}
		}
	}

	val armors = arrayOf(
		Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
		Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET,
		Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET,
		Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET,
		Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET
	)

	val weapons = arrayOf(
		Material.WOODEN_SWORD, Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD,
		Material.WOODEN_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE
	)

	val bows = arrayOf(Material.BOW)

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			if (Classes.getClass(event.whoClicked.uniqueId) == QuirkClass.ENCHANTER) {
				val item = event.currentItem ?: return

				fun autoEnchant(materials: Array<Material>, enchant: Enchantment) =
					if (materials.contains(item.type)) {
						val meta = item.itemMeta
						meta.addEnchant(enchant, 1, true)
						item.itemMeta = meta
						true
					} else {
						false
					}

				autoEnchant(armors, Enchantment.PROTECTION_ENVIRONMENTAL) ||
				autoEnchant(weapons, Enchantment.DAMAGE_ALL) ||
				autoEnchant(bows, Enchantment.ARROW_DAMAGE)
			}
		}
	}

	@EventHandler
	fun blockBreak(event: BlockBreakEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			if (Classes.getClass(event.player.uniqueId) == QuirkClass.TRAPPER) {
				val logs = listOf(
						Material.OAK_LOG,
						Material.BIRCH_LOG,
						Material.DARK_OAK_LOG,
						Material.ACACIA_LOG,
						Material.SPRUCE_LOG,
						Material.JUNGLE_LOG)
				if (event.block.type in logs) {
					// breaks all blocks adjacent to this block of the same type, with a delay
					fun breakRecursively(block: Block, type: Material) {
						for (d in BlockFace.values().take(6)) {
							val nextBlock = block.getRelative(d)
							if (nextBlock.type == type) {
								SchedulerUtil.later(1) {
									// extra check to make sure the block hasn't changed
									if (nextBlock.type == type) {
										nextBlock.breakNaturally()
                                        nextBlock.world.playSound(nextBlock.location, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f)
										breakRecursively(nextBlock, type)
									}
								}
							}
						}
					}
					breakRecursively(event.block, event.block.type)
				}
				if (event.block.type == Material.REDSTONE_ORE) {
					// list of components with their relative frequency
					val components = listOf(
							Pair(10, Material.STONE_PRESSURE_PLATE),
							Pair(10, Material.REDSTONE_TORCH),
							Pair(10, Material.LEVER),
							Pair(10, Material.REPEATER),
							Pair(10, Material.COMPARATOR),
							Pair(5, Material.DROPPER),
							Pair(5, Material.DISPENSER),
							Pair(5, Material.TRAPPED_CHEST),
							Pair(5, Material.PISTON),
							Pair(5, Material.OBSERVER),
							Pair(1, Material.STICKY_PISTON),
							Pair(1, Material.SLIME_BLOCK),
							Pair(1, Material.TNT),
					)
					outer@ for (i in 1..5) {
						var total = components.map { it.first }.sum()
						for (c in components) {
							if (Math.random() < c.first.toDouble() / total) {
								event.block.world.dropItemNaturally(event.block.location, ItemStack(c.second))
								continue@outer
							} else {
								total -= c.first
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	fun interactEvent(event: PlayerInteractEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			if (Classes.getClass(event.player.uniqueId) == QuirkClass.TRAPPER) {
				when (event.action) {
					Action.LEFT_CLICK_BLOCK -> {
						if (event.player.isSneaking && event.clickedBlock!!.type == Material.LEVER) {
							val lever = event.clickedBlock!!
							val existing = Classes.remoteControls.find { (_, block, _) ->
								block == lever }
							if (existing != null && event.player.inventory.contains(existing.item)) {
								if (event.player.inventory.itemInMainHand != existing.item)
									event.player.sendMessage("${RED}You already have a controller for this lever.")
							} else {
								val item = ItemStack(Material.REDSTONE_TORCH)
								val control = Classes.RemoteControl(item, lever, "Remote Control")
								Classes.remoteControls.add(control)
								event.player.inventory.addItem(Classes.updateRemoteControl(control))
							}
						}
					}
					Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
						val control = Classes.remoteControls.find { (item, _, _) ->
							item == event.player.inventory.itemInMainHand }
						if (control != null) {
							event.isCancelled = true
							val leverData = control.block.blockData as? Switch
							if (leverData != null) {
								leverData.isPowered = !leverData.isPowered
								control.block.blockData = leverData
								control.block.world.playSound(
										control.block.location,
										Sound.BLOCK_LEVER_CLICK,
										1.0f,
										// some attempt to preserve the difference in pitch of on and off
										if (leverData.isPowered) 1.5f else 1.0f
								)
							}
							event.player.inventory.setItemInMainHand(Classes.updateRemoteControl(control))
						}
					}
				}
			}
		}
	}

	@EventHandler
	fun onMobSpawn(event: EntitySpawnEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			if (event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
				event.entity.setMetadata(HUNTER_SPAWN_META, FixedMetadataValue(UHCPlugin.plugin, true))
			}
		}
	}

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			val player = event.target as? Player ?: return

			if (Classes.getClass(player.uniqueId) == QuirkClass.HUNTER) {
				event.isCancelled = true
			}
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
			val player = event.player

			if (Classes.getClass(player.uniqueId) == QuirkClass.HUNTER) {
				if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
					val item = event.item

					if (item != null && item.type == Material.COMPASS) {
						fun onSameTeam(otherUUID: UUID): Boolean {
							val team = TeamData.playersTeam(otherUUID)
							return team != null && team.members.contains(player.uniqueId)
						}

						/* get the nearest player's location to the player */
						val trackLocation = PlayerData.playerDataList.asIterable().filter { (uuid, playerData) ->
							/* don't find the hunter themselves and don't find their teammates, spectators */
							playerData.participating && uuid != player.uniqueId && !onSameTeam(uuid)
						}.mapNotNull { (uuid, _) ->
							/* get the location of each other player */
							GameRunner.getPlayerLocation(uuid)
						}.filter { otherLocation ->
							/* prevent errors on .distance() */
							otherLocation.world == player.world
						}.map { otherLocation ->
							/* associate each location with a distance to player */
							Pair(otherLocation.distance(player.location), otherLocation)
						}.minBy { locationPair ->
							/* sort by distance to find the nearest */
							locationPair.first
						}?.second

						if (trackLocation == null) {
							player.sendActionBar("${RED}No players found!")

						} else {
							val vector = trackLocation.subtract(player.location).toVector().normalize()

							for (i in 0..64) {
								player.spawnParticle(Particle.REDSTONE, player.location.clone().add(vector.clone().multiply(i * (1.0 / 3.0))).add(0.0, 1.0, 0.0), 3, 0.1, 0.1, 0.1, Particle.DustOptions(Color.RED, 1.0f))
							}

							--item.amount
						}
					}
				}
			}
		}
	}
}

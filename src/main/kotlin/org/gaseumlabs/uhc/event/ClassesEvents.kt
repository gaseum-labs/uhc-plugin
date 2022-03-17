package org.gaseumlabs.uhc.event

import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.quirk.quirks.classes.Classes
import org.gaseumlabs.uhc.quirk.quirks.classes.Classes.Companion.HUNTER_SPAWN_META
import org.gaseumlabs.uhc.quirk.quirks.classes.QuirkClass
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.Util.materialRange
import org.gaseumlabs.uhc.util.extensions.LocationExtensions.minus
import org.gaseumlabs.uhc.util.extensions.LocationExtensions.plus
import org.gaseumlabs.uhc.util.extensions.VectorExtensions.plus
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.ChatColor.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Switch
import org.bukkit.block.data.type.Wall
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.*
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.random.Random

class ClassesEvents : Listener {
	private fun surface(block: Block): Boolean {
		return BlockFace.values().take(6).any { d ->
			block.getRelative(d).type.isAir
		}
	}

	fun <T> auraReplacer(
		player: Player,
		list: MutableList<T>,
		from: Material,
		to: Material,
		radiusX: Int,
		lowY: Int,
		highY: Int,
		radiusZ: Int,
		produce: (Block) -> T,
		modify: (Block) -> Unit,
	) {
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
	fun playerDamage(event: EntityDamageEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		val player = if (event.entity is Player) event.entity as Player else return

		if (classes.getClass(player.uniqueId) === QuirkClass.DIVER && event.cause === EntityDamageEvent.DamageCause.FALL) {
			event.isCancelled = true

			val block = player.location.block
			if (!block.type.isAir) block.breakNaturally()

			block.type = Material.WATER
			block.world.playSound(block.location.toCenterLocation(), Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f)

			if (player.isSprinting && block.world.environment != World.Environment.NETHER) player.velocity =
				player.location.direction.multiply(2)

			SchedulerUtil.later(if (block.world.environment == World.Environment.NETHER) 10 else 60) {
				if (block.type == Material.WATER) {
					block.type = Material.AIR
					block.world.playSound(block.location.toCenterLocation(), Sound.ITEM_BUCKET_FILL, 1.0f, 1.0f)
				}
			}
		}

		if (classes.getClass(player.uniqueId) == QuirkClass.ENGINEER && event.cause == EntityDamageEvent.DamageCause.FALL) {
			event.isCancelled = true
		}

		if (classes.getClass(player.uniqueId) == QuirkClass.ENCHANTER) {
			if (event.damage > player.health
				&& player.level > 0
				&& event.damage <= player.health + player.level
			) {
				event.isCancelled = true
				val health = player.health + player.level
				player.level = 0
				player.health = if (health > player.maxHealth) player.maxHealth else health
				if (health > player.maxHealth) {
					player.absorptionAmount += health - player.maxHealth
				}
			}
		}
	}

	@EventHandler
	fun playerMove(event: PlayerMoveEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		val player = event.player

		when (classes.getClass(player.uniqueId)) {
			QuirkClass.LAVACASTER -> {
				auraReplacer(player,
					Classes.obsidianifiedLava,
					Material.LAVA,
					Material.OBSIDIAN,
					3,
					-2,
					-1,
					3,
					{ block ->
						Classes.ObsidianifiedLava(block, (block.blockData as Levelled).level != 0)
					},
					{})
			}
			QuirkClass.DIVER -> {
				if (player.world.environment != World.Environment.NETHER) {
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
						player.velocity = motion.clone().normalize().multiply(3).setY(motion.y)
						player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40, 2, false, false, false))
					}
				}
			}
		}
	}

	@EventHandler
	fun entityDamageEntity(event: EntityDamageByEntityEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		val player = event.damager as? Player ?: return

		when (classes.getClass(player.uniqueId)) {
			QuirkClass.LAVACASTER -> {
				event.entity.fireTicks = 80
			}
			QuirkClass.ENCHANTER -> {
				val hurtPlayer = event.entity as? Player ?: return

				val enchantColors = arrayOf(RED, GOLD, YELLOW, GREEN, AQUA, BLUE, LIGHT_PURPLE)
				fun enchantColor(index: Int) = enchantColors[index % enchantColors.size]

				fun tellItem(itemStack: ItemStack?) {
					val meta = itemStack?.itemMeta ?: return

					player.sendActionBar(Component.text(
						meta.enchants.asIterable().mapIndexed { index, (enchant, level) ->
							"${enchantColor(index)}$BOLD${enchant.key.key} ${enchantColor(index)}$level"
						}.joinToString(", ",
							"$WHITE${hurtPlayer.name}'s $WHITE$BOLD${itemStack.type.name.lowercase()}: "
						)
					))
				}

				fun validItem(itemStack: ItemStack?) = itemStack != null &&
				itemStack.itemMeta?.enchants?.isNotEmpty() == true

				fun tellSlot(slot: Int): Boolean {
					val item = if (slot == 3)
						hurtPlayer.inventory.itemInMainHand
					else
						hurtPlayer.inventory.armorContents!![slot]

					return if (validItem(item)) {
						tellItem(item); true
					} else false
				}

				val offset = (Math.random() * 4).toInt()
				(0..3).any { slot -> tellSlot((slot + offset) % 4) }
			}
		}
	}

	@EventHandler
	fun onXP(event: PlayerExpChangeEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		if (classes.getClass(event.player.uniqueId) == QuirkClass.ENCHANTER) {
			event.amount = event.amount * 2
		}
	}

	@EventHandler
	fun onBlockDrop(event: BlockBreakEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		if (classes.getClass(event.player.uniqueId) == QuirkClass.ENCHANTER && event.block.type == Material.GRINDSTONE) {
			event.isDropItems = false
			event.block.world.dropItem(event.block.location.toCenterLocation(), ItemStack(Material.COBBLESTONE))
		}
	}

	@EventHandler
	fun onEnchant(event: EnchantItemEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		if (classes.getClass(event.enchanter.uniqueId) == QuirkClass.ENCHANTER) {
			val inventory = event.view.topInventory as EnchantingInventory

			val lapis = inventory.secondary

			if (lapis == null)
				inventory.secondary = ItemStack(Material.LAPIS_LAZULI)
			else
				++lapis.amount
		}
	}

	val armors = arrayOf(
		Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET,
		Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET,
		Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET,
		Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET,
		Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET,
		Material.NETHERITE_BOOTS, Material.NETHERITE_LEGGINGS, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_HELMET,
	)

	val weapons = arrayOf(
		Material.WOODEN_SWORD,
		Material.GOLDEN_SWORD,
		Material.STONE_SWORD,
		Material.IRON_SWORD,
		Material.DIAMOND_SWORD,
		Material.NETHERITE_SWORD,
		Material.WOODEN_AXE,
		Material.GOLDEN_AXE,
		Material.STONE_AXE,
		Material.IRON_AXE,
		Material.DIAMOND_AXE,
		Material.NETHERITE_AXE
	)

	val pickaxes = arrayOf(
		Material.WOODEN_PICKAXE,
		Material.GOLDEN_PICKAXE,
		Material.STONE_PICKAXE,
		Material.IRON_PICKAXE,
		Material.DIAMOND_PICKAXE,
		Material.NETHERITE_PICKAXE
	)

	val bows = arrayOf(Material.BOW)

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		if (classes.getClass(event.whoClicked.uniqueId) == QuirkClass.ENCHANTER) {
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
			autoEnchant(pickaxes, Enchantment.DIG_SPEED) ||
			autoEnchant(bows, Enchantment.ARROW_DAMAGE)
		}
	}

	@EventHandler
	fun blockBreak(event: BlockBreakEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		if (classes.getClass(event.player.uniqueId) == QuirkClass.ENGINEER) {
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

	@EventHandler
	fun interactEvent(event: PlayerInteractEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		when (event.action) {
			Action.LEFT_CLICK_BLOCK -> {
				if (classes.getClass(event.player.uniqueId) == QuirkClass.ENGINEER) {
					if (event.player.isSneaking && event.clickedBlock!!.type == Material.LEVER) {
						val lever = event.clickedBlock!!
						val existing = Classes.remoteControls.find { (_, block, _) ->
							block == lever
						}
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
			}
			Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK -> {
				val control = Classes.remoteControls.find { (item, _, _) ->
					item == event.player.inventory.itemInMainHand
				}
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

		if (
			classes.getClass(event.player.uniqueId) == QuirkClass.MINER &&
			pickaxes.contains(event.player.inventory.itemInMainHand.type) &&
			event.hand == EquipmentSlot.HAND
		) {
			if (event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR) {
				val minerData = Classes.minerDatas[event.player.uniqueId]

				if (minerData != null) {
					val superBreakTimer = minerData.superBreakTimer

					if (superBreakTimer < 20 * 30) {
						val timeLeftSeconds = 30 - (superBreakTimer / 20)

						return event.player.sendMessage("${RED}You can't use that yet. Try again in ${
							when (timeLeftSeconds) {
								0 -> "a moment"
								1 -> "1 second"
								else -> "$timeLeftSeconds seconds"
							}
						}.")

					} else {
						fun superRecursive(
							x: Double,
							z: Double,
							dx: Double,
							dz: Double,
							y: Double,
							n: Int,
							limit: Int,
						) {
							if (n > limit) return

							for (i in -1..1) for (j in 0..1) for (k in -1..1) {
								val block =
									event.player.world.getBlockAt(x.toInt(), y.toInt(), z.toInt()).getRelative(i, j, k)

								if (block.type != Material.BEDROCK && block.type != Material.OBSIDIAN) {
									block.breakNaturally()
								}
							}

							event.player.world.playSound(Location(event.player.world, x, y, z),
								Sound.BLOCK_STONE_BREAK,
								1.0f,
								1.0f)

							SchedulerUtil.later(4) { superRecursive(x + dx, z + dz, dx, dz, y, n + 1, limit) }
						}

						val dir = event.player.location.direction.setY(0).normalize()

						val numBlocks = when {
							superBreakTimer < 30 * 20 -> superBreakTimer / 20 / 3.0
							superBreakTimer < 60 * 20 -> 10 + (superBreakTimer - 30 * 20) / 20 / 1.5
							else -> 30.0
						}.toInt()

						/* begin digging tunnel */
						superRecursive(event.player.location.x,
							event.player.location.z,
							dir.x,
							dir.z,
							event.player.location.y,
							0,
							numBlocks)

						/* reset timer */
						minerData.superBreakTimer = 0
					}
				}
			}
		}
	}

	@EventHandler
	fun itemBreak(event: PlayerItemBreakEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		if (classes.getClass(event.player.uniqueId) == QuirkClass.MINER && pickaxes.contains(event.brokenItem.type)) {
			xray(event.player)
		}
	}

	private fun xray(sender: Player) {
		val radius = 35
		val shouldRemove = listOf(
			Material.STONE,
			Material.ANDESITE,
			Material.DIORITE,
			Material.GRANITE,
			Material.DIRT,
			Material.GRAVEL,
			Material.SANDSTONE,
			Material.SAND
		)
		val centerLocation = sender.location.clone()
		sender.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 6 * radius + 20 * 20, 1))
		fun fillRecursive(z: Int, limit: Int) {
			if (z > limit) return
			for (dx in -radius..radius) for (dy in -radius..radius) {
				if (centerLocation.block.getRelative(dx, dy, z).type in shouldRemove) {
					sender.sendBlockChange(centerLocation.block.getRelative(dx, dy, z).location,
						Material.BARRIER.createBlockData())
				}
				if (z != 0 && centerLocation.block.getRelative(dx, dy, -z).type in shouldRemove) {
					sender.sendBlockChange(centerLocation.block.getRelative(dx, dy, -z).location,
						Material.BARRIER.createBlockData())
				}
			}
			SchedulerUtil.later(6) {
				fillRecursive(z + 1, limit)
			}
		}

		fun restoreRecursive(z: Int) {
			if (z < 0) return
			for (dx in -radius..radius) for (dy in -radius..radius) {
				if (centerLocation.block.getRelative(dx, dy, z).type in shouldRemove) {
					sender.sendBlockChange(centerLocation.block.getRelative(dx, dy, z).location,
						centerLocation.block.getRelative(dx, dy, z).blockData)
				}
				if (z != 0 && centerLocation.block.getRelative(dx, dy, -z).type in shouldRemove) {
					sender.sendBlockChange(centerLocation.block.getRelative(dx, dy, -z).location,
						centerLocation.block.getRelative(dx, dy, -z).blockData)
				}
			}
			SchedulerUtil.later(6) {
				restoreRecursive(z - 1)
			}
		}
		fillRecursive(0, radius)
		SchedulerUtil.later((6 * radius * 2 + 20 * 20).toLong()) {
			restoreRecursive(radius)
		}
	}

	@EventHandler
	fun onMobSpawn(event: EntitySpawnEvent) {
		if (UHC.game?.quirkEnabled(QuirkType.CLASSES) == true) {
			if (event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
				event.entity.setMetadata(HUNTER_SPAWN_META, FixedMetadataValue(org.gaseumlabs.uhc.UHCPlugin.plugin, true))
			}
		}
	}

	@EventHandler
	fun onMobAnger(event: EntityTargetLivingEntityEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		val player = event.target as? Player ?: return

		if (classes.getClass(player.uniqueId) == QuirkClass.HUNTER) {
			event.isCancelled = true
		}
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		val game = UHC.game ?: return
		val classes = game.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		val player = event.player

		if (classes.getClass(player.uniqueId) == QuirkClass.HUNTER) {
			if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
				val item = event.item

				if (item != null && item.type == Material.COMPASS) {
					fun onSameTeam(otherUUID: UUID): Boolean {
						val team = game.teams.playersTeam(otherUUID)
						return team != null && team.members.contains(player.uniqueId)
					}

					/* get the nearest player's location to the player */
					val trackLocation = PlayerData.playerDataList.asIterable().filter { (uuid, playerData) ->
						/* don't find the hunter themselves and don't find their teammates, spectators */
						playerData.participating && uuid != player.uniqueId && !onSameTeam(uuid)
					}.mapNotNull { (uuid, _) ->
						/* get the location of each other player */
						org.gaseumlabs.uhc.util.Action.getPlayerLocation(uuid)
					}.filter { otherLocation ->
						/* prevent errors on .distance() */
						otherLocation.world == player.world
					}.map { otherLocation ->
						/* associate each location with a distance to player */
						Pair(otherLocation.distance(player.location), otherLocation)
					}.minByOrNull { locationPair ->
						/* sort by distance to find the nearest */
						locationPair.first
					}?.second

					if (trackLocation == null) {
						player.sendActionBar("${RED}No players found!")

					} else {
						val vector = trackLocation.subtract(player.location).toVector().normalize()

						for (i in 0..64) {
							player.spawnParticle(Particle.REDSTONE,
								player.location.clone().add(vector.clone().multiply(i * (1.0 / 3.0)))
									.add(0.0, 1.0, 0.0),
								3,
								0.1,
								0.1,
								0.1,
								Particle.DustOptions(Color.RED, 1.0f))
						}

						--item.amount
					}
				}
			}
		}
	}

	@EventHandler
	fun onBlockPlace(event: BlockPlaceEvent) {
		val classes = UHC.game?.getQuirk<Classes>(QuirkType.CLASSES) ?: return

		val player = event.player

		if (classes.getClass(player.uniqueId) == QuirkClass.ENGINEER) {
			when (event.block.type) {
				Material.LIGHT_WEIGHTED_PRESSURE_PLATE -> {
					for (x in -1..1) for (y in 1..7) for (z in -1..1) {
						if (player.location.block.getRelative(x, y, z).type.isSolid) {
							event.isCancelled = true
							org.gaseumlabs.uhc.util.Action.sendGameMessage(player,
								"You can't use that here. Get to a more open area.")
							return
						}
					}
					val location = event.blockPlaced.location + Vector(0.0, 5.0, 0.0)
					player.velocity = Vector(0.0, 1.2, 0.0) // fine tune
					SchedulerUtil.later(10) {
						SchedulerUtil.delayedFor(1, 0..3) { i ->
							for (dx in -i..i) for (dz in -i..i) {
								val block = location.block.getRelative(dx, 0, dz)
								if (block.type == Material.AIR) {
									block.type = Material.COBBLESTONE
								}
							}
						}
					}
				}
				Material.STONE_BRICK_WALL -> {
					val location = event.blockPlaced.location.clone()
					val difference = location - player.location
					val facingX = (event.player.getTargetBlockFace(10) ?: return).ordinal % 2 != 0
					SchedulerUtil.delayedFor(1, 0 until 5) { i ->
						for (dl in -2..2) {
							if (facingX) event.blockPlaced.getRelative(0, i, dl).type = Material.STONE
							else event.blockPlaced.getRelative(dl, i, 0).type = Material.STONE
						}
					}
				}
				Material.POLISHED_GRANITE_STAIRS -> {
					val location = event.blockPlaced.location.clone()
					val difference = location - player.location
					val direction = (event.blockPlaced.blockData as Stairs).facing
					event.blockPlaced.type = Material.COBBLESTONE_STAIRS
					SchedulerUtil.delayedFor(1, 0..8) { i ->
						for (dl in -1..1) {
							val block = when (direction) {
								BlockFace.EAST -> event.blockPlaced.getRelative(i, i, dl)
								BlockFace.WEST -> event.blockPlaced.getRelative(-i, i, dl)
								BlockFace.SOUTH -> event.blockPlaced.getRelative(dl, i, i)
								BlockFace.NORTH -> event.blockPlaced.getRelative(dl, i, -i)
								else -> throw Exception("can't")
							}
							if (block.type == Material.AIR) block.type = Material.COBBLESTONE_STAIRS
							val stairData: Stairs = block.blockData as Stairs
							stairData.facing = direction
							block.blockData = stairData

							val below = block.getRelative(0, -1, 0)
							if (below.type == Material.AIR) below.type = Material.COBBLESTONE
						}
					}
				}
				else -> {
				}
			}
		}
	}
}

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Util
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.ItemUtil
import com.codeland.uhc.quirk.ItemUtil.randFromArray
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import net.md_5.bungee.api.ChatColor.GOLD
import org.bukkit.ChatColor.*
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Item
import org.bukkit.entity.SpectralArrow
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SuspiciousStewMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.*
import kotlin.math.roundToInt

class CarePackages(type: QuirkType) : Quirk(type) {
	val OBJECTIVE_NAME = "carePackageDrop"

	override fun onEnable() {
		/* start the carepackage timer loop */
		currentRunnable = generateRunnable()
		currentRunnable?.runTaskTimer(GameRunner.plugin, 0, 20)

		/* get rid of any lingering objectives from this quirk */
		var scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		scoreboard.getObjective(OBJECTIVE_NAME)?.unregister()

		objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", "Next Care Package Drop")
		objective?.displaySlot = DisplaySlot.SIDEBAR
	}

	override fun onDisable() {
		currentRunnable?.cancel()
		currentRunnable = null

		objective?.unregister()
	}

	var objective = null as Objective?
	var currentRunnable = null as BukkitRunnable?

	var scoreT = null as Score?
	var scoreP = null as Score?

	fun generateRunnable(): BukkitRunnable {
		return object : BukkitRunnable() {
			val minTime = 2 * 60
			val maxTime = 10 * 60

			val minItems = 9
			val maxItems = 18

			var running = false

			var timer = 0
			var nextLocation = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)

			lateinit var dropTimes: Array<Int>
			var dropIndex = 0

			fun setScore(objective: Objective?, score: Score?, value: String, index: Int): Score {
				/* trick kotlin in allowing us to return */
				if (objective == null) return Bukkit.getScoreboardManager().mainScoreboard.registerNewObjective("", "", "").getScore("")

				/* remove the previous score if applicable */
				if (score != null) {
					val scoreboard = objective.scoreboard
					scoreboard?.resetScores(score.entry)
				}

				val ret = objective.getScore(value)
				ret.score = index

				return ret
			}

			fun shutOff() {
				running = false
				objective?.displaySlot = null
			}

			fun reset() {
				/* don't keep doing this outside of shrinking */
				if (!GameRunner.uhc.isPhase(PhaseType.SHRINK) || dropIndex == dropTimes.size) {
					shutOff()
					return
				}

				timer = dropTimes[dropIndex]

				nextLocation = findDropSpot(timer, 16, Bukkit.getWorlds()[0].worldBorder)

				val coordinateString = "at (${ChatColor.GOLD}${BOLD}${nextLocation.blockX}${RESET}, ${ChatColor.GOLD}${BOLD}${nextLocation.blockY}${RESET}, ${ChatColor.GOLD}${BOLD}${nextLocation.blockZ}${RESET})"

				scoreT = setScore(objective, scoreT, "in ${ChatColor.GOLD}${BOLD}${Util.timeString(timer)}", 1)
				scoreP = setScore(objective, scoreP, coordinateString, 0)
			}

			fun generateDropTimes(shrinkTime: Int): Array<Int> {
				/* we want about 1 every 5 minutes */
				val numDrops = lootEntries.size

				val averageTime = shrinkTime / numDrops
				val ret = Array(numDrops) { averageTime }

				/* add randomness in the times */
				for (i in ret.indices) {
					/* half a minute to a minute differences */
					var shiftAmount = Util.randRange(60, 120)

					var reduceIndex = Util.randRange(0, ret.lastIndex)
					var gainIndex = Util.randRange(0, ret.lastIndex)

					var oldReduce = ret[reduceIndex]
					ret[reduceIndex] -= shiftAmount

					/* don't reduce any time to less than a minute */
					/* if it would, find the actual amount it got reduced by */
					if (ret[reduceIndex] < 60) {
						ret[reduceIndex] = 60
						shiftAmount = oldReduce - ret[reduceIndex]
					}

					ret[gainIndex] += shiftAmount
				}

				return ret
			}

			fun generateDropAmount(dropIndex: Int, tiers: Int, dropTimes: Array<Int>): Array<Int> {
				val lastDropIndex = dropTimes.lastIndex
				val along = dropIndex.toFloat() / lastDropIndex.toFloat()

				Util.log("dropindex: $dropIndex out of ${dropTimes.lastIndex}")
				Util.log("along: $along")

				return Array<Int>(tiers) { tier ->
					var peak = (1.0 / (tiers - 1.0)) * tier

					var amount = 7 - Math.abs(3 * tiers * (along - peak))

					if (amount < 0) amount = 0.0

					Util.log("amount for ${tier}: ${amount.roundToInt()}")

					amount.roundToInt()
				}
			}

			override fun run() {
				if (running) {
					--timer
					if (timer == 0) {
						generateDrop(generateDropAmount(dropIndex, lootEntries.size, dropTimes), nextLocation)
						++dropIndex

						reset()
					} else {
						scoreT = setScore(objective, scoreT, "in ${ChatColor.GOLD}${BOLD}${Util.timeString(timer)}", 1)
					}

				/* start making drops during shrinking round */
				} else if (GameRunner.uhc.isPhase(PhaseType.SHRINK)) {
					running = true
					dropTimes = generateDropTimes(GameRunner.uhc.getTime(PhaseType.SHRINK))

					dropTimes.forEach { drop ->
						Util.log("time: $drop")
					}

					reset()
				}
			}
		}
	}

	companion object {
		class ToolInfo(val materials: Array<Material>, val enchants: Array<Array<Enchantment>>) {
			companion object {
				val WOOD = 0; val LEATHER = 0
				val GOLD = 1
				val STONE = 2; val CHAIN = 2
				val IRON = 3
				val DIAMOND = 4
				val NETHERITE = 5
				val SHELL = 6
			}
		}

		val weapons = arrayOf(
			ToolInfo(arrayOf(Material.WOODEN_SWORD, Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.LOOT_BONUS_MOBS),
				arrayOf(Enchantment.FIRE_ASPECT),
				arrayOf(Enchantment.KNOCKBACK),
				arrayOf(Enchantment.SWEEPING_EDGE),
				arrayOf(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD)
			)),
			ToolInfo(arrayOf(Material.WOODEN_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH),
				arrayOf(Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD)
			))
		)

		val tools = arrayOf(
			ToolInfo(arrayOf(Material.WOODEN_PICKAXE, Material.GOLDEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH)
			)),
			ToolInfo(arrayOf(Material.WOODEN_SHOVEL, Material.GOLDEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH)
			)),
			ToolInfo(arrayOf(Material.WOODEN_HOE, Material.GOLDEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.DIG_SPEED),
				arrayOf(Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH)
			))
		)

		val armor = arrayOf(
			ToolInfo(arrayOf(Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.OXYGEN),
				arrayOf(Enchantment.WATER_WORKER),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			)),
			ToolInfo(arrayOf(Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			)),
			ToolInfo(arrayOf(Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			)),
			ToolInfo(arrayOf(Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS), arrayOf(
				arrayOf(Enchantment.DURABILITY),
				arrayOf(Enchantment.MENDING),
				arrayOf(Enchantment.THORNS),
				arrayOf(Enchantment.PROTECTION_FALL),
				arrayOf(Enchantment.SOUL_SPEED),
				arrayOf(Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER),
				arrayOf(Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE)
			))
		)

		val bow = ToolInfo(arrayOf(Material.BOW), arrayOf(
			arrayOf(Enchantment.DURABILITY),
			arrayOf(Enchantment.MENDING, Enchantment.ARROW_INFINITE),
			arrayOf(Enchantment.ARROW_FIRE),
			arrayOf(Enchantment.ARROW_KNOCKBACK),
			arrayOf(Enchantment.ARROW_DAMAGE)
		))

		val crossbow = ToolInfo(arrayOf(Material.CROSSBOW), arrayOf(
			arrayOf(Enchantment.PIERCING, Enchantment.MULTISHOT),
			arrayOf(Enchantment.MENDING),
			arrayOf(Enchantment.DURABILITY),
			arrayOf(Enchantment.QUICK_CHARGE)
		))

		val elytra = ToolInfo(arrayOf(Material.ELYTRA), arrayOf(
			arrayOf(Enchantment.MENDING),
			arrayOf(Enchantment.DURABILITY)
		))

		val trident = ToolInfo(arrayOf(Material.TRIDENT), arrayOf(
			arrayOf(Enchantment.MENDING),
			arrayOf(Enchantment.DURABILITY),
			arrayOf(Enchantment.IMPALING),
			arrayOf(Enchantment.CHANNELING, Enchantment.LOYALTY, Enchantment.RIPTIDE)
		))

		fun randTool(toolArray: Array<ToolInfo>, material: Int, enchantChance: Double): ItemStack {
			var toolInfo = randFromArray(toolArray)
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[material]), toolInfo.enchants, enchantChance)
		}

		fun randTool(toolArray: Array<ToolInfo>, enchantChance: Double): ItemStack {
			var toolInfo = randFromArray(toolArray)
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[0]), toolInfo.enchants, enchantChance)
		}

		fun aTool(toolInfo: ToolInfo, enchantChance: Double): ItemStack {
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[0]), toolInfo.enchants, enchantChance)
		}

		fun aTool(toolInfo: ToolInfo, material: Int, enchantChance: Double): ItemStack {
			return ItemUtil.addRandomEnchants(ItemStack(toolInfo.materials[material]), toolInfo.enchants, enchantChance)
		}

		fun stewPart(): ItemStack {
			val rand = Math.random()

			return when {
				rand < 0.2 -> ItemStack(Material.BOWL, Util.randRange(4, 16))
				rand < 0.4 -> {
					val stew = ItemStack(Material.SUSPICIOUS_STEW, Util.randRange(4, 16))

					val meta = stew.itemMeta as SuspiciousStewMeta
					meta.addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 8, 0), true)
					stew.itemMeta = meta

					stew
				}
				rand < 0.6 -> ItemStack(Material.RED_MUSHROOM, Util.randRange(4, 16))
				rand < 0.8 -> ItemStack(Material.BROWN_MUSHROOM, Util.randRange(4, 16))
				else -> ItemStack(Material.OXEYE_DAISY, Util.randRange(4, 16))
			}
		}

		fun canePart(): ItemStack {
			return if (Math.random() < 0.5) {
				ItemStack(Material.SUGAR_CANE, Util.randRange(6, 18))
			} else {
				ItemStack(Material.PAPER, Util.randRange(6, 18))
			}
		}

		fun ironPart(): ItemStack {
			return if (Math.random() < 0.5) {
				ItemStack(Material.IRON_INGOT, Util.randRange(3, 6))
			} else {
				ItemStack(Material.IRON_NUGGET, Util.randRange(12, 24))
			}
		}

		fun goldPart(): ItemStack {
			return if (Math.random() < 0.5) {
				ItemStack(Material.GOLD_INGOT, Util.randRange(3, 6))
			} else {
				ItemStack(Material.GOLD_NUGGET, Util.randRange(12, 24))
			}
		}

		fun bucket(): ItemStack {
			val rand = Math.random()

			return when {
				rand < 1.0 / 3.0 -> ItemStack(Material.LAVA_BUCKET)
				rand < 2.0 / 3.0 -> ItemStack(Material.WATER_BUCKET)
				else -> ItemStack(Material.BUCKET)
			}
		}

		val brewingIngredients = arrayOf(
			Material.RABBIT_FOOT,
			Material.GLISTERING_MELON_SLICE,
			Material.SPIDER_EYE,
			Material.GHAST_TEAR,
			Material.MAGMA_CREAM,
			Material.PUFFERFISH,
			Material.PHANTOM_MEMBRANE,
			Material.REDSTONE,
			Material.GLOWSTONE_DUST,
			Material.FERMENTED_SPIDER_EYE,
			Material.DRAGON_BREATH,
			Material.BLAZE_POWDER
		)

		fun brewingIngredient(): ItemStack {
			return ItemStack(ItemUtil.randFromArray(brewingIngredients), Util.randRange(4, 16))
		}

		fun anyThrow(): Material {
			val rand = Math.random()

			return when {
				rand < 0.5 -> Material.POTION
				rand < 0.75 -> Material.SPLASH_POTION
				else -> Material.LINGERING_POTION
			}
		}

		fun splashLinger(): Material {
			return if (Math.random() < 0.5)
				Material.SPLASH_POTION
			else
				Material.LINGERING_POTION
		}

		fun arrowPart(): ItemStack {
			val rand = Math.random()

			return when {
				rand < 1.0 / 3.0 -> ItemStack(Material.FLINT, Util.randRange(12, 24))
				rand < 2.0 / 3.0 -> ItemStack(Material.STICK, Util.randRange(12, 24))
				else -> ItemStack(Material.FEATHER, Util.randRange(12, 24))
			}
		}

		class LootEntry(var makeStack: () -> ItemStack)

		val lootEntries = arrayOf(
			arrayOf(
				LootEntry { randTool(weapons, ToolInfo.WOOD, 0.25) },
				LootEntry { randTool(tools, ToolInfo.WOOD, 0.25) },
				LootEntry { randTool(armor, ToolInfo.LEATHER, 0.25) },
				LootEntry { ItemStack(Material.COOKED_BEEF, Util.randRange(2,  5)) },
				LootEntry { ItemStack(Material.GUNPOWDER, Util.randRange(5, 12)) },
				LootEntry { arrowPart() },
				LootEntry { canePart() },
				LootEntry { ItemStack(Material.STRING, Util.randRange(3, 10)) },
				LootEntry { stewPart() },
				LootEntry { ItemStack(Material.APPLE, Util.randRange(1, 6)) }
			),
			arrayOf(
				LootEntry { randTool(weapons, ToolInfo.GOLD, 0.5) },
				LootEntry { randTool(tools,  ToolInfo.GOLD, 0.5) },
				LootEntry { randTool(armor,  ToolInfo.GOLD, 0.5) },
				LootEntry { aTool(bow, 0.25) },
				LootEntry { aTool(crossbow, 0.25) },
				LootEntry { ItemStack(Material.LEATHER, Util.randRange(2,  6)) },
				LootEntry { ItemStack(Material.BOOK, Util.randRange(4,  9)) },
				LootEntry { ItemStack(Material.BOOKSHELF, Util.randRange(2, 4)) },
				LootEntry { ItemStack(Material.ARROW, Util.randRange(8, 32)) },
				LootEntry { ItemStack(Material.SADDLE) }
			),
			arrayOf(
				LootEntry { randTool(weapons, ToolInfo.STONE, 0.25) },
				LootEntry { randTool(tools, ToolInfo.STONE, 0.25) },
				LootEntry { randTool(armor, ToolInfo.CHAIN, 0.25) },
				LootEntry { ironPart() },
				LootEntry { goldPart() },
				LootEntry { ItemStack(Material.LAPIS_LAZULI, Util.randRange(8, 16)) },
				LootEntry { ItemStack(Material.TNT, Util.randRange(1,  9)) },
				LootEntry { ItemStack(Material.OBSIDIAN, Util.randRange(3,  7)) },
				LootEntry { bucket() },
				LootEntry { ItemStack(Material.ENDER_PEARL, Util.randRange(2,  7)) }
			),
			arrayOf(
				LootEntry { randTool(weapons, ToolInfo.IRON, 0.25) },
				LootEntry { randTool(tools, ToolInfo.IRON, 0.25) },
				LootEntry { randTool(armor, ToolInfo.IRON, 0.25) },
				LootEntry { aTool(armor[0], ToolInfo.SHELL, 0.25) },
				LootEntry { ItemStack(Material.GOLDEN_CARROT, Util.randRange(3,  6)) },
				LootEntry { ItemUtil.randomEnchantedBook() },
				LootEntry { ItemUtil.randomFireworkStar(Util.randRange(3, 7)) },
				LootEntry { ItemUtil.randomRocket(Util.randRange(3, 6)) },
				LootEntry { ItemStack(Material.GOLDEN_APPLE, Util.randRange(1,  3)) },
				LootEntry { ItemStack(Material.EXPERIENCE_BOTTLE, Util.randRange(4, 8)) },
				LootEntry { ItemStack(Material.SPECTRAL_ARROW, Util.randRange(4, 16)) }
			),
			arrayOf(
				LootEntry { randTool(weapons, ToolInfo.DIAMOND, 0.25) },
				LootEntry { randTool(tools, ToolInfo.DIAMOND, 0.25) },
				LootEntry { randTool(armor, ToolInfo.DIAMOND, 0.25) },
				LootEntry { aTool(trident, 0.25) },
				LootEntry { ItemStack(Material.BLAZE_ROD, Util.randRange(2, 4)) },
				LootEntry { brewingIngredient() },
				LootEntry { ItemStack(Material.NETHER_WART, Util.randRange(4,  7)) },
				LootEntry { ItemUtil.randomPotion(true, anyThrow()) },
				LootEntry { ItemUtil.randomPotion(false, splashLinger()) },
				LootEntry { ItemStack(Material.DIAMOND, Util.randRange(1,  3)) },
				LootEntry { ItemUtil.randomTippedArrow(Util.randRange(4, 16)) }
			),
			arrayOf(
				LootEntry { randTool(weapons, ToolInfo.NETHERITE, 0.25) },
				LootEntry { randTool(tools, ToolInfo.NETHERITE, 0.25) },
				LootEntry { randTool(armor, ToolInfo.NETHERITE, 0.25) },
				LootEntry { aTool(elytra, 0.25) },
				LootEntry { ItemStack(Material.NETHERITE_INGOT) },
				LootEntry { ItemStack(Material.BREWING_STAND) },
				LootEntry { ItemStack(Material.ANVIL) },
				LootEntry { ItemStack(Material.ENCHANTING_TABLE) },
				LootEntry { ItemStack(Material.ANCIENT_DEBRIS, Util.randRange(4, 8)) },
				LootEntry { ItemStack(Material.NETHERITE_SCRAP, Util.randRange(4, 8)) },
				LootEntry { ItemStack(Material.ENCHANTED_GOLDEN_APPLE) },
				LootEntry { ItemStack(Material.TOTEM_OF_UNDYING) }
			)
		)

		fun generateLoot(amounts: Array<Int>, inventory: Inventory) {
			amounts.forEachIndexed { tier, amount ->
				for (i in 0 until amount) {
					var space = Util.randRange(0, inventory.size - 1)

					while (inventory.getItem(space) != null) {
						++space
						space %= inventory.size
					}

					var loot = ItemUtil.randFromArray(lootEntries[tier])

					inventory.setItem(space, loot.makeStack())
				}
			}
		}

		fun findDropSpot(timeUntil: Int, buffer: Int, worldBorder: WorldBorder): Location {
			var currentRadius = worldBorder.size / 2

			var startRadius = GameRunner.uhc.preset.startRadius
			var endRadius = GameRunner.uhc.preset.endRadius

			/* distance over time */
			var speed = (startRadius - endRadius).toFloat() / (GameRunner.uhc.preset.shrinkTime).toFloat()

			var maxRadius = (currentRadius - (speed * timeUntil) - buffer).toInt()
			if (maxRadius < endRadius) maxRadius = endRadius.toInt()

			var world = Bukkit.getWorlds()[0]

			var x = Util.randRange(-maxRadius, maxRadius)
			var z = Util.randRange(-maxRadius, maxRadius)
			var y = Util.topBlockY(world, x, z) + 1

			return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
		}

		fun generateDrop(amounts: Array<Int>, location: Location) {
			var world = Bukkit.getWorlds()[0]

			var block = world.getBlockAt(location)
			block.type = Material.CHEST

			var chest = block.getState(false) as Chest
			chest.customName = "${ChatColor.GOLD}${ChatColor.BOLD}Care Package"

			generateLoot(amounts, chest.blockInventory)

			var firework = world.spawnEntity(location.add(0.5, 0.5, 0.5), EntityType.FIREWORK) as Firework

			/* add effect to the firework */
			var meta = firework.fireworkMeta
			meta.addEffect(ItemUtil.fireworkEffect(FireworkEffect.Type.BALL_LARGE))
			firework.fireworkMeta = meta

			firework.detonate()
		}
	}
}

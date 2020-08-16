import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.util.Util
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.ItemUtil.randomEnchantedBook
import com.codeland.uhc.util.Util.log
import com.codeland.uhc.util.Util.randFromArray
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.util.ToolTier
import com.codeland.uhc.util.ToolTier.ARMOR_SET
import com.codeland.uhc.util.ToolTier.BOW_SET
import com.codeland.uhc.util.ToolTier.DIAMOND
import com.codeland.uhc.util.ToolTier.IRON
import com.codeland.uhc.util.ToolTier.ONLY_TOOL_SET
import com.codeland.uhc.util.ToolTier.TIER_1
import com.codeland.uhc.util.ToolTier.TIER_2
import com.codeland.uhc.util.ToolTier.TIER_3
import com.codeland.uhc.util.ToolTier.TIER_TRIDENT
import com.codeland.uhc.util.ToolTier.TOOL_SET
import com.codeland.uhc.util.ToolTier.WEAPON_SET
import com.codeland.uhc.util.ToolTier.getTieredTool
import org.bukkit.ChatColor.*
import org.bukkit.*
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SuspiciousStewMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.*

class CarePackages {
	val OBJECTIVE_NAME = "carePackageDrop"

	var enabled = true
	set (value) {
		field = value

		if (value) onEnable() else onDisable()
	}

	fun onEnable() {
		var scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		scoreboard.getObjective(OBJECTIVE_NAME)?.unregister()

		objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy", "Next Care Package Drop")
		objective?.displaySlot = DisplaySlot.SIDEBAR

		Gui.updateCarePackages(this)
	}

	fun onDisable() {
		currentRunnable?.cancel()
		currentRunnable = null

		objective?.unregister()

		Gui.updateCarePackages(this)
	}

	fun onStart() {
		currentRunnable = generateRunnable()
		currentRunnable?.runTaskTimer(GameRunner.plugin, 0, 20)
		objective?.displaySlot = DisplaySlot.SIDEBAR
	}

	fun onEnd() {
		currentRunnable?.cancel()
		currentRunnable = null
		objective?.displaySlot = null
	}

	var objective = null as Objective?
	var currentRunnable = null as BukkitRunnable?

	var scoreTime = null as Score?
	var scorePosition = null as Score?

	val FAST_TIME = 5

	var fastMode = false
	private set

	/**
	 * @return if the setting was successful
	 *
	 * won't let you change when carepackages is
	 * already running
	 */
	fun setFastMode(value: Boolean): Boolean {
		if (currentRunnable == null) {
			fastMode = value
			Gui.updateCarePackages(this)

			return true
		}

		return false
	}

	fun generateRunnable(): BukkitRunnable {
		return object : BukkitRunnable() {
			val NUM_ITEMS = 18
			val NUM_DROPS = 6

			var running = false

			var timer = 0
			var previousLocation = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)
			var nextLocation = Location(Bukkit.getWorlds()[0], 0.0, 0.0, 0.0)

			lateinit var dropTimes: Array<Int>
			var dropIndex = 0

			fun getTier(): Int {
				return if (fastMode) {
					val phase = GameRunner.uhc.currentPhase
					if (phase == null)
						0
					else
						((1 - (phase.remainingSeconds.toDouble() / (phase.length + 1))) * NUM_DROPS).toInt()
				} else {
					dropIndex
				}
			}

			fun setScore(objective: Objective?, score: Score?, value: String, index: Int): Score? {
				/* trick kotlin in allowing us to return */
				if (objective == null) return null

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
				onEnd()
			}

			fun reset() {
				/* don't keep doing this outside of shrinking */
				if (!GameRunner.uhc.isPhase(PhaseType.SHRINK) || (!fastMode && dropIndex == dropTimes.size))
					return shutOff()

				timer = if (fastMode) FAST_TIME else dropTimes[dropIndex]

				nextLocation = findDropSpot(Bukkit.getWorlds()[0], previousLocation, timer, 16)
				previousLocation = nextLocation

				val color = dropTextColor(getTier())
				val coordinateString = "at ($color${BOLD}${nextLocation.blockX}${RESET}, $color${BOLD}${nextLocation.blockY}${RESET}, $color${BOLD}${nextLocation.blockZ}${RESET})"

				scoreTime = setScore(objective, scoreTime, "in $color${BOLD}${Util.timeString(timer)}", 1)
				scorePosition = setScore(objective, scorePosition, coordinateString, 0)
			}

			fun generateDropTimes(shrinkTime: Int): Array<Int> {
				val percentages = Array(NUM_DROPS) { 1.0f / NUM_DROPS }

				/* add randomness in the times */
				for (i in percentages.indices) {
					/* half a minute to a minute differences */
					var shiftAmount = Util.randRange(0.025f, 0.1f)

					var reduceIndex = Util.randRange(0, percentages.lastIndex)
					var gainIndex = Util.randRange(0, percentages.lastIndex)

					var reduced = percentages[reduceIndex]
					var gained = percentages[gainIndex]
					var valid = true

					var oldReduce = reduced
					reduced -= shiftAmount

					/* don't reduce any time to less than a minute */
					/* if it would, find the actual amount it got reduced by */
					if (reduced < 0.05f) {
						reduced = 0.05f
						shiftAmount = oldReduce - percentages[reduceIndex]

						if (shiftAmount < 0)
							valid = false
					}

					gained += shiftAmount

					if (valid) {
						percentages[reduceIndex] = reduced
						percentages[gainIndex] = gained
					}
				}

				return Array(NUM_DROPS) { i -> (percentages[i] * shrinkTime).toInt() }
			}

			override fun run() {
				if (running) {
					--timer
					if (timer == 0) {
						generateDrop(getTier(), NUM_ITEMS, nextLocation)

						++dropIndex

						reset()
					} else {
						scoreTime = setScore(objective, scoreTime, "in ${dropTextColor(getTier())}${BOLD}${Util.timeString(timer)}", 1)
					}

				/* start making drops during shrinking round */
				} else if (GameRunner.uhc.isPhase(PhaseType.SHRINK)) {
					running = true

					if (!fastMode) dropTimes = generateDropTimes(GameRunner.uhc.getTime(PhaseType.SHRINK))

					reset()
				}
			}
		}
	}

	companion object {
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
			Material.FERMENTED_SPIDER_EYE
		)

		fun brewingIngredient(): ItemStack {
			return ItemStack(randFromArray(brewingIngredients), Util.randRange(4, 16))
		}

		fun anyThrow(): Material {
			val rand = Math.random()

			return if (rand < 0.75)
				Material.SPLASH_POTION
			else
				Material.LINGERING_POTION
		}

		fun splashLinger(): Material {
			return if (Math.random() < 0.5)
				Material.SPLASH_POTION
			else
				Material.LINGERING_POTION
		}

		class LootEntry(var makeStack: () -> ItemStack)

		const val ENCHANT_CHANCE = 0.25

		val guaranteedEntries = arrayOf(
			arrayOf(
				LootEntry { getTieredTool(ARMOR_SET, IRON, TIER_1, 2, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(TOOL_SET, IRON, TIER_1, 2, ENCHANT_CHANCE) },

				LootEntry { randomEnchantedBook() },
				LootEntry { ItemStack(Material.WATER_BUCKET) },
				LootEntry { ItemStack(Material.LAVA_BUCKET) }
			),
			arrayOf(
				LootEntry { getTieredTool(ARMOR_SET, IRON, TIER_1, 2, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(TOOL_SET, IRON, TIER_1, 2, ENCHANT_CHANCE) },

				LootEntry { randomEnchantedBook() },

				/* bucket section */
				LootEntry { ItemStack(Material.WATER_BUCKET) },
				LootEntry { ItemStack(Material.LAVA_BUCKET) },

				LootEntry { ItemStack(Material.EXPERIENCE_BOTTLE, Util.randRange(4, 8)) }
			),
			arrayOf(
				LootEntry { getTieredTool(ARMOR_SET, IRON, TIER_2, 2, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(TOOL_SET, IRON, TIER_2, 2, ENCHANT_CHANCE) },

				LootEntry { randomEnchantedBook() },

				/* bucket section */
				LootEntry { ItemStack(Material.WATER_BUCKET) },
				LootEntry { ItemStack(Material.LAVA_BUCKET) },

				/* crossbow section */
				LootEntry { getTieredTool(BOW_SET, TIER_1, 1, ENCHANT_CHANCE) },
				LootEntry { ItemUtil.randomFireworkStar(Util.randRange(8, 16)) },
				LootEntry { ItemUtil.randomRocket(Util.randRange(8, 16)) },

				LootEntry { ItemStack(Material.EXPERIENCE_BOTTLE, Util.randRange(4, 8)) }
			),
			arrayOf(
				LootEntry { getTieredTool(WEAPON_SET, IRON, TIER_2, 1, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(ARMOR_SET, IRON, TIER_2, 1, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(ONLY_TOOL_SET, DIAMOND, TIER_3, 1, ENCHANT_CHANCE) },

				LootEntry { randomEnchantedBook() },

				/* crossbow section */
				LootEntry { getTieredTool(BOW_SET, TIER_1, 1, ENCHANT_CHANCE) },
				LootEntry { ItemUtil.randomFireworkStar(Util.randRange(8, 16)) },
				LootEntry { ItemUtil.randomRocket(Util.randRange(8, 16)) },

				LootEntry { ItemStack(Material.EXPERIENCE_BOTTLE, Util.randRange(4, 8)) },

				/* brewing section */
				LootEntry { ItemStack(Material.BLAZE_ROD, Util.randRange(2, 3)) },
				LootEntry { ItemStack(Material.NETHER_WART, Util.randRange(4,  7)) },
				LootEntry { ItemStack(Material.GLASS_BOTTLE, Util.randRange(2,  4)) },
				LootEntry { brewingIngredient() }
			),
			arrayOf(
				LootEntry { getTieredTool(WEAPON_SET, DIAMOND, TIER_2, 1, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(ARMOR_SET, DIAMOND, TIER_2, 1, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(BOW_SET, TIER_2, 1, ENCHANT_CHANCE) },

				LootEntry { getTieredTool(TIER_TRIDENT, 1, ENCHANT_CHANCE) },

				/* brewing section */
				LootEntry { ItemStack(Material.BLAZE_ROD, Util.randRange(2, 3)) },
				LootEntry { ItemStack(Material.NETHER_WART, Util.randRange(4,  7)) },
				LootEntry { ItemStack(Material.GLASS_BOTTLE, Util.randRange(2,  4)) },
				LootEntry { brewingIngredient() },
				LootEntry { ItemUtil.randomPotion(true, Material.POTION) }
			),
			arrayOf(
				LootEntry { getTieredTool(WEAPON_SET, DIAMOND, TIER_3, 1, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(ARMOR_SET, DIAMOND, TIER_3, 1, ENCHANT_CHANCE) },
				LootEntry { getTieredTool(BOW_SET, TIER_3, 1, ENCHANT_CHANCE) },

				LootEntry { getTieredTool(ToolTier.TIER_ELYTRA, 1, ENCHANT_CHANCE) },
				LootEntry { ItemUtil.randomShulker(1) },

				LootEntry { ItemStack(Material.BLAZE_ROD, Util.randRange(2, 3)) },
				LootEntry { ItemStack(Material.NETHER_WART, Util.randRange(4,  7)) },
				LootEntry { ItemStack(Material.DRAGON_BREATH, Util.randRange(2, 5)) },
				LootEntry { brewingIngredient() },
				LootEntry { ItemUtil.randomPotion(false, anyThrow()) },

				LootEntry { ItemStack(Material.SPECTRAL_ARROW, Util.randRange(4, 16)) }
			)
		)

		val materialTiers = arrayOf(
			// basic materials
			arrayOf(
				LootEntry { ItemStack(Material.COOKED_BEEF, Util.randRange(2,  5)) },
				LootEntry { ItemStack(Material.GUNPOWDER, Util.randRange(5, 12)) },
				LootEntry { ItemStack(Material.FEATHER, Util.randRange(6, 18)) },
				LootEntry { ItemStack(Material.SUGAR_CANE, Util.randRange(6, 18)) },
				LootEntry { ItemStack(Material.RED_MUSHROOM, Util.randRange(6, 16)) },
				LootEntry { ItemStack(Material.BROWN_MUSHROOM, Util.randRange(6, 16)) },
				LootEntry { ItemStack(Material.OXEYE_DAISY, Util.randRange(6, 16)) },
				LootEntry { ItemStack(Material.LEATHER, Util.randRange(3,  8)) },
				LootEntry { ItemStack(Material.CARROT, Util.randRange(8,  17)) },
				LootEntry { ItemStack(Material.COAL, Util.randRange(8,  17)) }
			),
			// medium materials
			arrayOf(
				LootEntry { ItemStack(Material.IRON_INGOT, Util.randRange(8,  16)) },
				LootEntry { ItemStack(Material.GOLD_INGOT, Util.randRange(8,  16)) },
				LootEntry { ItemStack(Material.BOOK, Util.randRange(4,  9)) },
				LootEntry { ItemStack(Material.ARROW, Util.randRange(8, 32)) },
				LootEntry { ItemStack(Material.OBSIDIAN, Util.randRange(3,  7)) },
				LootEntry { ItemStack(Material.LAPIS_LAZULI, Util.randRange(8, 16)) },
				LootEntry { ItemStack(Material.APPLE, Util.randRange(2, 6)) },
				LootEntry { ItemStack(Material.STRING, Util.randRange(3, 10)) },
				LootEntry { ItemStack(Material.STONE, Util.randRange(32, 64)) }
			),
			// advanced materials
			arrayOf(
				LootEntry { ItemStack(Material.DIAMOND, Util.randRange(3,  8)) },
				LootEntry { ItemStack(Material.IRON_BLOCK, Util.randRange(2,  4)) },
				LootEntry { ItemStack(Material.GOLD_BLOCK, Util.randRange(2,  4)) },
				LootEntry { ItemStack(Material.ANCIENT_DEBRIS, Util.randRange(4,  9)) },
				LootEntry { ItemStack(Material.TNT, Util.randRange(2,  9)) },
				LootEntry { ItemStack(Material.ENDER_PEARL, Util.randRange(2,  7)) }
			)
		)

		fun generateLoot(tier: Int, amount: Int, inventory: Inventory) {
			val addItem = { item: ItemStack ->
				var space = Util.randRange(0, inventory.size - 1)

				while (inventory.getItem(space) != null) {
					++space
					space %= inventory.size
				}

				inventory.setItem(space, item)
			}

			val guaranteedArray = guaranteedEntries[tier]

			guaranteedArray.forEach { entry ->
				addItem(entry.makeStack())
			}

			for (i in guaranteedArray.size until amount) {
				/* any material from base until max this tier allows */
				val materialArray = materialTiers[Util.randRange(0, tier / 2)]

				addItem(Util.randFromArray(materialArray).makeStack())
			}
		}

		class XZReturn(val x: Int, val z: Int)

		/**
		 * unit testable
		 *
		 * does not interact with bukkit
		 * @return where a care package should land in terms of x and z
		 */
		fun findDropXZ(lastX: Int, lastZ: Int, startRadius: Double, endRadius: Double, remainingSeconds: Int, timeUntil: Int, phaseLength: Int, buffer: Int): XZReturn {
			class RangeReference(var value: Double = 0.0) {
				fun intValue(): Int {
					return value.toInt()
				}
			}

			var speed = (startRadius - endRadius) / phaseLength.toDouble()
			var invAlong = (remainingSeconds - timeUntil) / phaseLength.toDouble()

			var finalRadius = ((startRadius - endRadius) * invAlong + endRadius) - buffer
			if (finalRadius < endRadius) finalRadius = endRadius

			/* the next care package can spawn in one of 8 squares */
			/* around a square of the previous drop */

			/* blockCoordinate can be the X or Z of the block */
			val choosePlaceIndex = { blockCoordinate: Int, lower: RangeReference, upper: RangeReference ->
				lower.value = blockCoordinate - (finalRadius / 2)
				upper.value = blockCoordinate + (finalRadius / 2)

				val allows = arrayOf((lower.value >= -finalRadius), (upper.value <= finalRadius))
				var placeIndex = Util.randRange(0, 1)
				if (!allows[placeIndex]) placeIndex = placeIndex.xor(1)

				placeIndex
			}

			val lower = RangeReference()
			val upper = RangeReference()

			/* use ints for the block position based off the values we got */
			val intRadius = finalRadius.toInt()

			/* random decide whether the chest will spawn guaranteed */
			/* to the left or right of the last chest */
			/* r to the up or down of the last chest */
			return if (Math.random() < 0.5) {
				if (choosePlaceIndex(lastX, lower, upper) == 0)
					XZReturn(Util.randRange(-intRadius, lower.intValue()), Util.randRange(-intRadius, intRadius))
				else
					XZReturn(Util.randRange(upper.intValue(), intRadius), Util.randRange(-intRadius, intRadius))
			} else {
				if (choosePlaceIndex(lastZ, lower, upper) == 0)
					XZReturn(Util.randRange(-intRadius, intRadius), Util.randRange(-intRadius, lower.intValue()))
				else
					XZReturn(Util.randRange(-intRadius, intRadius), Util.randRange(upper.intValue(), intRadius))
			}
		}

		fun testDropXZ() {
			var xz = findDropXZ(0, 0, 550.0, 25.0, 120, 60, 2700, 0)
			log("x: ${xz.x}, z: ${xz.z}")

			xz = findDropXZ(0, 0, 550.0, 25.0, 120, 60, 2700, 0)
			log("x: ${xz.x}, z: ${xz.z}")

			xz = findDropXZ(0, 0, 550.0, 25.0, 120, 60, 2700, 0)
			log("x: ${xz.x}, z: ${xz.z}")

			xz = findDropXZ(0, 0, 550.0, 25.0, 120, 60, 2700, 0)
			log("x: ${xz.x}, z: ${xz.z}")

			xz = findDropXZ(0, 0, 550.0, 25.0, 60, 900, 2700, 0)
			log("x: ${xz.x}, z: ${xz.z}")
		}

		fun findDropSpot(world: World, lastLocation: Location, timeUntil: Int, buffer: Int): Location {
			/* helper classes and functions */
			val makeLocation = { x: Int, z: Int ->
				val y = Util.topBlockY(world, x, z) + 1
				Location(world, x.toDouble(), y.toDouble(), z.toDouble())
			}

			val uhc = GameRunner.uhc
			val phaseTime = uhc.getTime(PhaseType.SHRINK)
			val remainingSeconds = uhc.currentPhase?.remainingSeconds ?: return Location(world, 0.0, 0.0, 0.0)

			val xz = findDropXZ(lastLocation.blockX, lastLocation.blockZ, uhc.startRadius, uhc.endRadius, remainingSeconds, timeUntil, phaseTime, buffer)

			return makeLocation(xz.x, xz.z)
		}

		fun dropTextColor(tier: Int): ChatColor {
			return when (tier / 2) {
				0 -> GOLD
				1 -> BLUE
				2 -> LIGHT_PURPLE
				else -> GRAY
			}
		}

		fun generateDrop(tier: Int, amount: Int, location: Location) {
			var world = Bukkit.getWorlds()[0]

			var block = world.getBlockAt(location)
			block.breakNaturally()
			block.type = Material.CHEST

			var chest = block.getState(false) as Chest
			chest.customName = "${dropTextColor(tier)}${BOLD}Care Package"

			generateLoot(tier, amount, chest.blockInventory)

			var firework = world.spawnEntity(location.add(0.5, 0.5, 0.5), EntityType.FIREWORK) as Firework

			/* add effect to the firework */
			var meta = firework.fireworkMeta
			meta.addEffect(ItemUtil.fireworkEffect(FireworkEffect.Type.BALL_LARGE))
			firework.fireworkMeta = meta

			firework.detonate()

			/* announce in chat so positions are saved */
			Bukkit.getOnlinePlayers().forEach { player ->
				player.sendMessage("${dropTextColor(tier)}${BOLD}Care Package Dropped at (${location.blockX}, ${location.blockY}, ${location.blockZ})")
			}
		}
	}
}

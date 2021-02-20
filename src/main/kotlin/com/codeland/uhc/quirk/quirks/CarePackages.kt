package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.blockfix.LeavesFix
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util.log
import com.codeland.uhc.util.Util.randFromArray
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.CarePackageUtil.pickOne
import com.codeland.uhc.util.ScoreboardDisplay
import org.bukkit.ChatColor.*
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.*

class CarePackages(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	val NUM_DROPS = 3

	var taskID = -1

	var scoreboardDisplay: ScoreboardDisplay? = null

	var running = false

	var timer = 0

	lateinit var dropLocations: Array<Location>
	lateinit var dropTimes: Array<Int>

	var dropIndex = 0

	/*
	 * example scoreboard:
	 * -------------------------
	 * Care Packages
	 *
	 * Drop 1:
	 * (45, 70, -329)
	 * Dropped
	 *
	 * Drop 2:
	 * (102, 64, 92)
	 * 2 minutes 1 second
	 *
	 * Drop 3:
	 * (-78, 103, -28)
	 * Awaiting
	 */

	override fun onEnable() {
		if (uhc.currentPhase?.phaseType == PhaseType.GRACE || uhc.currentPhase?.phaseType == PhaseType.SHRINK) onStart()
	}

	override fun onDisable() {
		onEnd()
	}

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) onStart()
		else if (phase.type == PhaseType.WAITING) onEnd()
	}

	private fun onStart() {
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, ::perSecond, 20, 20)

		scoreboardDisplay = ScoreboardDisplay("Care Packages", NUM_DROPS * 4)
		scoreboardDisplay?.show()

		if (generateDrops()) prepareDrop(0) else onEnd()
	}

	private fun onEnd() {
		Bukkit.getScheduler().cancelTask(taskID)
		taskID = -1

		running = false

		scoreboardDisplay?.destroy()
		scoreboardDisplay = null
	}

	private fun generateDrops(): Boolean {
		fun findLocations(findCurrentRadius: (Int) -> Double) {
			val initialAngle = Math.random() * PI * 2
			val angleAdance = PI * 2 / NUM_DROPS

			dropLocations = Array(NUM_DROPS) { i ->
				val currentRadius = findCurrentRadius(i)

				val x = cos(initialAngle + angleAdance * i) * currentRadius / 2
				val z = sin(initialAngle + angleAdance * i) * currentRadius / 2

				Location(uhc.getDefaultWorld(), x, 0.0, z)
			}
		}

		/* find drop times */
		if (uhc.isPhase(PhaseType.GRACE)) {
			val remaining = uhc.currentPhase?.remainingSeconds ?: 0

			/* distribute drops over shrinking phase so that if there were another, */
			/* it would fall exactly at the end of shrinking phase */

			val dropPeriod = uhc.getTime(PhaseType.SHRINK) * (NUM_DROPS / (NUM_DROPS + 1.0))
			val dropInterval = (dropPeriod / NUM_DROPS).toInt()

			/* all drops are equally spaced by dropInterval */
			dropTimes = Array(NUM_DROPS) { dropInterval }

			/* the first drop has to wait for the end of grace period */
			dropTimes[0] += remaining

			findLocations { i ->
				uhc.startRadius * (1 - ((dropInterval * i).toDouble() / uhc.getTime(PhaseType.SHRINK)))
			}

		} else if (uhc.isPhase(PhaseType.SHRINK)) {
			val elapsed = uhc.getTime(PhaseType.SHRINK) - (uhc.currentPhase?.remainingSeconds ?: 0)

			val cutOff = uhc.getTime(PhaseType.SHRINK) * (NUM_DROPS / (NUM_DROPS + 1.0)).toInt()

			val available = (cutOff - elapsed)
			val dropInterval = (available / NUM_DROPS)

			/* there must be at least 1 second left for each care package to drop */
			if (available <= NUM_DROPS) dropTimes = Array(NUM_DROPS) { available / NUM_DROPS }
			else return false

			findLocations { i ->
				uhc.startRadius * (1 - ((elapsed + dropInterval * i).toDouble() / uhc.getTime(PhaseType.SHRINK)))
			}
		}

		return true
	}

	private fun prepareDrop(index: Int) {
		dropIndex = index
		timer = if (dropIndex < NUM_DROPS) dropTimes[dropIndex] else 0
	}

	private fun updateScoreboard() {
		val scoreboard = scoreboardDisplay ?: return

		for (i in 0 until NUM_DROPS) {
			val location = dropLocations[i]
			val color = if (i == dropIndex) "${dropTextColor(i)}${BOLD}" else "${WHITE}"

			scoreboard.setLine(i * 4 + 1, "${color}Drop ${i + 1}")
			scoreboard.setLine(i * 4 + 2, "${color}(${location.blockX}, ${location.blockZ})")
			scoreboard.setLine(i * 4 + 3, if (i < dropIndex)
				"${color}Dropped"
			else if(i == dropIndex)
				"${color}${Util.timeString(timer)}"
			else
				"${color}Awaiting"
			)
		}
	}

	private fun perSecond() {
		if (dropIndex < NUM_DROPS) {
			--timer

			if (timer == 0) {
				generateDrop(dropIndex, dropLocations[dropIndex])
				prepareDrop(dropIndex + 1)
			}

			updateScoreboard()
		}
	}

	fun forceDrop(): Boolean {
		return if (dropIndex < NUM_DROPS) {
			generateDrop(dropIndex, dropLocations[dropIndex])
			prepareDrop(dropIndex + 1)

			updateScoreboard()

			true

		} else {
			false
		}
	}

	companion object {
		class LootEntry(var makeStack: () -> ItemStack)

		private const val ENCHANT_CHANCE = 0.25

		val chestEntries = arrayOf(
			arrayOf(
				LootEntry { ItemStack(WATER_BUCKET) },
				LootEntry { ItemStack(LAVA_BUCKET) },
				LootEntry { CarePackageUtil.randomBucket() },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomItem(BROWN_MUSHROOM, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(BROWN_MUSHROOM, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(RED_MUSHROOM, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(RED_MUSHROOM, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(OXEYE_DAISY, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(OXEYE_DAISY, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(LEATHER, 4, 8) },
				LootEntry { CarePackageUtil.randomItem(LEATHER, 4, 8) },
				LootEntry { CarePackageUtil.randomItem(PAPER, 6, 9) },
				LootEntry { CarePackageUtil.randomItem(PAPER, 6, 9) },
				LootEntry { CarePackageUtil.randomItem(BOOK, 1, 2) },
				LootEntry { CarePackageUtil.randomItem(BOOKSHELF, 1) },
				LootEntry { CarePackageUtil.randomItem(MELON_SLICE, 2) },
				LootEntry { CarePackageUtil.randomItem(MELON_SLICE, 2) },
				LootEntry { CarePackageUtil.regenerationStew() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.flamingLazerSword() },
				LootEntry { ItemStack(NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(GUNPOWDER, pickOne(2, 4)) },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { ItemStack(STONE, pickOne(16, 32, 48)) },
				LootEntry { ItemStack(STONE, pickOne(16, 32, 48)) },
				LootEntry { ItemStack(TORCH, pickOne(16, 32)) },
				LootEntry { ItemStack(TORCH, pickOne(16, 32)) },
				LootEntry { ItemStack(OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { CarePackageUtil.randomBoat() },
				LootEntry { CarePackageUtil.randomItem(FLINT, 5, 10) },
				LootEntry { CarePackageUtil.randomItem(FLINT, 5, 10) },
				LootEntry { CarePackageUtil.randomItem(FEATHER, 4, 9) },
				LootEntry { CarePackageUtil.randomItem(FEATHER, 4, 9) },
				LootEntry { CarePackageUtil.randomItem(ARROW, 10, 15) },
				LootEntry { CarePackageUtil.randomItem(ARROW, 10, 15) },
				LootEntry { CarePackageUtil.randomItem(COOKED_PORKCHOP, 8, 12) },
				LootEntry { CarePackageUtil.randomItem(COOKED_BEEF, 8, 12) },
				LootEntry { CarePackageUtil.powerBow(1) },
				LootEntry { CarePackageUtil.piercingCrossbow() },
				LootEntry { CarePackageUtil.randomItem(APPLE, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(APPLE, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(STRING, 2, 3) },
				LootEntry { CarePackageUtil.randomItem(STRING, 2, 3) },
				LootEntry { CarePackageUtil.randomAxe(false, false) },
				LootEntry { CarePackageUtil.randomSword(false, false) },
				LootEntry { CarePackageUtil.randomPick(false, false) },
			),
			arrayOf(
				LootEntry { ItemStack(WATER_BUCKET) },
				LootEntry { ItemStack(LAVA_BUCKET) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomItem(BROWN_MUSHROOM, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(RED_MUSHROOM, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(OXEYE_DAISY, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(LEATHER, 4, 8) },
				LootEntry { CarePackageUtil.randomItem(PAPER, 6, 9) },
				LootEntry { CarePackageUtil.randomItem(BOOK, 1, 2) },
				LootEntry { CarePackageUtil.randomItem(BOOKSHELF, 2) },
				LootEntry { CarePackageUtil.randomItem(MELON_SLICE, 2) },
				LootEntry { CarePackageUtil.regenerationStew() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomItem(LEATHER, 4, 8) },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(true) },
				LootEntry { CarePackageUtil.superSwaggyPants() },
				LootEntry { ItemStack(NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(BLAZE_ROD, 2) },
				LootEntry { ItemStack(BLAZE_ROD, 2) },
				LootEntry { ItemStack(REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(GUNPOWDER, pickOne(2, 4)) },
				LootEntry { ItemStack(GUNPOWDER, pickOne(2, 4)) },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { ItemStack(OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { CarePackageUtil.randomItem(FLINT, 5, 10) },
				LootEntry { CarePackageUtil.randomItem(FLINT, 5, 10) },
				LootEntry { CarePackageUtil.randomItem(FEATHER, 4, 9) },
				LootEntry { CarePackageUtil.randomItem(FEATHER, 4, 9) },
				LootEntry { CarePackageUtil.randomItem(ARROW, 10, 15) },
				LootEntry { CarePackageUtil.randomItem(ARROW, 10, 15) },
				LootEntry { CarePackageUtil.randomItem(ENDER_PEARL, 4, 6) },
				LootEntry { CarePackageUtil.randomItem(SPECTRAL_ARROW, 10, 15) },
				LootEntry { CarePackageUtil.powerBow(1) },
				LootEntry { CarePackageUtil.powerBow(1) },
				LootEntry { CarePackageUtil.piercingCrossbow() },
				LootEntry { CarePackageUtil.piercingCrossbow() },
				LootEntry { CarePackageUtil.randomItem(APPLE, 2, 4) },
				LootEntry { CarePackageUtil.randomItem(GOLDEN_APPLE, 1, 2) },
				LootEntry { CarePackageUtil.randomItem(GOLDEN_APPLE, 1, 2) },
				LootEntry { CarePackageUtil.randomItem(STRING, 2, 3) },
				LootEntry { CarePackageUtil.randomAxe(false, true) },
				LootEntry { CarePackageUtil.randomAxe(true, false) },
				LootEntry { CarePackageUtil.randomSword(false, true) },
				LootEntry { CarePackageUtil.randomSword(true, false) },
				LootEntry { CarePackageUtil.randomPick(false, true) },
				LootEntry { CarePackageUtil.randomPick(true, false) },
				LootEntry { CarePackageUtil.randomPotion() },
				LootEntry { CarePackageUtil.randomPotion() },
			),
			arrayOf(
				LootEntry { ItemStack(LAVA_BUCKET) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomItem(BOOK, 4) },
				LootEntry { CarePackageUtil.randomItem(BOOKSHELF, 2) },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomEnchantedBook(true) },
				LootEntry { CarePackageUtil.randomEnchantedBook(true) },
				LootEntry { CarePackageUtil.randomEnchantedBook(true) },
				LootEntry { ItemStack(NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(BLAZE_ROD, 2) },
				LootEntry { ItemStack(BLAZE_ROD, 2) },
				LootEntry { ItemStack(BLAZE_ROD, 2) },
				LootEntry { ItemStack(REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(GUNPOWDER, pickOne(2, 4)) },
				LootEntry { ItemStack(GUNPOWDER, pickOne(2, 4)) },
				LootEntry { ItemStack(GUNPOWDER, pickOne(2, 4)) },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { ItemStack(OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { CarePackageUtil.randomItem(FLINT, 5, 10) },
				LootEntry { CarePackageUtil.randomItem(FEATHER, 4, 9) },
				LootEntry { CarePackageUtil.randomItem(ARROW, 10, 15) },
				LootEntry { CarePackageUtil.randomItem(ENDER_PEARL, 4, 6) },
				LootEntry { CarePackageUtil.randomItem(ENDER_PEARL, 4, 6) },
				LootEntry { CarePackageUtil.randomItem(SPECTRAL_ARROW, 10, 15) },
				LootEntry { CarePackageUtil.randomItem(SPECTRAL_ARROW, 10, 15) },
				LootEntry { CarePackageUtil.powerBow(1) },
				LootEntry { CarePackageUtil.powerBow(2) },
				LootEntry { CarePackageUtil.piercingCrossbow() },
				LootEntry { CarePackageUtil.piercingCrossbow() },
				LootEntry { CarePackageUtil.randomItem(GOLDEN_APPLE, 1, 2) },
				LootEntry { CarePackageUtil.randomItem(GOLDEN_APPLE, 1, 2) },
				LootEntry { CarePackageUtil.randomAxe(true, true) },
				LootEntry { CarePackageUtil.randomAxe(true, false) },
				LootEntry { CarePackageUtil.randomSword(true, true) },
				LootEntry { CarePackageUtil.randomSword(true, false) },
				LootEntry { CarePackageUtil.randomPick(true, true) },
				LootEntry { CarePackageUtil.randomPick(true, false) },
				LootEntry { CarePackageUtil.randomPotion() },
				LootEntry { CarePackageUtil.randomPotion() },
				LootEntry { CarePackageUtil.randomPotion() },
			)
		)

		data class SpireData(val ore: Material, val block: Material)

		val SPIRE_COAL = SpireData(COAL_ORE, COAL_BLOCK)
		val SPIRE_IRON = SpireData(IRON_ORE, IRON_BLOCK)
		val SPIRE_LAPIS = SpireData(LAPIS_ORE, LAPIS_BLOCK)
		val SPIRE_GOLD = SpireData(GOLD_ORE, GOLD_BLOCK)
		val SPIRE_DIAMOND = SpireData(DIAMOND_ORE, DIAMOND_ORE)

		val spireAmounts = arrayOf(
			arrayOf(SPIRE_COAL, SPIRE_COAL, SPIRE_IRON, SPIRE_IRON),
			arrayOf(SPIRE_IRON, SPIRE_IRON, SPIRE_GOLD, SPIRE_LAPIS),
			arrayOf(SPIRE_IRON, SPIRE_GOLD, SPIRE_LAPIS, SPIRE_DIAMOND)
		)

		fun generateLoot(tier: Int, inventories: ArrayList<Inventory>) {
			val usingEntries = chestEntries[tier].toMutableList() as ArrayList<LootEntry>
			usingEntries.shuffle()

			val perChest = usingEntries.size / inventories.size

			for (i in usingEntries.indices) {
				val inventoryIndex = (i / perChest).coerceAtMost(inventories.lastIndex)

				ItemUtil.randomAddInventory(inventories[inventoryIndex], usingEntries[i].makeStack())
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

			val xz = findDropXZ(lastLocation.blockX, lastLocation.blockZ, uhc.startRadius.toDouble(), uhc.endRadius.toDouble(), remainingSeconds, timeUntil, phaseTime, buffer)

			return makeLocation(xz.x, xz.z)
		}

		fun dropTextColor(tier: Int): ChatColor {
			return when (tier) {
				0 -> GOLD
				1 -> BLUE
				2 -> LIGHT_PURPLE
				else -> GRAY
			}
		}

		fun generateSpire(world: World, x: Int, z: Int, maxRadius: Float, height: Int, spireData: SpireData) {
			var baseY = 63

			for (y in 255 downTo 0) {
				val block = world.getBlockAt(x, y, z)

				if (!block.isPassable && !LeavesFix.isLeaves(block.type)) {
					baseY = y
					break
				}
			}

			val magnitudeField = Array(9) { (Math.random() * 0.2 + 0.9).toFloat() }

			fun fillBlock(block: Block) {
				val random = Math.random()

				block.setType(when {
					random < 1 / 16.0 -> spireData.ore
					random < 1 / 5.0 -> ANDESITE
					else -> STONE
				}, false)
			}

			fun isSpireBlock(block: Block): Boolean {
				return block.type == STONE || block.type == ANDESITE || block.type == spireData.ore
			}

			fun fillCircle(radius: Float, y: Int, magnitudeHeight: Float, allowHangers: Boolean, onBlock: (Block) -> Unit) {
				val intRadius = ceil(radius).toInt()
				val boundingSize = intRadius * 2 + 1

				for (i in 0 until boundingSize * boundingSize) {
					val offX = (i % boundingSize) - intRadius
					val offZ = ((i / boundingSize) % boundingSize) - intRadius

					val angle = (atan2(offZ.toDouble(), offX.toDouble()) + PI).toFloat()

					val blockRadius = radius * Util.bilinearWrap(magnitudeField, 3, 3, angle / (PI.toFloat() * 2.0f), magnitudeHeight)

					if (sqrt(offX.toDouble().pow(2) + offZ.toDouble().pow(2)) < blockRadius) {
						val block = world.getBlockAt(x + offX, y, z + offZ)

						if (allowHangers || isSpireBlock(block.getRelative(BlockFace.DOWN))) onBlock(block)
					}
				}
			}

			for (y in baseY - 1 downTo 0) {
				var allFilled = true

				fillCircle(maxRadius, y, 0.0f, true) { block ->
					if (block.isPassable) allFilled = false
					fillBlock(block)
				}

				if (allFilled) break
			}

			for (y in 0 until height) {
				val along = y / (height - 1).toFloat()
				val usingRadius = Util.interp(1.0f, maxRadius, 1 - along)

				fillCircle(usingRadius, baseY + y, along, y == 0) { block ->
					fillBlock(block)
				}
			}

			world.getBlockAt(x, baseY + height, z).setType(spireData.block, false)
		}

		fun circlePlacement(centerX: Int, centerZ: Int, minRadius: Float, maxRadius: Float, numPlaces: Int, onPlace: (Int, Int, Int) -> Unit) {
			val initialAngle = Math.random() * PI * 2
			val angleIncrement = PI * 2 / numPlaces
			val angleDeviance = angleIncrement / 4

			for (i in 0 until numPlaces) {
				val angle = initialAngle + i * angleIncrement + (Math.random() * angleDeviance * 2) - angleDeviance
				val radius = (Math.random() * (maxRadius - minRadius)) + minRadius

				val x = (centerX + cos(angle) * radius).toInt()
				val z = (centerZ + sin(angle) * radius).toInt()

				onPlace(i, x, z)
			}
		}

		fun spirePlacement(world: World, centerX: Int, centerZ: Int, placeRadius: Int, spireList: Array<SpireData>) {
			circlePlacement(centerX, centerZ, placeRadius * 0.5f, placeRadius.toFloat(), spireList.size) { i, x, z ->
				generateSpire(world, x, z, 5f, 14, spireList[i])
			}
		}

		fun generateChest(world: World, x: Int, z: Int, tier: Int): Inventory {
			val chunk = world.getChunkAt(world.getBlockAt(x, 0, z))
			var y = 0

			for (dy in 255 downTo 0) {
				if (!chunk.getBlock(Util.mod(x, 16), dy, Util.mod(z, 16)).isPassable) {
					y = dy + 1
					break
				}
			}

			var block = world.getBlockAt(x, y, z)
			block.breakNaturally()
			block.type = CHEST

			var chest = block.getState(false) as Chest
			chest.customName = "${dropTextColor(tier)}${BOLD}Care Package"

			var firework = world.spawnEntity(Location(world, x + 0.5, y + 1.0, z + 0.5), EntityType.FIREWORK) as Firework

			/* add effect to the firework */
			var meta = firework.fireworkMeta
			meta.addEffect(ItemUtil.fireworkEffect(FireworkEffect.Type.BALL_LARGE, 3))
			meta.power = 2
			firework.fireworkMeta = meta

			return chest.blockInventory
		}

		fun chestPlacement(world: World, centerX: Int, centerZ: Int, placeRadius: Int, ringChests: Int, tier: Int): ArrayList<Inventory> {
			val inventoryList = ArrayList<Inventory>()

			circlePlacement(centerX, centerZ, placeRadius * 0.4f, placeRadius * 0.6f, ringChests) { _, x, z ->
				inventoryList.add(generateChest(world, x, z, tier))
			}

			inventoryList.add(generateChest(world, centerX, centerZ, tier))

			return inventoryList
		}

		fun placeSugarcane(world: World, x: Int, z: Int, height: Int): Boolean {
			fun placable(block: Block): Boolean {
				return block.type == GRASS_BLOCK ||
					block.type == STONE ||
					block.type == DIRT ||
					block.type == SAND ||
					block.type == RED_SAND ||
					block.type == PODZOL
			}

			val directions = arrayOf(BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH)

			for (y in 255 downTo 0) {
				val block = world.getBlockAt(x, y, z)

				if (block.type == SUGAR_CANE || block.type == WATER || block.type == LAVA) return false

				if (!block.isPassable) {
					if (placable(block)) {
						return if (
							!block.getRelative(BlockFace.WEST).isPassable &&
							!block.getRelative(BlockFace.EAST).isPassable &&
							!block.getRelative(BlockFace.NORTH).isPassable &&
							!block.getRelative(BlockFace.SOUTH).isPassable
						) {
							val placeDirection = randFromArray(directions)

							var baseBlock = block.getRelative(placeDirection)

							block.setType(WATER, false)
							baseBlock.setType(SAND, false)

							for (i in 0 until height) {
								baseBlock = baseBlock.getRelative(BlockFace.UP)
								baseBlock.setType(SUGAR_CANE, false)
							}

							true

						} else {
							false
						}
					} else {
						return false
					}
				}
			}

			return false
		}

		fun sugarcanePlacement(world: World, centerX: Int, centerZ: Int, placeRadius: Int, numSugarcane: Int) {
			for (i in 0 until numSugarcane) {
				val height = Util.randRange(1, 4)

				for (attempt in 0 until 10) {
					val x = Util.randRange(centerX - placeRadius, centerX + placeRadius)
					val z = Util.randRange(centerZ - placeRadius, centerZ + placeRadius)

					if (placeSugarcane(world, x, z, height)) break
				}
			}
		}

		fun generateDrop(tier: Int, location: Location) {
			var world = location.world

			val radius = 30

			val x = location.blockX
			val z = location.blockZ

			spirePlacement(world, x, z, radius, spireAmounts[tier])
			sugarcanePlacement(world, x, z, radius, 9)
			generateLoot(tier, chestPlacement(world, x, z, radius, 5, tier))

			Bukkit.getOnlinePlayers().forEach { player ->
				player.sendMessage("${dropTextColor(tier)}${BOLD}Drop at (${x}, ${z})")
			}
		}
	}
}

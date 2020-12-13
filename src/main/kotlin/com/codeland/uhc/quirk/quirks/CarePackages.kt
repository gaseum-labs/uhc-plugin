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
import com.codeland.uhc.gui.item.GuiOpener
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.CarePackageUtil.pickOne
import com.codeland.uhc.util.ItemUtil.randomDye
import com.codeland.uhc.util.ItemUtil.randomDyeArmor
import com.codeland.uhc.util.ItemUtil.randomMusicDisc
import com.codeland.uhc.util.ItemUtil.randomTippedArrow
import com.codeland.uhc.util.ScoreboardDisplay
import com.codeland.uhc.util.ToolTier.ARMOR_SET
import com.codeland.uhc.util.ToolTier.LEATHER
import com.codeland.uhc.util.ToolTier.ONLY_TOOL_SET
import com.codeland.uhc.util.ToolTier.SHELL
import com.codeland.uhc.util.ToolTier.TIER_1
import com.codeland.uhc.util.ToolTier.TIER_3
import com.codeland.uhc.util.ToolTier.TIER_ELYTRA
import com.codeland.uhc.util.ToolTier.TIER_HELMET
import com.codeland.uhc.util.ToolTier.TIER_SHIELD
import com.codeland.uhc.util.ToolTier.WEAPON_SET
import com.codeland.uhc.util.ToolTier.WOOD
import com.codeland.uhc.util.ToolTier.getTieredTool
import org.bukkit.ChatColor.*
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionType
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

				Location(Util.worldFromEnvironment(uhc.defaultEnvironment), x, 0.0, z)
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
				LootEntry { CarePackageUtil.randomBucket() },
				LootEntry { CarePackageUtil.randomBucket() },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(true) },
				LootEntry { CarePackageUtil.randomPotion() },
				LootEntry { CarePackageUtil.flamingLazerSword() },
				LootEntry { CarePackageUtil.superSwaggyPants() },
				LootEntry { ItemStack(Material.BLAZE_ROD) },
				LootEntry { ItemStack(Material.BLAZE_ROD) },
				LootEntry { ItemStack(Material.BLAZE_ROD) },
				LootEntry { ItemStack(Material.BLAZE_ROD) },
				LootEntry { ItemStack(Material.NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(Material.NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(Material.NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(Material.NETHER_WART, pickOne(1, 2)) },
				LootEntry { ItemStack(Material.REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(Material.REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(Material.REDSTONE, pickOne(2, 4)) },
				LootEntry { ItemStack(Material.GUNPOWDER, pickOne(2, 4)) },
				LootEntry { ItemStack(Material.GUNPOWDER, pickOne(2, 4)) },
				LootEntry { ItemStack(Material.GUNPOWDER, pickOne(2, 4)) },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { CarePackageUtil.glowstone() },
				LootEntry { ItemStack(Material.ENDER_PEARL, pickOne(2, 4)) },
				LootEntry { ItemStack(Material.STONE, pickOne(16, 32, 48)) },
				LootEntry { ItemStack(Material.STONE, pickOne(16, 32, 48)) },
				LootEntry { ItemStack(Material.TORCH, pickOne(16, 32)) },
				LootEntry { ItemStack(Material.TORCH, pickOne(16, 32)) },
				LootEntry { ItemStack(Material.OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(Material.OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(Material.OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(Material.OBSIDIAN, pickOne(3, 4, 10)) },
				LootEntry { ItemStack(Material.EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(Material.EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { ItemStack(Material.EXPERIENCE_BOTTLE, pickOne(14, 18, 22)) },
				LootEntry { CarePackageUtil.randomBoat() }
			),
			arrayOf(
				LootEntry { CarePackageUtil.randomBucket() },
				LootEntry { CarePackageUtil.randomBucket() },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(true) },
				LootEntry { CarePackageUtil.randomPotion() },
				LootEntry { CarePackageUtil.flamingLazerSword() }
			),
			arrayOf(
				LootEntry { CarePackageUtil.randomBucket() },
				LootEntry { CarePackageUtil.randomBucket() },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(false) },
				LootEntry { CarePackageUtil.randomArmor(true) },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomStewPart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBottlePart() },
				LootEntry { CarePackageUtil.randomBrewingIngredient() },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(false) },
				LootEntry { CarePackageUtil.randomEnchantedBook(true) },
				LootEntry { CarePackageUtil.randomPotion() },
				LootEntry { CarePackageUtil.flamingLazerSword() }
			)
		)

		data class SpireData(val ore: Material, val block: Material)

		val SPIRE_COAL = SpireData(Material.COAL_ORE, Material.COAL_BLOCK)
		val SPIRE_IRON = SpireData(Material.IRON_ORE, Material.IRON_BLOCK)
		val SPIRE_LAPIS = SpireData(Material.LAPIS_ORE, Material.LAPIS_BLOCK)
		val SPIRE_GOLD = SpireData(Material.GOLD_ORE, Material.GOLD_BLOCK)
		val SPIRE_DIAMOND = SpireData(Material.DIAMOND_ORE, Material.DIAMOND_ORE)

		val spireAmounts = arrayOf(
			arrayOf(SPIRE_COAL, SPIRE_COAL, SPIRE_IRON, SPIRE_IRON),
			arrayOf(SPIRE_IRON, SPIRE_IRON, SPIRE_GOLD, SPIRE_LAPIS),
			arrayOf(SPIRE_IRON, SPIRE_GOLD, SPIRE_LAPIS, SPIRE_DIAMOND)
		)

		val wackyEntries = arrayOf(
			LootEntry { ItemStack(Material.CARROT, Util.randRange(7, 18)) },
			LootEntry { randomDye(Util.randRange(13, 28)) },
			LootEntry { getTieredTool(WEAPON_SET, WOOD, TIER_3, 3, ENCHANT_CHANCE) },
			LootEntry { getTieredTool(ONLY_TOOL_SET, WOOD, TIER_3, 3, ENCHANT_CHANCE) },
			LootEntry { randomDyeArmor(getTieredTool(ARMOR_SET, LEATHER, TIER_3, 3, ENCHANT_CHANCE)) },
			LootEntry { ItemStack(Material.TNT_MINECART) },
			LootEntry { ItemStack(Material.RAIL, Util.randRange(18, 64)) },
			LootEntry { ItemStack(Material.EGG, Util.randRange(7, 16)) },
			LootEntry { getTieredTool(TIER_SHIELD, 1, ENCHANT_CHANCE) },
			LootEntry { ItemStack(Material.ENDER_CHEST, Util.randRange(2, 17)) },
			LootEntry { ItemStack(Material.NETHERITE_HOE) },
			LootEntry { getTieredTool(TIER_ELYTRA, 1, ENCHANT_CHANCE) },
			LootEntry { ItemStack(Material.BLAZE_ROD) },
			LootEntry { ItemStack(Material.NETHER_WART) },
			LootEntry { randomMusicDisc() },
			LootEntry { ItemStack(Material.PANDA_SPAWN_EGG, Util.randRange(9, 27)) },
			LootEntry { Summoner.randomPassiveEgg(Util.randRange(9, 27)) },
			LootEntry { Summoner.randomAggroEgg(Util.randRange(4, 10)) },
			LootEntry { CarePackageUtil.regenerationStew() },
			LootEntry { ItemStack(Material.SPECTRAL_ARROW, Util.randRange(7, 16)) },
			LootEntry { ItemUtil.randomShulker(Util.randRange(1, 3)) },
			LootEntry { ItemUtil.randomRocket(Util.randRange(17, 33)) },
			LootEntry { randomTippedArrow(Util.randRange(3, 7), PotionType.INSTANT_HEAL) },
			LootEntry { randomTippedArrow(Util.randRange(1, 3), randFromArray(ItemUtil.badPotionTypes)) },
			LootEntry { ItemStack(Material.PUFFERFISH_BUCKET) },
			LootEntry { ItemStack(if (Math.random() < 0.5) Material.MELON else Material.CARVED_PUMPKIN, 64) },
			LootEntry { ItemStack(Material.DRAGON_BREATH, Util.randRange(2, 5)) },
			LootEntry { ItemStack(Material.TNT, Util.randRange(16, 32)) },
			LootEntry { ItemStack(Material.SADDLE) },
			LootEntry { getTieredTool(TIER_HELMET, SHELL, Util.randRange(TIER_1, TIER_3), 1, ENCHANT_CHANCE) },
			LootEntry { ItemStack(randFromArray(Creative.blocks), 64) },
			LootEntry { ItemStack(Material.WET_SPONGE, Util.randRange(1, 5)) },
			LootEntry { Pests.genPestTool(randFromArray(Pests.pestToolList)) },
			LootEntry { ItemUtil.randomPotion(false, Material.LINGERING_POTION) },
			LootEntry { ItemUtil.randomPotion(false, Material.SPLASH_POTION) },
			LootEntry { ItemUtil.randomPotion(true, Material.POTION) },
			LootEntry { ItemStack(Material.IRON_NUGGET, 64) },
			LootEntry { ItemStack(Material.GOLD_NUGGET, 64) },
			LootEntry { ItemStack(Material.IRON_BLOCK, Util.randRange(1, 2)) },
			LootEntry { ItemStack(Material.GOLD_BLOCK, Util.randRange(1, 2)) },
			LootEntry { CarePackageUtil.randomBucket() }
		)

		fun generateLoot(tier: Int, inventories: ArrayList<Inventory>) {
			fun addItem(inventory: Inventory, item: ItemStack) {
				var space = Util.randRange(0, inventory.size - 1)

				while (inventory.getItem(space) != null)
					space = (space + 1) % inventory.size

				inventory.setItem(space, item)
			}

			val usingEntries = chestEntries[tier].toMutableList() as ArrayList<LootEntry>
			usingEntries.shuffle()

			val perChest = usingEntries.size / inventories.size

			for (i in usingEntries.indices) {
				val inventoryIndex = (i / perChest).coerceAtMost(inventories.lastIndex)

				addItem(inventories[inventoryIndex], usingEntries[i].makeStack())
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
					random < 1 / 5.0 -> Material.ANDESITE
					else -> Material.STONE
				}, false)
			}

			fun isSpireBlock(block: Block): Boolean {
				return block.type == Material.STONE || block.type == Material.ANDESITE || block.type == spireData.ore
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
			block.type = Material.CHEST

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
				return block.type == Material.GRASS_BLOCK ||
					block.type == Material.STONE ||
					block.type == Material.DIRT ||
					block.type == Material.SAND ||
					block.type == Material.RED_SAND ||
					block.type == Material.PODZOL
			}

			val directions = arrayOf(BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH)

			for (y in 255 downTo 0) {
				val block = world.getBlockAt(x, y, z)

				if (block.type == Material.SUGAR_CANE || block.type == Material.WATER || block.type == Material.LAVA) return false

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

							block.setType(Material.WATER, false)
							baseBlock.setType(Material.SAND, false)

							for (i in 0 until height) {
								baseBlock = baseBlock.getRelative(BlockFace.UP)
								baseBlock.setType(Material.SUGAR_CANE, false)
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

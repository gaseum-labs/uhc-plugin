package com.codeland.uhc.quirk.quirks.carePackages

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.*
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.phases.*
import com.codeland.uhc.event.Brew
import com.codeland.uhc.event.Brew.Companion
import com.codeland.uhc.event.Brew.Companion.PotionInfo
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_COAL
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_DIAMOND
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_GOLD
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_IRON
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_LAPIS
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.pickOne
import com.codeland.uhc.util.*
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.ChatColor.*
import org.bukkit.Material.*
import org.bukkit.Material.WATER
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.*
import org.bukkit.potion.PotionType.*
import kotlin.math.*
import kotlin.random.Random

class CarePackages(type: QuirkType, game: Game) : Quirk(type, game) {
	val NUM_DROPS = 2

	var taskID = -1

	var scoreboardDisplay: ScoreboardDisplay? = null

	var running = false

	var timer = 0

	lateinit var dropLocations: Array<Location>
	lateinit var dropTimes: Array<Int>

	var dropIndex = 0

	val random = Random(game.world.seed)

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

	init {
		if (game.phase is Grace || game.phase is Shrink) onStart()
	}

	override fun customDestroy() {
		onEnd()
	}

	override fun onPhaseSwitch(phase: Phase) {
		if (phase is Grace) onStart()
		else if (phase is Endgame || phase is Postgame) onEnd()
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

				Location(game.world, x, 0.0, z)
			}
		}

		/* find drop times */
		if (game.phase is Grace) {
			val remaining = game.phase.remainingSeconds()

			/* distribute drops over shrinking phase so that if there were another, */
			/* it would fall exactly at the end of shrinking phase */

			val dropPeriod = game.config.shrinkTime.get() * (NUM_DROPS / (NUM_DROPS + 1.0))
			val dropInterval = (dropPeriod / NUM_DROPS).toInt()

			/* all drops are equally spaced by dropInterval */
			dropTimes = Array(NUM_DROPS) { dropInterval }

			/* the first drop has to wait for the end of grace period */
			dropTimes[0] += remaining

			findLocations { i ->
				game.initialRadius * (1 - ((dropInterval * i).toDouble() / game.config.shrinkTime.get()))
			}

		} else if (game.phase is Shrink) {
			val elapsed = game.config.shrinkTime.get() - game.phase.remainingSeconds()

			val cutOff = game.config.shrinkTime.get() * (NUM_DROPS / (NUM_DROPS + 1.0)).toInt()

			val available = (cutOff - elapsed)
			val dropInterval = (available / NUM_DROPS)

			/* there must be at least 1 second left for each care package to drop */
			if (available <= NUM_DROPS) dropTimes = Array(NUM_DROPS) { available / NUM_DROPS }
			else return false

			findLocations { i ->
				game.initialRadius * (1 - ((elapsed + dropInterval * i).toDouble() / game.config.shrinkTime.get()))
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
			else if (i == dropIndex)
				"${color}${Util.timeString(timer)}"
			else
				"${color}Awaiting"
			)
		}
	}

	private fun perSecond() {
		if (dropIndex < NUM_DROPS) {
			--timer

			if (timer <= 0) {
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
		val chestEntries = arrayOf<Array<ItemCreator>>(
			arrayOf<ItemCreator>(
				CarePackagesItems.helmet,
				CarePackagesItems.chestplate,
				CarePackagesItems.leggings,
				CarePackagesItems.boots,

				CarePackagesItems.smallHelmet,
				CarePackagesItems.smallChestplate,
				CarePackagesItems.smallLeggings,
				CarePackagesItems.smallBoots,

				CarePackagesItems.sword0,
				CarePackagesItems.sword0,
				CarePackagesItems.axe0,
				CarePackagesItems.axe0,
				CarePackagesItems.bow,
				CarePackagesItems.bow,
				CarePackagesItems.crossbow0,
				CarePackagesItems.crossbow0,

				CarePackagesItems.enchantedBook,
				CarePackagesItems.enchantedBook,
				CarePackagesItems.enchantedBook,
				CarePackagesItems.enchantedBook,

				CarePackagesItems.supaPickaxe,

				CarePackagesItems.trident,

				ItemCreator.regular(ARROW).amount(16),
				ItemCreator.regular(ARROW).amount(16),
				ItemCreator.regular(ARROW).amount(16),
				ItemCreator.regular(ARROW).amount(16),
				ItemCreator.regular(ARROW).amount(16),

				ItemCreator.regular(SPECTRAL_ARROW).amount(16),
				ItemCreator.regular(SPECTRAL_ARROW).amount(16),
				ItemCreator.regular(SPECTRAL_ARROW).amount(16),

				CarePackagesItems.poisonTippedArrow,
				CarePackagesItems.slownessTippedArrow,
				CarePackagesItems.weaknessTippedArrow,

				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),

				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),

				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),

				ItemCreator.regular(ANVIL),
				ItemCreator.regular(ENCHANTING_TABLE),
				ItemCreator.regular(BREWING_STAND),
				ItemCreator.regular(BLAST_FURNACE).amount(8),

				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),

				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,

				CarePackagesItems.elytraRocket,
				CarePackagesItems.elytraRocket,

				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),

				ItemCreator.regular(SPYGLASS),
				ItemCreator.regular(SPYGLASS),

				/* brewing ingredients */
				ItemCreator.regular(GLISTERING_MELON_SLICE).amount(2),
				ItemCreator.regular(GLISTERING_MELON_SLICE).amount(2),
				ItemCreator.regular(GLISTERING_MELON_SLICE).amount(2),
				ItemCreator.regular(GHAST_TEAR).amount(2),
				ItemCreator.regular(GHAST_TEAR).amount(2),
				ItemCreator.regular(GHAST_TEAR).amount(2),
				ItemCreator.regular(SPIDER_EYE).amount(2),
				ItemCreator.regular(SPIDER_EYE).amount(2),
				ItemCreator.regular(FERMENTED_SPIDER_EYE).amount(2),
				ItemCreator.regular(FERMENTED_SPIDER_EYE).amount(2),
				ItemCreator.regular(GUNPOWDER).amount(5),
				ItemCreator.regular(GUNPOWDER).amount(5),
				ItemCreator.regular(GUNPOWDER).amount(5),
				ItemCreator.regular(GLOWSTONE).amount(12),
				ItemCreator.regular(GLOWSTONE).amount(12),
				ItemCreator.regular(GLOWSTONE).amount(12),
				ItemCreator.regular(BLAZE_ROD).amount(1),
				ItemCreator.regular(BLAZE_ROD).amount(1),
				ItemCreator.regular(BLAZE_ROD).amount(1),
				ItemCreator.regular(NETHER_WART).amount(4),
				ItemCreator.regular(NETHER_WART).amount(4),
				ItemCreator.regular(NETHER_WART).amount(4),
				ItemCreator.regular(NETHER_WART).amount(4),

				Brew.externalCreatePotion(SPLASH_POTION, Brew.POISON_INFO, true, false),
				Brew.externalCreatePotion(SPLASH_POTION, Brew.WEAKNESS_INFO, true, false),
				Brew.externalCreatePotion(SPLASH_POTION, Brew.REGEN_INFO, true, false),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(INSTANT_HEAL, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(INSTANT_HEAL, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(INSTANT_HEAL, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(FIRE_RESISTANCE, true, false)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(SPEED, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(JUMP, false, true)),
			),
			arrayOf<ItemCreator>(
				CarePackagesItems.helmet,
				CarePackagesItems.chestplate,
				CarePackagesItems.leggings,
				CarePackagesItems.boots,

				CarePackagesItems.sword1,
				CarePackagesItems.sword1,
				CarePackagesItems.axe1,
				CarePackagesItems.axe1,
				CarePackagesItems.shredderBow,
				CarePackagesItems.crossbow1,
				CarePackagesItems.crossbow1,

				CarePackagesItems.enchantedBook,
				CarePackagesItems.enchantedBook,
				CarePackagesItems.enchantedBook,
				CarePackagesItems.enchantedBook,

				ItemCreator.regular(ARROW).amount(16),
				ItemCreator.regular(ARROW).amount(16),
				ItemCreator.regular(ARROW).amount(16),
				ItemCreator.regular(ARROW).amount(16),

				ItemCreator.regular(SPECTRAL_ARROW).amount(16),
				ItemCreator.regular(SPECTRAL_ARROW).amount(16),
				ItemCreator.regular(SPECTRAL_ARROW).amount(16),
				ItemCreator.regular(SPECTRAL_ARROW).amount(16),

				CarePackagesItems.poisonTippedArrow,
				CarePackagesItems.poisonTippedArrow,
				CarePackagesItems.slownessTippedArrow,
				CarePackagesItems.slownessTippedArrow,
				CarePackagesItems.weaknessTippedArrow,
				CarePackagesItems.weaknessTippedArrow,

				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),
				ItemCreator.regular(EXPERIENCE_BOTTLE).amount(32),

				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),
				ItemCreator.regular(BOOK).amount(9),

				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),
				ItemCreator.regular(OBSIDIAN).amount(10),

				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),
				ItemCreator.regular(GOLDEN_APPLE),

				/* op items */
				ItemCreator.regular(GHAST_SPAWN_EGG).amount(3),
				ItemCreator.regular(ELYTRA),
				ItemCreator.regular(TOTEM_OF_UNDYING),
				KillReward.uhcAppleCreator,
				Brew.externalCreatePotion(POTION, Brew.STRENGTH_INFO, true, false).name(
					Util.gradientString("Illegal Substance", TextColor.color(0xe8e854), TextColor.color(0xa86007))
				),

				CarePackagesItems.regenerationStew,
				CarePackagesItems.regenerationStew,

				CarePackagesItems.elytraRocket,
				CarePackagesItems.elytraRocket,
				CarePackagesItems.elytraRocket,
				CarePackagesItems.elytraRocket,
				CarePackagesItems.elytraRocket,

				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),
				ItemCreator.regular(ENDER_PEARL).amount(3),

				/* brewing ingredients */
				ItemCreator.regular(GLISTERING_MELON_SLICE).amount(2),
				ItemCreator.regular(GLISTERING_MELON_SLICE).amount(2),
				ItemCreator.regular(GHAST_TEAR).amount(2),
				ItemCreator.regular(GHAST_TEAR).amount(2),
				ItemCreator.regular(SPIDER_EYE).amount(2),
				ItemCreator.regular(FERMENTED_SPIDER_EYE).amount(2),
				ItemCreator.regular(GUNPOWDER).amount(5),
				ItemCreator.regular(GLOWSTONE).amount(12),
				ItemCreator.regular(BLAZE_ROD).amount(1),
				ItemCreator.regular(NETHER_WART).amount(4),
				ItemCreator.regular(NETHER_WART).amount(4),

				Brew.externalCreatePotion(SPLASH_POTION, Brew.POISON_INFO, true, false),
				Brew.externalCreatePotion(SPLASH_POTION, Brew.WEAKNESS_INFO, true, false),
				Brew.externalCreatePotion(SPLASH_POTION, Brew.REGEN_INFO, true, false),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(INSTANT_HEAL, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(INSTANT_HEAL, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(INSTANT_HEAL, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(FIRE_RESISTANCE, true, false)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(SPEED, false, true)),
				Brew.createDefaultPotion(SPLASH_POTION, PotionData(JUMP, false, true)),
			)
		)

		val spireAmounts = arrayOf(
			arrayOf(SPIRE_IRON, SPIRE_GOLD, SPIRE_LAPIS, SPIRE_DIAMOND),
			arrayOf(SPIRE_GOLD, SPIRE_LAPIS, SPIRE_DIAMOND, SPIRE_DIAMOND)
		)

		fun generateLoot(tier: Int, inventories: ArrayList<Inventory>) {
			val usingEntries = chestEntries[tier].toMutableList()
			usingEntries.shuffle()

			val perChest = usingEntries.size / inventories.size

			for (i in usingEntries.indices) {
				val inventoryIndex = (i / perChest).coerceAtMost(inventories.lastIndex)

				ItemUtil.randomAddInventory(inventories[inventoryIndex], usingEntries[i].create())
			}
		}

		fun dropTextColor(tier: Int): ChatColor {
			return when (tier) {
				0 -> GOLD
				1 -> BLUE
				2 -> LIGHT_PURPLE
				else -> GRAY
			}
		}

		fun circlePlacement(
			centerX: Int,
			centerZ: Int,
			minRadius: Float,
			maxRadius: Float,
			numPlaces: Int,
			onPlace: (Int, Int, Int) -> Unit,
		) {
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

		fun spirePlacement(
			world: World,
			centerX: Int,
			centerZ: Int,
			placeRadius: Int,
			spireList: Array<CarePackageUtil.SpireData>,
		) {
			circlePlacement(centerX, centerZ, placeRadius * 0.5f, placeRadius.toFloat(), spireList.size) { i, x, z ->
				CarePackageUtil.generateSpire(world, CarePackageUtil.dropBlock(world, x, z), 5f, 14, spireList[i])
			}
		}

		fun chestPlacement(
			world: World,
			centerX: Int,
			centerZ: Int,
			placeRadius: Int,
			ringChests: Int,
			tier: Int,
		): ArrayList<Inventory> {
			val inventoryList = ArrayList<Inventory>()

			circlePlacement(centerX, centerZ, placeRadius * 0.4f, placeRadius * 0.6f, ringChests) { _, x, z ->
				inventoryList.add(CarePackageUtil.generateChest(world,
					CarePackageUtil.dropBlock(world, x, z),
					dropTextColor(tier)))
			}

			inventoryList.add(CarePackageUtil.generateChest(world,
				CarePackageUtil.dropBlock(world, centerX, centerZ),
				dropTextColor(tier)))

			return inventoryList
		}

		fun generateDrop(tier: Int, location: Location) {
			val world = location.world

			val radius = 30

			val x = location.blockX
			val z = location.blockZ

			spirePlacement(world, x, z, radius, spireAmounts[tier])
			generateLoot(tier, chestPlacement(world, x, z, radius, 5, tier))

			Bukkit.getOnlinePlayers().forEach { player ->
				player.sendMessage("${dropTextColor(tier)}${BOLD}Drop at (${x}, ${z})")
			}
		}
	}
}

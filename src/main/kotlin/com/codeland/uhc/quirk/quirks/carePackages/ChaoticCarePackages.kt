package com.codeland.uhc.quirk.quirks.carePackages

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.Summoner
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.ArmorType
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_COAL
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_DIAMOND
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_GOLD
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_IRON
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.SPIRE_LAPIS
import com.codeland.uhc.quirk.quirks.carePackages.CarePackageUtil.ToolType
import com.codeland.uhc.util.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.random.Random

class ChaoticCarePackages(type: QuirkType) : Quirk(type) {
	override fun onEnable() {
		if (UHC.isGameGoing()) startDropping()
	}

	override fun customDestroy() {
		stopDropping()
	}

	override val representation = ItemCreator.fromType(TIPPED_ARROW)
		.customMeta { meta -> (meta as PotionMeta).basePotionData = PotionData(PotionType.INSTANT_HEAL) }

	override fun onPhaseSwitch(phase: PhaseVariant) {
		when (phase.type) {
			PhaseType.GRACE -> startDropping()
			PhaseType.ENDGAME -> stopDropping()
			PhaseType.WAITING -> stopDropping()
		}
	}

	fun startDropping() {
		timer = 0
		dropNum = 0
		random = Random(UHC.getDefaultWorldGame().seed)
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, ::onSecond, 0, 20)
	}

	fun stopDropping() {
		Bukkit.getScheduler().cancelTask(taskId)
	}

	val DROP_TIME = 5

	var taskId = -1

	var timer = 0
	var dropNum = 0
	lateinit var random: Random

	fun onSecond() {
		++timer

		if (timer >= DROP_TIME) {
			val world = UHC.getDefaultWorldGame()
			val block = chaoticDropBlock(world)
			val along = gameAlong()

			/* drop a spire every 20 drops */
			if (dropNum % 20 == 0) {
				CarePackageUtil.generateSpire(world, block, 5f, 14, getSpire(random, along))

			/* otherwise drop a care package */
			} else {
				val inventory = CarePackageUtil.generateChest(world, block, ChatColor.values()[random.nextInt(ChatColor.MAGIC.ordinal)])

				fillChestInventory(inventory, 8, 14, along)
			}

			Bukkit.getOnlinePlayers().forEach { player ->
				if (PlayerData.isParticipating(player.uniqueId)) {
					GameRunner.sendGameMessage(player, "Care package dropped at ${block.x} ${block.y} ${block.z}")
				}
			}

			++dropNum
			timer = 0
		}
	}

	/* util for onSecond() */

	val spireTypes = arrayOf(
		SPIRE_COAL,
		SPIRE_IRON,
		SPIRE_GOLD,
		SPIRE_LAPIS,
		SPIRE_DIAMOND
	)

	fun getSpire(random: Random, along: Double): CarePackageUtil.SpireData {
		val max = ceil(spireTypes.size * along).toInt().coerceAtMost(spireTypes.size)

		return spireTypes[random.nextInt(0, max)]
	}

	/**
	 * @return the block that a chest or spire will generate at
	 *
	 * picks the space randomly within the worldborder of the given world
	 */
	fun chaoticDropBlock(world: World): Block {
		val radius = (world.worldBorder.size / 2.0) - 5.0

		return CarePackageUtil.dropBlock(
			world,
			((random.nextDouble() * radius * 2.0) - radius).roundToInt(),
			((random.nextDouble() * radius * 2.0) - radius).roundToInt()
		)
	}

	/**
	 * @return a value from 0 to 1 for how complete this game is
	 *
	 * 0 is grace just started,
	 * 1 is shrink just ended
	 */
	fun gameAlong(): Double {
		val phase = UHC.currentPhase ?: return 0.0
		val graceTime = UHC.phaseTime(PhaseType.GRACE)
		val shrinkTime = UHC.phaseTime(PhaseType.SHRINK)

		return when (phase.phaseType) {
			/* time elapsed in grace out of grace time plus shrink time */
			PhaseType.GRACE -> (graceTime - phase.remainingSeconds).toDouble() / (graceTime + shrinkTime)
			/* time grace time plus time elapsed in shrink out of grace time plus shrink time */
			PhaseType.SHRINK -> (graceTime + (shrinkTime - phase.remainingSeconds)).toDouble() / (graceTime + shrinkTime)
			else -> 1.0
		}
	}

	fun fillChestInventory(inventory: Inventory, low: Int, high: Int, along: Double) {
		fun addItem(itemStack: ItemStack) {
			var slot = random.nextInt(0, inventory.size)
			while (inventory.getItem(slot) != null) slot = (slot + 1) % inventory.size

			inventory.setItem(slot, itemStack)
		}

		val numItems = random.nextInt(low, high + 1)

		/* find greatest item in the chaotic items list */
		var greatestItem = 0
		for (itemPair in chaoticItems) {
			if (itemPair.second > along)
				break
			else
				++greatestItem
		}

		for (i in 0 until numItems) {
			addItem(chaoticItems[random.nextInt(0, greatestItem)].first())
		}
	}

	fun itemAmount(material: Material, low: Int, high: Int): () -> ItemStack =
		{ ItemStack(material, random.nextInt(low, high + 1)) }

	/**
	 * first in each pair is a funciton that generates the item
	 *
	 * second in each pair is the along level that must be cleared
	 * to start generating this item
	 */
	val chaoticItems: Array<Pair<() -> ItemStack, Double>> = arrayOf(
		Pair(itemAmount(COAL, 5, 15), 0.0),
		Pair(itemAmount(LEATHER, 1, 6), 0.0),
		Pair(itemAmount(SUGAR_CANE, 3, 8), 0.0),
		Pair(itemAmount(IRON_INGOT, 4, 6), 0.0),
		Pair(itemAmount(BONE, 6, 17), 0.0),
		Pair(itemAmount(OAK_LOG, 14, 28), 0.0),
		Pair(itemAmount(SAND, 8, 13), 0.0),
		Pair(itemAmount(FLINT, 2, 4), 0.0),
		Pair(itemAmount(COOKED_CHICKEN, 7, 14), 0.0),
		Pair(itemAmount(COBBLESTONE, 23, 57), 0.0),
		Pair({ CarePackageUtil.randomBoat() }, 0.0),
		Pair({ CarePackageUtil.randomStewPart() }, 0.0),
		Pair({ CarePackageUtil.chaoticArmor(random, ArmorType.LEATHER) }, 0.0),
		Pair({ CarePackageUtil.chaoticPick(ToolType.WOOD) }, 0.0),
		Pair({ CarePackageUtil.chaoticSword(ToolType.WOOD) }, 0.0),
		Pair({ CarePackageUtil.chaoticAxe(ToolType.WOOD) }, 0.0),

		Pair({ CarePackageUtil.randomBucket() }, 0.1),
		Pair(itemAmount(APPLE, 1, 3), 0.1),
		Pair(itemAmount(COOKED_BEEF, 6, 11), 0.1),
		Pair(itemAmount(COOKED_PORKCHOP, 5, 12), 0.1),
		Pair({ ItemUtil.randomMusicDisc() }, 0.1),
		Pair({ CarePackageUtil.turtleShell(random) }, 0.1),
		Pair({ Summoner.randomPassiveEgg(random.nextInt(4, 9)) }, 0.1),
		Pair({ CarePackageUtil.chaoticArmor(random, ArmorType.GOLD) }, 0.1),
		Pair({ CarePackageUtil.chaoticPick(ToolType.GOLD) }, 0.1),
		Pair({ CarePackageUtil.chaoticSword(ToolType.GOLD) }, 0.1),
		Pair({ CarePackageUtil.chaoticAxe(ToolType.GOLD) }, 0.1),

		Pair(itemAmount(GUNPOWDER, 8, 14), 0.2),
		Pair(itemAmount(FEATHER, 2, 7), 0.2),
		Pair(itemAmount(MELON, 2, 14), 0.2),
		Pair(itemAmount(REDSTONE, 4, 13), 0.2),
		Pair(itemAmount(STRING, 3, 6), 0.2),
		Pair({ ItemUtil.randomDye(random.nextInt(6, 27)) }, 0.2),
		Pair({ CarePackageUtil.chaoticArmor(random, ArmorType.CHAIN) }, 0.2),
		Pair({ CarePackageUtil.chaoticPick(ToolType.STONE) }, 0.2),
		Pair({ CarePackageUtil.chaoticSword(ToolType.STONE) }, 0.2),
		Pair({ CarePackageUtil.chaoticAxe(ToolType.STONE) }, 0.2),

		Pair(itemAmount(OBSIDIAN, 2, 8), 0.3),
		Pair(itemAmount(PAPER, 6, 19), 0.3),
		Pair({ ItemStack(FLINT_AND_STEEL) }, 0.3),
		Pair({ ItemStack(TNT_MINECART) }, 0.3),
		Pair(itemAmount(RAIL, 20, 35), 0.3),
		Pair({ ItemStack(SADDLE) }, 0.3),
		Pair({ Summoner.randomAggroEgg(random.nextInt(2, 7)) }, 0.3),
		Pair({ CarePackageUtil.regenerationStew() }, 0.3),
		Pair({ ItemUtil.randomFireworkStar(16) }, 0.3),

		Pair({ ItemStack(SHIELD) }, 0.4),
		Pair(itemAmount(EXPERIENCE_BOTTLE, 5, 13), 0.4),
		Pair(itemAmount(IRON_BLOCK, 1, 2), 0.4),
		Pair(itemAmount(GOLD_INGOT, 3, 12), 0.4),
		Pair(itemAmount(LAPIS_LAZULI, 4, 16), 0.4),
		Pair(itemAmount(GLOWSTONE_DUST, 7, 15), 0.4),
		Pair(itemAmount(ARROW, 4, 11), 0.4),
		Pair({ CarePackageUtil.piercingCrossbow() }, 0.4),

		Pair(itemAmount(GOLD_BLOCK, 1, 2), 0.5),
		Pair(itemAmount(SPIDER_EYE, 2, 4), 0.5),
		Pair(itemAmount(ENDER_PEARL, 2, 5), 0.5),
		Pair(itemAmount(BOOK, 1, 3), 0.5),
		Pair(itemAmount(GLASS, 6, 12), 0.5),
		Pair(itemAmount(GLASS_BOTTLE, 3, 7), 0.5),
		Pair({ CarePackageUtil.chaoticTippedArrow(random, 4, 8) }, 0.5),
		Pair({ CarePackageUtil.randomEnchantedBook(false) }, 0.5),
		Pair({ CarePackageUtil.flamingLazerSword() }, 0.5),
		Pair({ CarePackageUtil.superSwaggyPants() }, 0.5),
		Pair({ CarePackageUtil.powerBow(1) }, 0.5),
		Pair({ CarePackageUtil.quickChargeCrossbow() }, 0.5),
		Pair({ CarePackageUtil.chaoticArmor(random, ArmorType.IRON) }, 0.5),
		Pair({ CarePackageUtil.chaoticPick(ToolType.IRON) }, 0.5),
		Pair({ CarePackageUtil.chaoticSword(ToolType.IRON) }, 0.5),
		Pair({ CarePackageUtil.chaoticAxe(ToolType.IRON) }, 0.5),

		Pair(itemAmount(BLAZE_ROD, 1, 3), 0.6),
		Pair(itemAmount(GLISTERING_MELON_SLICE, 1, 3), 0.6),
		Pair(itemAmount(GHAST_TEAR, 1, 2), 0.6),
		Pair(itemAmount(NETHER_WART, 2, 3), 0.6),
		Pair(itemAmount(MAGMA_CREAM, 6, 11), 0.6),
		Pair(itemAmount(DIAMOND, 1, 5), 0.6),
		Pair(itemAmount(SPECTRAL_ARROW, 8, 18), 0.6),
		Pair({ CarePackageUtil.randomPotion(random) }, 0.6),

		Pair(itemAmount(GOLDEN_APPLE, 1, 3), 0.7),
		Pair(itemAmount(FERMENTED_SPIDER_EYE, 1, 2), 0.7),
		Pair({ CarePackageUtil.elytraRocket(8) }, 0.7),
		Pair({ CarePackageUtil.randomEnchantedBook(true) }, 0.7),

		Pair({ ItemStack(NETHERITE_SCRAP, 4) }, 0.8),
		Pair({ ItemStack(DIAMOND_BLOCK) }, 0.8),
		Pair({ ItemStack(ELYTRA) }, 0.8),
		Pair({ CarePackageUtil.randomPotion(random) }, 0.8),
		Pair({ CarePackageUtil.chaoticArmor(random, ArmorType.DIAMOND) }, 0.8),
		Pair({ CarePackageUtil.chaoticPick(ToolType.DIAMOND) }, 0.8),
		Pair({ CarePackageUtil.chaoticSword(ToolType.DIAMOND) }, 0.8),
		Pair({ CarePackageUtil.chaoticAxe(ToolType.DIAMOND) }, 0.8),
	)
}

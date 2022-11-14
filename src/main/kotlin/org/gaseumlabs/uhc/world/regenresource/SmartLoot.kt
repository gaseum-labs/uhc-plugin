package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType
import org.gaseumlabs.uhc.chc.chcs.carePackages.CarePackageUtil
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.team.Teams
import org.gaseumlabs.uhc.util.Action

object SmartLoot {
	fun goldCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.GOLD_NUGGET -> 1
			Material.RAW_GOLD -> 9
			Material.GOLD_INGOT -> 9
			Material.GOLD_BLOCK -> 9 * 9
			else -> 0
		}
	} / 9

	fun diamondCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.DIAMOND -> 1
			Material.DIAMOND_BLOCK -> 9
			Material.DIAMOND_PICKAXE -> 3
			Material.DIAMOND_AXE -> 3
			Material.DIAMOND_SWORD -> 2
			Material.DIAMOND_HELMET -> 5
			Material.DIAMOND_CHESTPLATE -> 8
			Material.DIAMOND_LEGGINGS -> 7
			Material.DIAMOND_BOOTS -> 4
			Material.ENCHANTING_TABLE -> 2
			else -> 0
		}
	}

	fun appleCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.APPLE) it.amount else 0 }

	fun blazeRodCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.BLAZE_ROD -> 2
			Material.BLAZE_POWDER -> 1
			Material.BREWING_STAND -> 2
			else -> 0
		}
	} / 2

	fun wartCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.NETHER_WART) it.amount else 0 }

	fun gunpowderCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.GUNPOWDER) it.amount else 0 }

	fun sandCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.SAND -> 1
			Material.GLASS -> 1
			Material.GLASS_BOTTLE -> 1
			Material.POTION -> 1
			else -> 0
		}
	}

	fun glowstoneCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.GLOWSTONE_DUST -> 1
			Material.GLOWSTONE -> 3
			else -> 0
		}
	}

	fun flintCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.GRAVEL -> 1
			Material.FLINT -> 10
			else -> 0
		}
	} / 10

	fun featherCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.FEATHER) it.amount else 0 }

	fun stringCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.STRING -> 1
			Material.BOW -> 3
			Material.CROSSBOW -> 2
			else -> 0
		}
	}

	fun lapisCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.LAPIS_LAZULI -> 1
			Material.LAPIS_BLOCK -> 9
			else -> 0
		}
	}

	fun smeltCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.LAVA_BUCKET -> 100
			Material.COAL_BLOCK -> 80
			Material.DRIED_KELP_BLOCK -> 20
			Material.BLAZE_ROD -> 12
			Material.COAL -> 8
			Material.CHARCOAL -> 8
			else -> 0
		}
	}

	fun saddleCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.SADDLE) it.amount else 0 }

	fun brownMushroomCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.BROWN_MUSHROOM) it.amount else 0 }

	fun redMushroomCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.RED_MUSHROOM) it.amount else 0 }

	fun oxeyeCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.OXEYE_DAISY) it.amount else 0 }

	fun potentialHealthCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.GOLDEN_APPLE -> 4.0
			Material.ENCHANTED_GOLDEN_APPLE -> 4.0
			Material.SUSPICIOUS_STEW -> 3.0
			Material.POTION,
			Material.SPLASH_POTION -> {
				val data = it.itemMeta as PotionMeta
				data.customEffects.sumOf { effect -> when (effect.type) {
					PotionEffectType.HEAL -> 2.0 * effect.amplifier
					PotionEffectType.REGENERATION -> effect.duration /
						if (effect.amplifier == 1) 50.0 else 25.0
					else -> 0.0
				} }
			}
			else -> 0.0
		}
	}

	fun leatherCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.LEATHER -> 1
			Material.BOOK -> 1
			Material.ENCHANTED_BOOK -> 1
			Material.BOOKSHELF -> 3
			Material.ENCHANTING_TABLE -> 1
			else -> 0
		}
	}

	fun paperCount(items: List<ItemStack>) = items.sumOf {
		it.amount * when (it.type) {
			Material.PAPER -> 1
			Material.SUGAR_CANE -> 1
			Material.BOOK -> 3
			Material.ENCHANTED_BOOK -> 3
			Material.BOOKSHELF -> 9
			Material.ENCHANTING_TABLE -> 3
			else -> 0
		}
	}

	fun arrowCount(items: List<ItemStack>) = items.sumOf { if (it.type === Material.ARROW) it.amount else 0 }

	/* -------------------------------------------------------- */

	fun findGroundItems(list: ArrayList<ItemStack>, location: Location) {
		list.addAll(
			location.world.getNearbyEntitiesByType(Item::class.java, location, 16.0).map { it.itemStack }
		)
	}

	fun findContainerItems(list: ArrayList<ItemStack>, location: Location) {
		val center = location.block

		for (x in -9..9) {
			for (y in -4..4) {
				for (z in -9..9) {
					val block = center.getRelative(x, y, z)
					val state = block.getState(false)
					if (state is Container) {
						list.addAll(state.inventory.contents.filterNotNull())
					}
				}
			}
		}
	}

	fun calculateDeficiencies(
		teams: Teams<Team>,
		team: Team,
		surface: Boolean,
		luck: Int,
	): List<ItemStack> {
		val phase = UHC.game?.phase?.phaseType ?: return emptyList()
		val maxTeamSize = teams.teams().maxOf { it.members.count { uuid -> PlayerData.get(uuid).participating } }

		val playerDatas = team.members.map { PlayerData.get(it) }
			.filter { it.participating }

		if (playerDatas.isEmpty()) return emptyList()

		val teammateDown = playerDatas.size < maxTeamSize

		val teamItems = ArrayList<ItemStack>()
		playerDatas.forEach { data ->
			Action.getPlayerLocation(data.uuid)?.let {
				findGroundItems(teamItems, it)
				findContainerItems(teamItems, it)
			}
			Action.playerInventory(data.uuid)?.let {
				it.forEach { item -> if (item != null) teamItems.add(item) }
			}
		}

		/* ------------------------------ */
		val gold = goldCount(teamItems)
		val diamond = diamondCount(teamItems)
		val apple = appleCount(teamItems)
		val blazeRod = blazeRodCount(teamItems)
		val wart = wartCount(teamItems)
		val gunpowder = gunpowderCount(teamItems)
		val glowstone = glowstoneCount(teamItems)
		val flint = flintCount(teamItems)
		val feather = featherCount(teamItems)
		val string = stringCount(teamItems)
		val lapis = lapisCount(teamItems)
		val smelts = smeltCount(teamItems)
		val brownMushroom = brownMushroomCount(teamItems)
		val redMushroom = redMushroomCount(teamItems)
		val oxeye = oxeyeCount(teamItems)
		val leather = leatherCount(teamItems)
		val paper = paperCount(teamItems)
		val arrow = arrowCount(teamItems)
		val sand = sandCount(teamItems)
		val saddle = saddleCount(teamItems)
		/* ------------------------------ */
		var totalHealth = playerDatas.sumOf { Action.playerHealth(it.uuid) ?: 0.0 }
		totalHealth += potentialHealthCount(teamItems)
		totalHealth += brownMushroom.coerceAtMost(redMushroom).coerceAtMost(oxeye) * 3.0
		totalHealth += apple.coerceAtMost(gold / 8) * 4.0
		val healthBracket = totalHealth / (maxTeamSize * 20.0)
		/* ------------------------------ */

		val potentials = ArrayList<ItemStack>()

		if (surface) {
			/* apple to craft golden apple */
			if ((gold / 8) > apple) potentials.add(ItemStack(Material.APPLE))

			/* feather to craft arrow */
			if (flint > feather) potentials.add(ItemStack(Material.FEATHER, 2))

			/* sugar cane to craft book */
			if (leather > (paper / 3)) potentials.add(ItemStack(Material.SUGAR_CANE, 3))

			/* leather to craft book */
			if ((paper / 3) > leather) potentials.add(ItemStack(Material.LEATHER, 1))

			/* need oxeye to heal */
			if (oxeye < brownMushroom.coerceAtMost(redMushroom)) potentials.add(ItemStack(Material.OXEYE_DAISY))

			/* need sand to brew */
			if (sand == 0) potentials.add(ItemStack(Material.SAND, 12))
		}

		/* gold to craft golden apple */
		if (gold > 0 && apple > (gold / 8)) potentials.add(ItemStack(Material.GOLD_INGOT, 4))

		/* flint to craft arrow */
		if (feather > flint) potentials.add(ItemStack(Material.FLINT, 2))

		/* glowstone to craft spectral arrow */
		if (glowstone > 0 && arrow > (glowstone / 4)) potentials.add(ItemStack(Material.GLOWSTONE_DUST, 8))

		/* arrow to craft spectral arrow */
		if ((glowstone / 4) > arrow) potentials.add(ItemStack(Material.ARROW, 3))

		/* not enough string for a bow */
		if (phase >= PhaseType.SHRINK && string < 3) potentials.add(ItemStack(Material.STRING, 2))

		/* need brown mushroom to heal */
		if (brownMushroom < redMushroom.coerceAtMost(oxeye)) potentials.add(ItemStack(Material.BROWN_MUSHROOM))

		/* need red mushroom to heal */
		if (redMushroom < brownMushroom.coerceAtMost(oxeye)) potentials.add(ItemStack(Material.RED_MUSHROOM))

		/* need coal to smelt */
		if ((phase >= PhaseType.SHRINK || smelts > 0) && smelts < 8) potentials.add(ItemStack(Material.COAL, 3))

		val brewable = blazeRod > 0 || wart > 0

		/* need blaze rod to brew */
		if (brewable && blazeRod < (wart / 4)) potentials.add(ItemStack(Material.BLAZE_ROD))

		/* need wart to brew */
		if (brewable && (wart / 4) < blazeRod) potentials.add(ItemStack(Material.NETHER_WART))

		/* enough to get a new cool brewing ingredient */
		if (brewable && blazeRod >= 2 && wart >= 4) potentials.add(arrayOf(
			ItemStack(Material.MELON_SLICE),
			ItemStack(Material.GHAST_TEAR, 2),
			ItemStack(Material.MAGMA_CREAM, 3),
		).random())

		/* need gunpowder to brew */
		if (brewable && gunpowder < 3) potentials.add(ItemStack(Material.GUNPOWDER))

		/* need lapis to enchant */
		if (phase >= PhaseType.SHRINK && lapis < 16) potentials.add(ItemStack(Material.LAPIS_LAZULI, 4))

		/* have a saddle */
		if (phase <= PhaseType.SHRINK && saddle == 0) potentials.add(ItemStack(Material.SADDLE))

		/* ores */
		when (phase) {
			PhaseType.GRACE -> {
				potentials.add(ItemStack(Material.IRON_INGOT))
				potentials.add(ItemStack(Material.EMERALD))

				potentials.add(ItemStack(Material.SUGAR_CANE, 3))
				potentials.add(ItemStack(Material.LEATHER))
			}
			PhaseType.SHRINK -> {
				potentials.add(ItemStack(Material.IRON_INGOT, 3))
				potentials.add(ItemStack(Material.EMERALD, 2))

				if (diamond < 8) potentials.add(ItemStack(Material.DIAMOND))
			}
			PhaseType.BATTLEGROUND -> {
				potentials.add(ItemStack(Material.IRON_INGOT, 4))
				potentials.add(ItemStack(Material.EMERALD, 3))

				if (diamond < 14) potentials.add(ItemStack(Material.DIAMOND))
			}
			else -> {
				potentials.add(ItemStack(Material.DIAMOND))
			}
		}

		/* health */
		if (phase >= PhaseType.SHRINK) when {
			healthBracket <= 0.10 -> potentials.add(ItemStack(Material.GOLDEN_APPLE, 2))
			healthBracket <= 0.25 -> potentials.add(ItemStack(Material.GOLDEN_APPLE))
			healthBracket <= 0.5 -> potentials.add(CarePackageUtil.regenerationStew())
		}

		return potentials.take((if (phase === PhaseType.GRACE) 2 else if (teammateDown) 4 else 3) + luck)
	}
}
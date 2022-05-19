package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Material
import org.gaseumlabs.uhc.world.regenresource.type.ResourceMelon
import org.gaseumlabs.uhc.world.regenresource.type.ResourceOre
import org.gaseumlabs.uhc.world.regenresource.type.ResourceSugarCane
import org.gaseumlabs.uhc.world.regenresource.type.ResourceLeather
import org.gaseumlabs.uhc.world.regenresource.type.ResourceBlaze
import org.gaseumlabs.uhc.world.regenresource.type.ResourceNetherWart
import org.gaseumlabs.uhc.world.WorldManager
import kotlin.collections.hashMapOf
import org.gaseumlabs.uhc.core.phase.PhaseType

enum class RegenResource(createDescription: () -> ResourceDescription) {
	MELON({
		ResourceMelon(
			hashMapOf(PhaseType.GRACE to 2, PhaseType.SHRINK to 2, PhaseType.ENDGAME to 2),
			2,
			10,
			"Melon"
		)
	}),
	SUGAR_CANE({
		ResourceSugarCane(
			hashMapOf(PhaseType.GRACE to 14, PhaseType.SHRINK to 34, PhaseType.ENDGAME to 4),
			10,
			10,
			"Sugar cane"
		)
	}),
	LEATHER({
		ResourceLeather(
			hashMapOf(PhaseType.GRACE to 14, PhaseType.SHRINK to 34, PhaseType.ENDGAME to 4),
			10,
			10,
			"Leather"
		)
	}),
	BLAZE({
		ResourceBlaze(
			hashMapOf(PhaseType.GRACE to 2, PhaseType.SHRINK to 4, PhaseType.ENDGAME to 0),
			4,
			30,
			"Blaze"
		)
	}),
	NETHER_WART({
		ResourceNetherWart(
			hashMapOf(PhaseType.GRACE to 3, PhaseType.SHRINK to 7, PhaseType.ENDGAME to 0),
			8,
			10,
			"Nether wart"
		)
	}),
	DIAMOND({
		ResourceOre(
			Material.DIAMOND_ORE,
			Material.DEEPSLATE_DIAMOND_ORE,
			3,
			{ y -> y in -54..0 },
			WorldManager.GAME_WORLD_NAME,

			hashMapOf(PhaseType.GRACE to 4, PhaseType.SHRINK to 8, PhaseType.ENDGAME to 4),
			8,
			10,
			"Diamond"
		)
	}),
	GOLD({
		ResourceOre(
			Material.GOLD_ORE,
			Material.DEEPSLATE_GOLD_ORE,
			5,
			{ y -> y in -54..32 || y in 90..150 },
			WorldManager.GAME_WORLD_NAME,

			hashMapOf(PhaseType.GRACE to -1, PhaseType.SHRINK to -1, PhaseType.ENDGAME to -1),
			12,
			10,
			"Gold"
		)
	}),
	EMERALD({
		ResourceOre(
			Material.EMERALD_ORE,
			Material.DEEPSLATE_EMERALD_ORE,
			1,
			{ y -> y in -54..48 || y in 76..150 },
			WorldManager.GAME_WORLD_NAME,

			hashMapOf(PhaseType.GRACE to -1, PhaseType.SHRINK to -1, PhaseType.ENDGAME to -1),
			10,
			10,
			"Emerald"
		)
	}),
	ANCIENT_DEBRIS({
		ResourceOre(
			Material.ANCIENT_DEBRIS,
			Material.ANCIENT_DEBRIS,
			2,
			{ y -> y in 32..112 },
			WorldManager.NETHER_WORLD_NAME,

			hashMapOf(PhaseType.GRACE to 4, PhaseType.SHRINK to 8, PhaseType.ENDGAME to 4),
			8,
			10,
			"Ancient debris"
		)
	});

	val description = createDescription()

	init {
		description.regenResource = this
	}
}

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
			3,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 9.0f,
			"Melon"
		)
	}),
	SUGAR_CANE({
		ResourceSugarCane(
			hashMapOf(PhaseType.GRACE to 14, PhaseType.SHRINK to 34, PhaseType.ENDGAME to 4),
			4,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 4.0f,
			"Sugar cane"
		)
	}),
	LEATHER({
		ResourceLeather(
			hashMapOf(PhaseType.GRACE to 14, PhaseType.SHRINK to 34, PhaseType.ENDGAME to 4),
			4,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 7.0f,
			"Leather"
		)
	}),
	BLAZE({
		ResourceBlaze(
			hashMapOf(PhaseType.GRACE to 2, PhaseType.SHRINK to 4, PhaseType.ENDGAME to 0),
			4,
			WorldManager.NETHER_WORLD_NAME,
			1.0f / 13.0f,
			"Blaze"
		)
	}),
	NETHER_WART({
		ResourceNetherWart(
			hashMapOf(PhaseType.GRACE to 3, PhaseType.SHRINK to 7, PhaseType.ENDGAME to 0),
			4,
			WorldManager.NETHER_WORLD_NAME,
			1.0f / 8.0f,
			"Nether wart"
		)
	}),
	DIAMOND({
		ResourceOre(
			Material.DIAMOND_ORE,
			Material.DEEPSLATE_DIAMOND_ORE,
			3,
			{ y -> RegenUtil.yRangeLinear(y, 0.0f, 1.0f, -54, 0) },

			hashMapOf(PhaseType.GRACE to 4, PhaseType.SHRINK to 8, PhaseType.ENDGAME to 4),
			4,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 6.0f,
			"Diamond"
		)
	}),
	GOLD({
		ResourceOre(
			Material.GOLD_ORE,
			Material.DEEPSLATE_GOLD_ORE,
			5,
			{ y ->
				RegenUtil.yRangeLinear(y, 0.0f, 0.8f, -54, 32) +
				RegenUtil.yRangeLinear(y, 0.8f, 1.0f, 87, 130)
			},

			hashMapOf(PhaseType.GRACE to -1, PhaseType.SHRINK to -1, PhaseType.ENDGAME to -1),
			4,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 3.0f,
			"Gold"
		)
	}),
	EMERALD({
		ResourceOre(
			Material.EMERALD_ORE,
			Material.DEEPSLATE_EMERALD_ORE,
			1,
			{ y ->
				RegenUtil.yRangeLinear(y, 0.0f, 0.7f, -54, 48) +
				RegenUtil.yRangeLinear(y, 0.7f, 1.0f, 76, 130)
			},

			hashMapOf(PhaseType.GRACE to -1, PhaseType.SHRINK to -1, PhaseType.ENDGAME to -1),
			4,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 3.0f,
			"Emerald"
		)
	}),
	ANCIENT_DEBRIS({
		ResourceOre(
			Material.ANCIENT_DEBRIS,
			Material.ANCIENT_DEBRIS,
			2,
			{ y -> RegenUtil.yRangeLinear(y, 0.0f, 1.0f, 32, 110) },

			hashMapOf(PhaseType.GRACE to 4, PhaseType.SHRINK to 8, PhaseType.ENDGAME to 4),
			4,
			WorldManager.NETHER_WORLD_NAME,
			1.0f / 6.0f,
			"Ancient debris"
		)
	});

	val description = createDescription()

	init {
		description.regenResource = this
	}
}

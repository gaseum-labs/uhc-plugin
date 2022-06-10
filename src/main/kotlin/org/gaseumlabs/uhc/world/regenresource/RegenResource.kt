package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.gaseumlabs.uhc.UHCPlugin
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
			hashMapOf(
				PhaseType.GRACE to 2, PhaseType.SHRINK to 2,
				PhaseType.BATTLEGROUND to 1, PhaseType.ENDGAME to 1,
			),
			3,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 9.0f,
			"Melon"
		)
	}),
	SUGAR_CANE({
		ResourceSugarCane(
			hashMapOf(
				PhaseType.GRACE to 16, PhaseType.SHRINK to 16,
				PhaseType.BATTLEGROUND to 5, PhaseType.ENDGAME to 5,
			),
			5,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 4.0f,
			"Sugar cane"
		)
	}),
	LEATHER({
		ResourceLeather(
			hashMapOf(
				PhaseType.GRACE to 16, PhaseType.SHRINK to 16,
				PhaseType.BATTLEGROUND to 5, PhaseType.ENDGAME to 5,
			),
			5,
			WorldManager.GAME_WORLD_NAME,
			1.0f / 10.0f,
			"Leather"
		)
	}),
	BLAZE({
		ResourceBlaze(
			hashMapOf(
				PhaseType.GRACE to 2, PhaseType.SHRINK to 2,
				PhaseType.BATTLEGROUND to 2, PhaseType.ENDGAME to 2,
			),
			4,
			WorldManager.NETHER_WORLD_NAME,
			1.0f / 13.0f,
			"Blaze"
		)
	}),
	NETHER_WART({
		ResourceNetherWart(
			hashMapOf(
				PhaseType.GRACE to 3, PhaseType.SHRINK to 3,
				PhaseType.BATTLEGROUND to 3, PhaseType.ENDGAME to 3,
			),
			4,
			WorldManager.NETHER_WORLD_NAME,
			1.0f / 9.0f,
			"Nether wart"
		)
	}),
	DIAMOND({
		ResourceOre(
			Material.DIAMOND_ORE,
			Material.DEEPSLATE_DIAMOND_ORE,
			3,
			{ y -> RegenUtil.yRangeLinear(y, 0.0f, 1.0f, -54, 0) },
			{ y -> y < 16 },

			hashMapOf(
				PhaseType.GRACE to 6, PhaseType.SHRINK to 6,
				PhaseType.BATTLEGROUND to 12, PhaseType.ENDGAME to 0,
			),
			5,
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
			{ true },

			hashMapOf(
				PhaseType.GRACE to -1, PhaseType.SHRINK to -1,
				PhaseType.BATTLEGROUND to 16, PhaseType.ENDGAME to 16,
			),
			5,
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
			{ true },

			hashMapOf(
				PhaseType.GRACE to -1, PhaseType.SHRINK to -1,
				PhaseType.BATTLEGROUND to 16, PhaseType.ENDGAME to 16,
			),
			5,
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
			{ true },

			hashMapOf(
				PhaseType.GRACE to -1, PhaseType.SHRINK to -1,
				PhaseType.BATTLEGROUND to -1, PhaseType.ENDGAME to -1,
			),
			5,
			WorldManager.NETHER_WORLD_NAME,
			1.0f / 4.0f,
			"Ancient debris"
		)
	});

	val description = createDescription()

	val chunkKey = NamespacedKey(UHCPlugin.plugin, name)

	init {
		description.regenResource = this
	}
}

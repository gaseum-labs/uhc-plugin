package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Material
import org.gaseumlabs.uhc.world.regenresource.type.ResourceMelon
import org.gaseumlabs.uhc.world.regenresource.type.ResourceOre
import org.gaseumlabs.uhc.world.regenresource.type.ResourceSugarCane
import org.gaseumlabs.uhc.world.regenresource.type.ResourceLeather
import org.gaseumlabs.uhc.world.regenresource.type.ResourceBlaze
import org.gaseumlabs.uhc.world.regenresource.type.ResourceNetherWart
import org.gaseumlabs.uhc.world.WorldManager

enum class RegenResource(createDescription: () -> ResourceDescription) {
	MELON({ ResourceMelon() }),
	SUGAR_CANE({
		ResourceSugarCane()
	}),
	LEATHER({
		ResourceLeather()
	}),
	BLAZE({
		ResourceBlaze()
	}),
	NETHER_WART({
		ResourceNetherWart()
	}),
	DIAMOND({
		ResourceOre(
			Material.DIAMOND_ORE,
			Material.DEEPSLATE_DIAMOND_ORE,
			3,
			-54..0,
			WorldManager.GAME_WORLD_NAME,

			1, 12, 6, 10 * 20
		)
	}),
	GOLD({
		ResourceOre(
			Material.GOLD_ORE,
			Material.DEEPSLATE_GOLD_ORE,
			5,
			-54..32,
			WorldManager.GAME_WORLD_NAME,

			3, 26, 6, 10 * 20
		)
	}),
	LAPIS({
		ResourceOre(
			Material.LAPIS_ORE,
			Material.DEEPSLATE_LAPIS_ORE,
			4,
			-54..32,
			WorldManager.GAME_WORLD_NAME,

			1, 8, 6, 10 * 20
		)
	}),
	EMERALD({
		ResourceOre(
			Material.EMERALD_ORE,
			Material.DEEPSLATE_EMERALD_ORE,
			1,
			-54..128,
			WorldManager.GAME_WORLD_NAME,

			6, 21, 6, 10 * 20
		)
	}),
	ANCIENT_DEBRIS({
		ResourceOre(
			Material.ANCIENT_DEBRIS,
			Material.ANCIENT_DEBRIS,
			2,
			12..109,
			WorldManager.NETHER_WORLD_NAME,

			1, 8, 6, 10 * 20
		)
	});

	val description = createDescription()

	init {
		description.regenResource = this
	}
}

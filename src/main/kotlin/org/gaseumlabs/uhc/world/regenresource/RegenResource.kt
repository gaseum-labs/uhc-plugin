package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Material
import org.gaseumlabs.uhc.world.regenresource.type.ResourceMelon
import org.gaseumlabs.uhc.world.regenresource.type.ResourceOre
import org.gaseumlabs.uhc.world.regenresource.type.ResourceSugarCane
import org.gaseumlabs.uhc.world.regenresource.type.ResourceLeather

enum class RegenResource(createDescription: () -> ResourceDescription) {
	MELON({ ResourceMelon() }),
	SUGAR_CANE({
		ResourceSugarCane()
	}),
	LEATHER({
		ResourceLeather()
	}),
	DIAMOND({
		ResourceOre(
			Material.DIAMOND_ORE,
			Material.DEEPSLATE_DIAMOND_ORE,
			3,
			-54..0,

			3, 20, 4, 10 * 20
		)
	}),
	GOLD({
		ResourceOre(
			Material.GOLD_ORE,
			Material.DEEPSLATE_GOLD_ORE,
			5,
			-54..32,

			3, 20, 4, 10 * 20
		)
	}),
	LAPIS({
		ResourceOre(
			Material.LAPIS_ORE,
			Material.DEEPSLATE_LAPIS_ORE,
			4,
			-54..32,

			3, 20, 4, 10 * 20
		)
	}),
	EMERALD({
		ResourceOre(
			Material.EMERALD_ORE,
			Material.DEEPSLATE_EMERALD_ORE,
			1,
			-54..48,

			3, 20, 4, 10 * 20
		)
	});

	val description = createDescription()

	init {
		description.regenResource = this
	}
}

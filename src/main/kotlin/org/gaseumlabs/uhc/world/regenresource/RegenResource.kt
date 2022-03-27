package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Material
import org.gaseumlabs.uhc.world.regenresource.type.ResourceMelon
import org.gaseumlabs.uhc.world.regenresource.type.ResourceOre

enum class RegenResource(createDescription: () -> ResourceDescription) {
	MELON({ ResourceMelon() }),
	DIAMOND({ ResourceOre(
		Material.DIAMOND_ORE,
		Material.DEEPSLATE_DIAMOND_ORE,
		3,
		-54..0,
		10.0f, 8.0f, 90.0f,
		12
	)});

	val description = createDescription()
	init { description.regenResource = this }
}

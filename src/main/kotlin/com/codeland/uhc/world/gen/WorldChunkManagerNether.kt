package com.codeland.uhc.world.gen

import net.minecraft.core.IRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld

class WorldChunkManagerNether(
	val seed: Long,
	var4: IRegistry<BiomeBase>
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val genLayerField = WorldChunkManagerOverworld::class.java.getDeclaredField("f")

	init {
		genLayerField.isAccessible = true
		genLayerField[this] = CustomGenLayers.createGenLayerNether(seed)
	}
}

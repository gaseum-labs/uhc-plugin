package com.codeland.uhc.world.gen

import net.minecraft.server.v1_16_R3.BiomeBase
import net.minecraft.server.v1_16_R3.IRegistry
import net.minecraft.server.v1_16_R3.WorldChunkManagerOverworld

class WorldChunkManagerNether(
	val seed: Long,
	private val var4: IRegistry<BiomeBase>
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val genLayerField = WorldChunkManagerOverworld::class.java.getDeclaredField("f")

	init {
		genLayerField.isAccessible = true
		genLayerField[this] = CustomGenLayers.createGenLayerNether(seed)
	}
}

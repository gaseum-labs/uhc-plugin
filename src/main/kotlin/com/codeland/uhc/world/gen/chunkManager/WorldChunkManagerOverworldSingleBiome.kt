package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.BiomeNo
import net.minecraft.core.IRegistry
import net.minecraft.world.level.biome.*

class WorldChunkManagerOverworldSingleBiome(
	val seed: Long,
	private val var4: IRegistry<BiomeBase>,
	singleBiomeNo: Int,
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val singleBiome = BiomeNo.fromId(singleBiomeNo)!!

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		var4.d(Biomes.B)
		return singleBiome
	}
}

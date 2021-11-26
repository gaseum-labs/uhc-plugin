package com.codeland.uhc.world.gen.chunkManager

import com.codeland.uhc.world.gen.CustomGenLayers
import com.codeland.uhc.world.gen.FeatureBiomes
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import kotlin.math.abs

class WorldChunkManagerOverworldGame(
	val seed: Long,
	private val var4: IRegistry<BiomeBase>,
	val centerBiomeNo: Int?,
	val startRadius: Int,
	val endRadius: Int,
	val features: Boolean,
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val areaLazy = CustomGenLayers.createAreaGame2(seed, startRadius)

	private val centerBiome = if (centerBiomeNo == null) null else biomeFromInt(centerBiomeNo)

	fun withFeatures(newFeatures: Boolean): WorldChunkManagerOverworldGame {
		return WorldChunkManagerOverworldGame(
			seed, var4, centerBiomeNo, startRadius, endRadius, newFeatures
		)
	}

	fun inRange(x: Int, z: Int, range: Int): Boolean {
		return abs(x) <= range / 4 && abs(z) <= range / 4
	}

	fun biomeFromInt(no: Int): BiomeBase {
		return if (features) {
			FeatureBiomes.biomes[no]!!
		} else {
			var4.d(BiomeRegistry.a(no))
		}
	}

	override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
		/* center biome area */
		return if (centerBiome != null && inRange(x, z, endRadius)) {
			centerBiome

			/* regular game area */
		} else {
			biomeFromInt(areaLazy.a(x, z))
		}
	}
}

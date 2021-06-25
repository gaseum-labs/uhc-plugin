package com.codeland.uhc.world.gen

import com.codeland.uhc.util.Util
import net.minecraft.core.IRegistry
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.WorldChunkManagerOverworld
import kotlin.math.*
import kotlin.random.Random

class WorldChunkManagerOverworldGame(
	val seed: Long,
	private val var4: IRegistry<BiomeBase>,
	centerBiome: ResourceKey<BiomeBase>?,
	val fixedJungle: Boolean,
	val startRadius: Int,
	val endRadius: Int
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val areaLazy = CustomGenLayers.createGenLayerGame(seed)

	private val centerBiome = if (centerBiome == null) null else var4.d(centerBiome)
	private val oceanBiome = var4.d(Biomes.a)
	private val beachBiome = var4.d(Biomes.q)

	/* determine jungle placement */

	data class JunglePlacement(val x: Float, val z: Float, val angleOffset: Float, val widths: Array<Float>)

	private fun genJungleWidths(): Array<Float> {
		return arrayOf(60.0f / 4, 16.0f / 4, 60.0f / 4, 12.0f / 4, 56.0f / 4)
	}

	private fun genJungles(num: Int): Array<JunglePlacement> {
		val random = Random(seed)

		val inner = (startRadius * (1.0 / 3.0)).toInt()
		val outer = (startRadius * (2.0 / 3.0)).toInt()

		val angleAdvance = Math.PI * 2 / num
		val angleOffset = random.nextFloat() * Math.PI * 2

		return Array(num) { i ->
			val radius = random.nextInt(inner, outer)
			val angle = (angleAdvance * i) + angleOffset

			JunglePlacement(
				cos(angle).toFloat() * radius / 4,
				sin(angle).toFloat() * radius / 4,
				random.nextFloat() * Math.PI.toFloat() * 2,
				genJungleWidths()
			)
		}
	}

	val jungles = genJungles(2)

	fun getJungleLevel(x: Int, z: Int): Int {
		var jungleLevel = 0

		jungles.forEach { (jx, jz, angleOffset, widths) ->
			val dist = sqrt((x - jx) * (x - jx) + (z - jz) * (z - jz))
			val angleAlong = Util.mod((atan2((z - jz), (x - jx)) + angleOffset), PI.toFloat() * 2) / (PI.toFloat() * 2)
			val width = Util.bilinear(widths, angleAlong)

			val thisLevel = when {
				dist < width -> 2
				dist < width + 4 -> 1
				else -> 0
			}

			if (thisLevel == 2)
				return thisLevel
			else if (thisLevel > jungleLevel)
				jungleLevel = thisLevel
		}

		return jungleLevel
	}

	fun inRange(x: Int, z: Int, range: Int): Boolean {
		return abs(x) <= range / 4 && abs(z) <= range / 4
	}

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
	    /* center biome area */
	    return if (centerBiome != null && inRange(x, z, endRadius)) {
	        centerBiome

	    /* regular game area */
        } else if (inRange(x, z, startRadius)) {
	        val oldBiome = areaLazy.a(x, z)

			if (oldBiome == BiomeNo.RIVER) return var4.d(BiomeRegistry.a(oldBiome))

		    var4.d(BiomeRegistry.a(when (getJungleLevel(x, z)) {
	        	2 -> when (oldBiome) {
			        BiomeNo.MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE
			        BiomeNo.GRAVELLY_MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE
			        BiomeNo.MODIFIED_GRAVELLY_MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE
			        BiomeNo.WOODED_MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE
			        BiomeNo.GIANT_TREE_TAIGA -> BiomeNo.BAMBOO_JUNGLE
			        BiomeNo.GIANT_TREE_TAIGA_HILLS -> BiomeNo.BAMBOO_JUNGLE
			        BiomeNo.GIANT_SPRUCE_TAIGA -> BiomeNo.BAMBOO_JUNGLE
			        else -> BiomeNo.JUNGLE
		        }
		        1 -> when (oldBiome) {
			        BiomeNo.MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE_EDGE
			        BiomeNo.GRAVELLY_MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE_EDGE
			        BiomeNo.MODIFIED_GRAVELLY_MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE_EDGE
			        BiomeNo.WOODED_MOUNTAINS -> BiomeNo.MODIFIED_JUNGLE_EDGE
			        else -> BiomeNo.JUNGLE_EDGE
		        }
		        else -> oldBiome
	        }))

	    /* surrounding beach */
        } else if(inRange(x, z, startRadius + 16)) {
        	beachBiome

		/* outer oceans */
	    } else {
	    	oceanBiome
	    }
    }
}

package com.codeland.uhc.world.gen

import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.*
import kotlin.math.*
import kotlin.random.Random

class WorldChunkManagerOverworldGame(
	val seed: Long,
	private val var4: IRegistry<BiomeBase>,
	val centerBiome: ResourceKey<BiomeBase>?,
	val fixedJungle: Boolean,
	val maxRadius: Int
) : WorldChunkManagerOverworld(seed, false, false, var4) {
	private val genLayerField = WorldChunkManagerOverworld::class.java.getDeclaredField("f")

	init {
		genLayerField.isAccessible = true
		//genLayerField[this] = CustomGenLayers.createGenLayer(seed, false, 2, 4, !fixedJungle)
		genLayerField[this] = CustomGenLayers.createGenLayerGame(seed)
	}

	/* determine jungle placement */

	data class JunglePlacement(val x: Float, val z: Float, val splitAngle: Float, val widths: Array<Float>)

	fun genJungleWidths(random: Random, median: Float, deviation: Float): Array<Float> {
		val numWidths = random.nextInt(5, 8) * 2

		val array = Array(numWidths) { median / 4 }
		val indices = array.indices.shuffled()

		for (i in 0 until numWidths / 2) {
			val diff = Util.interp(-deviation, deviation, random.nextFloat()) / 4

			array[indices[i * 2    ]] += diff
			array[indices[i * 2 + 1]] -= diff
		}

		return array
	}

	fun genJungles(num: Int): Array<JunglePlacement> {
		val random = Random(seed)

		val inner = (maxRadius * (1.0 / 3.0)).toInt()
		val outer = (maxRadius * (2.0 / 3.0)).toInt()

		val angleAdvance = Math.PI * 2 / num
		val angleOffset = random.nextFloat() * Math.PI * 2

		return Array(num) { i ->
			val radius = random.nextInt(inner, outer)
			val angle = (angleAdvance * i) + angleOffset

			JunglePlacement(
				cos(angle).toFloat() * radius / 4,
				sin(angle).toFloat() * radius / 4,
				random.nextFloat() * PI.toFloat() * 2.0f,
				genJungleWidths(random, 2.5f * 16.0f, 16.0f)
			)
		}
	}

	val jungles = genJungles(2)

    override fun getBiome(x: Int, y: Int, z: Int): BiomeBase {
        return if (centerBiome != null && abs(x) <= 8 && abs(z) <= 8) {
	        var4.d(centerBiome)

        } else {
        	var hilly = false
	        var jungleLevel = 0

	        jungles.any { (jx, jz, splitAngle, widths) ->
		        val dist = sqrt((x - jx) * (x - jx) + (z - jz) * (z - jz))
		        val angleAlong = atan2((z - jz), (x - jx)) / (PI.toFloat() * 2)
		        val width = Util.bilinear(widths, angleAlong)
				val side = (x - jx) * sin(splitAngle) - (z - jz) * cos(splitAngle) < 0

		        val thisLevel = when {
			        dist < width -> 2
			        dist < width + 4 -> 1
			        else -> 0
		        }

		        if (thisLevel > jungleLevel) {
		        	jungleLevel = thisLevel
			        hilly = side
		        }

		        jungleLevel == 2
	        }

	        val oldBiome = super.getBiome(x, y, z)

	        if (oldBiome === var4.d(Biomes.RIVER)) {
	        	oldBiome

	        } else when (jungleLevel) {
		        2 -> var4.d(if (hilly) Biomes.JUNGLE_HILLS else Biomes.JUNGLE)
		        1 -> var4.d(Biomes.JUNGLE_EDGE)
		        else -> oldBiome
	        }
        }
    }
}

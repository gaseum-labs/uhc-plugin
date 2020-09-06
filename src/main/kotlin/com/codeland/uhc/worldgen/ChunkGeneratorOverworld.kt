package com.codeland.uhc.worldgen

import nl.rutgerkok.worldgeneratorapi.BaseNoiseGenerator
import nl.rutgerkok.worldgeneratorapi.BaseNoiseGenerator.TerrainSettings
import nl.rutgerkok.worldgeneratorapi.BiomeGenerator
import org.bukkit.Material
import org.bukkit.block.Biome

class ChunkGeneratorOverworld(settings: OverworldGenSettings) : BaseNoiseGenerator {
	private val minLimitPerlinNoise: NoiseGeneratorOctaves
	private val maxLimitPerlinNoise: NoiseGeneratorOctaves
	private val mainPerlinNoise: NoiseGeneratorOctaves
	private val settings: OverworldGenSettings
	private val depthNoise: NoiseGeneratorOctaves
	private val biomeWeights: FloatArray
	private fun a(biome: Biome, i: Int, j: Int, k: Int, d0: Double, d1: Double, d2: Double, d3: Double, d2z: Double): Double {
		var d4 = 0.0
		var d5 = 0.0
		var d6 = 0.0
		var d7 = 1.0
		for (l in 0..15) {
			val d8 = NoiseGeneratorOctaves.a(i * d0 * d7)
			val d9 = NoiseGeneratorOctaves.a(j * d1 * d7)
			val d10 = NoiseGeneratorOctaves.a(k * d0 * d7)
			val d11 = d1 * d7
			d4 += minLimitPerlinNoise.a(l)!!.a(d8, d9, d10, d11, j * d11) / d7
			d5 += (maxLimitPerlinNoise.a(l)!!.a(d8, d9, d10, d11, j * d11)) / d7
			if (l < 8) {
				d6 += mainPerlinNoise.a(l)!!.a(NoiseGeneratorOctaves.a(i * d2 * d7),
					NoiseGeneratorOctaves.a(j * d3 * d7), NoiseGeneratorOctaves.a(k * d2z * d7), d3 * d7,
					j * d3 * d7) / d7
			}
			d7 /= 2.0
		}

		//all of these settings are biome dependent
		val dd2 = d4 / settings.lowerLimitScale
		val dd3 = d5 / settings.upperLimitScale
		val dd4 = (d6 / 10.0 + 1.0) / 2.0
		val dd5: Double
		dd5 = if (dd4 < settings.lowerLimitScaleWeight) {
			dd2
		} else if (dd4 > settings.upperLimitScaleWeight) {
			dd3
		} else {
			dd2 + (dd3 - dd2) * dd4
		}
		return dd5
	}

	private fun a(biomeGenerator: BiomeGenerator, biome: Biome, i: Int, j: Int): DoubleArray {
		val adouble = DoubleArray(2)
		var f2 = 0.0f
		var f3 = 0.0f
		var f4 = 0.0f
		for (j1 in -2..2) {
			for (k1 in -2..2) {
				val biome1 = biomeGenerator.getZoomedOutBiome(i + j1, j + k1)
				val f5 = (settings.biomeDepthOffset //base height is biome
					+ settings.baseHeight * settings.biomeDepthWeight)
				val f6 = (settings.biomeScaleOffset //height variation
					+ settings.heightVariation * settings.biomeScaleWeight)
				var f7 = biomeWeights[j1 + 2 + (k1 + 2) * 5] / (f5 + 2.0f)

				//these two are biomes
				if (settings.baseHeight > settings.baseHeight) {
					f7 /= 2.0f
				}
				f2 += f6 * f7
				f3 += f5 * f7
				f4 += f7
			}
		}
		f2 = f2 / f4
		f3 = f3 / f4
		f2 = f2 * 0.9f + 0.1f
		f3 = (f3 * 4.0f - 1.0f) / 8.0f
		adouble[0] = f3 + c(i, j)
		adouble[1] = f2.toDouble()
		return adouble
	}

	protected fun a(biomeGenerator: BiomeGenerator, adouble: DoubleArray, i: Int, j: Int, d0: Double,
					d1: Double,
					d2: Double, d3: Double, d2z: Double, k: Int, l: Int) {

		val biome = biomeGenerator.getZoomedOutBiome(i, j)
		val adouble2 = this.a(biomeGenerator, biome, i, j)
		val d4 = adouble2[0]
		val d5 = adouble2[1]
		val d6 = g()
		val d7 = h()
		for (i2 in 0 until i()) {
			var d8 = this.a(biome, i, i2, j, d0, d1, d2, d3, d2z)
			d8 -= this.a(d4, d5, i2)
			if (i2 > d6) {
				d8 = MathHelper.b(d8, l.toDouble(), (i2 - d6) / k)
			} else if (i2 < d7) {
				d8 = MathHelper.b(d8, -30.0, (d7 - i2) / (d7 - 1.0))
			}
			adouble[i2] = d8
		}
	}

	private fun a(d0: Double, d1: Double, i: Int): Double {
		val d2 = settings.baseSize.toDouble()
		var d3 = (i - (d2 + d0 * d2 / 8.0 * 4.0)) * settings.stretchY * 128.0 / 256.0 / d1
		if (d3 < 0.0) {
			d3 *= 4.0
		}
		return d3
	}

	private fun c(i: Int, j: Int): Double {
		var d0 = depthNoise.a(i * settings.depthNoiseScaleX.toDouble(), 10.0,
			j * settings.depthNoiseScaleZ.toDouble(), 1.0, 0.0, true) / 8000.0
		if (d0 < 0.0) {
			d0 = -d0 * 0.3
		}
		d0 = d0 * 3.0 - 2.0
		if (d0 < 0.0) {
			d0 /= 28.0
		} else {
			if (d0 > 1.0) {
				d0 = 1.0
			}
			d0 /= 40.0
		}
		return d0
	}

	protected fun g(): Double {
		return (i() - 4).toDouble()
	}

	override fun getNoise(biomeGenerator: BiomeGenerator, buffer: DoubleArray, x: Int, z: Int) {
		val d0 = settings.coordinateScale.toDouble()
		val d2 = settings.heightScale.toDouble()
		val d3 = settings.coordinateScale / settings.mainNoiseScaleX.toDouble()
		val d4 = settings.heightScale / settings.mainNoiseScaleY.toDouble()
		val d5 = settings.coordinateScale / settings.mainNoiseScaleZ.toDouble()
		this.a(biomeGenerator, buffer, x, z, d0, d2, d3, d4, d5, 3, -10)
	}

	override fun getTerrainSettings(): TerrainSettings {
		val settings = TerrainSettings()
		settings.stoneBlock = Material.STONE.createBlockData()
		settings.waterBlock = Material.WATER.createBlockData()
		settings.seaLevel = this.settings.seaLevel
		return settings
	}

	protected fun h(): Double {
		return 0.0
	}

	private fun i(): Int {
		return 33
	}

	init {
		val sharedseedrandom = SharedSeedRandom(settings.worldSeed)
		minLimitPerlinNoise = NoiseGeneratorOctaves(sharedseedrandom, 16)
		maxLimitPerlinNoise = NoiseGeneratorOctaves(sharedseedrandom, 16)
		mainPerlinNoise = NoiseGeneratorOctaves(sharedseedrandom, 8)
		NoiseGeneratorPerlin(sharedseedrandom, 4) // Kept for seed setting side-effect
		NoiseGeneratorOctaves(sharedseedrandom, 10) // Kept for seed setting side-effect
		depthNoise = NoiseGeneratorOctaves(sharedseedrandom, 16)
		biomeWeights = FloatArray(25)
		for (i in -2..2) {
			for (j in -2..2) {
				val f = 10.0f / MathHelper.sqrt(i * i + j * j + 0.2f)
				biomeWeights[i + 2 + (j + 2) * 5] = f
			}
		}
		this.settings = settings
	}
}
package com.codeland.uhc.world.gen

import com.codeland.uhc.lobbyPvp.PvpGameManager
import net.minecraft.util.MathHelper
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.WorldChunkManager
import net.minecraft.world.level.levelgen.ChunkGeneratorAbstract
import net.minecraft.world.level.levelgen.NoiseModifier
import net.minecraft.world.level.levelgen.NoiseSampler
import net.minecraft.world.level.levelgen.NoiseSettings
import net.minecraft.world.level.levelgen.synth.BlendedNoise
import net.minecraft.world.level.levelgen.synth.NoiseGenerator3Handler
import net.minecraft.world.level.levelgen.synth.NoiseGeneratorOctaves
import kotlin.math.abs

class NoiseSamplerUHC(
	val worldChunkManager: WorldChunkManager,
	val iii: Int, val jjj: Int, val kkk: Int, val noiseSettings: NoiseSettings, val blendedNoise: BlendedNoise,
	gen3: NoiseGenerator3Handler?, val octaves: NoiseGeneratorOctaves,
	val noiseModifier: NoiseModifier, val amplified: Boolean, val pvp: Boolean, val finalRadius: Int
) : NoiseSampler(
	worldChunkManager, iii, jjj, kkk,
	noiseSettings, blendedNoise,
	gen3, octaves, noiseModifier
) {
	companion object {
		const val AMPLIFIED_BASE = 1.5f
		const val AMPLIFIED_SCALE = 5.5f

		const val PVP_BASE = 0.01f
		const val PVP_SCALE = 0.16f

		const val FINAL_BASE = 0.36f
		const val FINAL_SCALE = 0.36f

		val circle = FloatArray(25)
		init {
			for (i in -2..2) for (j in -2..2) {
				circle[i + 2 + (j + 2) * 5] = 10.0f / MathHelper.c((i * i + j * j).toFloat() + 0.2f)
			}
		}

		val cField = NoiseSampler::class.java.getDeclaredField("c")
		val dField = NoiseSampler::class.java.getDeclaredField("d")
		val eField = NoiseSampler::class.java.getDeclaredField("e")
		val fField = NoiseSampler::class.java.getDeclaredField("f")
		val gField = NoiseSampler::class.java.getDeclaredField("g")
		val hField = NoiseSampler::class.java.getDeclaredField("h")
		val iField = NoiseSampler::class.java.getDeclaredField("i")
		val jField = NoiseSampler::class.java.getDeclaredField("j")
		val sField = NoiseSampler::class.java.getDeclaredField("s")

		init {
			cField.isAccessible = true
			dField.isAccessible = true
			eField.isAccessible = true
			fField.isAccessible = true
			gField.isAccessible = true
			hField.isAccessible = true
			iField.isAccessible = true
			jField.isAccessible = true
			sField.isAccessible = true
		}

		fun fromOriginal(
			original: NoiseSampler,
			worldChunkManager: WorldChunkManager,
			amplified: Boolean,
			pvp: Boolean,
			finalRadius: Int,
		): NoiseSamplerUHC {
			NoiseSamplerUHC
			return NoiseSamplerUHC(
				worldChunkManager,
				dField[original] as Int,
				eField[original] as Int,
				fField[original] as Int,
				gField[original] as NoiseSettings,
				hField[original] as BlendedNoise,
				iField[original] as NoiseGenerator3Handler?,
				jField[original] as NoiseGeneratorOctaves,
				sField[original] as NoiseModifier,
				amplified,
				pvp,
				finalRadius
			)
		}

		val uField = ChunkGeneratorAbstract::class.java.getDeclaredField("u")
		init { uField.isAccessible = true }

		fun inject(
			chunkGeneratorAbstract: ChunkGeneratorAbstract,
			worldChunkManager: WorldChunkManager,
			amplified: Boolean, pvp: Boolean, finalRadius: Int
		): NoiseSamplerUHC {
			NoiseSamplerUHC
			val original = uField[chunkGeneratorAbstract] as NoiseSampler

			val noiseSamplerUHC = fromOriginal(original, worldChunkManager, amplified, pvp, finalRadius)
			uField[chunkGeneratorAbstract] = noiseSamplerUHC

			return noiseSamplerUHC
		}
	}

	fun getBase(biomeBase: BiomeBase, x: Int, z: Int): Float {
		if (pvp) return if (PvpGameManager.onEdge(x * 4, z * 4)) {
			-0.9f
		} else {
			PVP_BASE
		}

		val originalBase = biomeBase.h()

		return if (amplified) {
			if (originalBase < 0)
				originalBase
			else
				AMPLIFIED_BASE
		} else {
			if (abs(x) <= finalRadius / 4 && abs(z) <= finalRadius / 4)
				FINAL_BASE
			else
				biomeBase.h()
		}
	}

	fun getScale(biomeBase: BiomeBase, base: Float, x: Int, z: Int): Float {
		if (pvp) return if (PvpGameManager.onEdge(x * 4, z * 4)) {
			0.0f
		} else {
			PVP_SCALE
		}

		if (base > 0 && amplified) return AMPLIFIED_SCALE

		if (abs(x) <= finalRadius && abs(z) <= finalRadius) return FINAL_BASE

		return biomeBase.j()
	}

	override fun a(adouble: DoubleArray, i: Int, j: Int, noisesettings: NoiseSettings, k: Int, l: Int, i1: Int) {
		var d0: Double
		var d1: Double
		var d2: Double
		var f = 0.0f
		var f1 = 0.0f
		var f2 = 0.0f
		val originBase = getBase(worldChunkManager.getBiome(i, k, j), i, j)

		for (k1 in -2..2) {
			for (l1 in -2..2) {
				val x = i + k1
				val z = j + l1

				val biome = worldChunkManager.getBiome(x, k, z)

				val otherBase = getBase(biome, x, z).coerceAtLeast(-1.8f)
				val otherScale = getScale(biome, otherBase, x, z)

				val f8 = if (otherBase > originBase) 0.5f else 1.0f
				val f9 = f8 * circle[k1 + 2 + (l1 + 2) * 5] / (otherBase + 2.0f)
				f += otherScale * f9
				f1 += otherBase * f9
				f2 += f9
			}
		}

		val f10 = f1 / f2
		val f11 = f / f2
		d2 = (f10 * 0.5f - 0.125f).toDouble()
		val d3 = (f11 * 0.9f + 0.1f).toDouble()
		d0 = d2 * 0.265625
		d1 = 96.0 / d3

		val LOWER_LIMIT = -0.05

		if ((pvp || amplified) && originBase > 0) {
			if (d0 < LOWER_LIMIT) d0 = LOWER_LIMIT
			if (d1 < LOWER_LIMIT) d1 = LOWER_LIMIT
		}
		d2 = 0.0

		var xzScale = noisesettings.c().a()
		if (pvp) xzScale *= 2.0 else if (amplified) xzScale /= 4.0

		var xzFactor = noisesettings.c().c()
		if (pvp) xzFactor *= 2.0 else if (amplified) xzFactor /= 4.0

		val d4 = 684.412 * xzScale
		val d5 = 684.412 * noisesettings.c().b()
		val d6 = d4 / xzFactor
		val d7 = d5 / noisesettings.c().d()

		//d2 = if (noisesettings.k()) supplementalNoise(i, j) else 0.0

		for (i2 in 0..i1) {
			val j2 = i2 + l
			val d8 = blendedNoise.a(i, j2, j, d4, d5, d6, d7)

			var d9 = this.supplementalNoise2(j2, d0, d1, d2) + d8
			d9 = noiseModifier.modifyNoise(d9, j2 * jjj, j * iii, i * iii)
			this.supplementalNoise3(d9, j2)

			adouble[i2] = d9
		}
	}

	fun supplementalNoise(i: Int, j: Int): Double {
		val d0 = octaves.a((i * 200).toDouble(), 10.0, (j * 200).toDouble(), 1.0, 0.0, true)
		val d1: Double = if (d0 < 0.0) -d0 * 0.3 else d0
		val d2 = d1 * 24.575625 - 2.0
		return if (d2 < 0.0) d2 * 0.009486607142857142 else d2.coerceAtMost(1.0) * 0.006640625
	}

	private fun supplementalNoise2(i: Int, d0: Double, d1: Double, d2: Double): Double {
		val d3 = 1.0 - i.toDouble() * 2.0 / 32.0 + d2
		val d4 = d3 * noiseSettings.h() + noiseSettings.i()
		val d5 = (d4 + d0) * d1
		return d5 * (if (d5 > 0.0) 4 else 1).toDouble()
	}

	private fun supplementalNoise3(d0: Double, i: Int): Double {
		val l = noiseSettings.d().b().toDouble()
		val m = noiseSettings.d().c().toDouble()
		val k2 = noiseSettings.d().a().toDouble()
		val o = noiseSettings.e().b().toDouble()
		val n = noiseSettings.e().a().toDouble()
		val p = noiseSettings.e().c().toDouble()

		var d0 = d0
		val j = MathHelper.a(noiseSettings.a(), jjj)
		val k = i - j
		var d1: Double

		if (l > 0.0) {
			d1 = ((kkk - k).toDouble() - m) / l
			d0 = MathHelper.b(k2, d0, d1)
		}

		if (o > 0.0) {
			d1 = (k.toDouble() - p) / o
			d0 = MathHelper.b(n, d0, d1)
		}

		return d0
	}
}

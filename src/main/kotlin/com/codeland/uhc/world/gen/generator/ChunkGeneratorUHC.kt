package com.codeland.uhc.world.gen.generator

import com.mojang.serialization.Codec
import net.minecraft.server.v1_16_R3.*
import java.util.function.Supplier
import java.util.stream.IntStream
import kotlin.math.sqrt

class ChunkGeneratorUHC(
	worldChunkManager: WorldChunkManager,
	structureSettings: StructureSettings,
	val seed: Long,
	val old: ChunkGeneratorAbstract
) : ChunkGenerator(worldChunkManager, structureSettings) {
	companion object {
		val supplierField = ChunkGeneratorAbstract::class.java.getDeclaredField("h")

		val jj = FloatArray(25)

		init {
			supplierField.isAccessible = true

			for (i in -2..2) for (j in -2..2) {
				jj[i + 2 + (j + 2) * 5] = 10.0f / sqrt((i * i + j * j) + 0.2f)
			}
		}

	}

	/* noise variables (magic) */
	val generatorSettings: GeneratorSettingBase = (supplierField[old] as Supplier<GeneratorSettingBase>).get()
	val l: Int
	val m: Int
	val n: Int
	val o: Int
	val p: Int

	val stoneBlock = generatorSettings.c()
	val liquidBlock = generatorSettings.d()

	val random = SeededRandom(seed)
	val noise0 = NoiseGeneratorOctaves(random, IntStream.rangeClosed(-15, 0))
	val noise1 = NoiseGeneratorOctaves(random, IntStream.rangeClosed(-15, 0))
	val noise2 = NoiseGeneratorOctaves(random, IntStream.rangeClosed(-7, 0))

	init {
		val noiseSettings = generatorSettings.b()

		l = noiseSettings.f() * 4
		m = noiseSettings.e() * 4
		n = 16 / m
		o = noiseSettings.a() / l
		p = 16 / m
	}

	/* manual override (not delegated) */
	override fun a(x: Int, z: Int): IBlockAccess {
		val blockDataList = arrayOfNulls<IBlockData>(o * l)
		this.a(x, z, blockDataList)
		return BlockColumn(blockDataList)
	}

	private fun a(i: Int, j: Int, blockDataList: Array<IBlockData?>): Int {
		val k = Math.floorDiv(i, this.m)
		val l = Math.floorDiv(j, this.m)
		val i1 = Math.floorMod(i, this.m)
		val j1 = Math.floorMod(j, this.m)
		val d0 = i1.toDouble() / this.m
		val d1 = j1.toDouble() / this.m

		val adouble = arrayOf(b(k, l), b(k, l + 1), b(k + 1, l), b(k + 1, l + 1))

		for (k1 in this.o - 1 downTo 0) {
			val d2 = adouble[0][k1]
			val d3 = adouble[1][k1]
			val d4 = adouble[2][k1]
			val d5 = adouble[3][k1]
			val d6 = adouble[0][k1 + 1]
			val d7 = adouble[1][k1 + 1]
			val d8 = adouble[2][k1 + 1]
			val d9 = adouble[3][k1 + 1]

			for (l1 in l - 1 downTo 0) {
				val d10 = l1.toDouble() / l.toDouble()
				val d11 = MathHelper.a(d10, d0, d1, d2, d6, d4, d8, d3, d7, d5, d9)
				val i2: Int = k1 * l + l1

				blockDataList[i2] = this.a(d11, i2)
			}
		}

		return 0
	}

	private fun a(d0: Double, i: Int): IBlockData {
		return when {
			d0 > 0.0 -> stoneBlock
			i < seaLevel -> liquidBlock
			else -> Blocks.AIR.blockData
		}
	}

	private fun b(i: Int, j: Int): DoubleArray {
		val adouble = DoubleArray(o + 1)
		this.a(adouble, i, j)
		return adouble
	}

	private fun a(adouble: DoubleArray, i: Int, j: Int) {
		val noiseSettings = generatorSettings.b()
		val d0: Double
		val d1: Double
		var d2: Double
		var d3: Double

		var f = 0.0f
		var f1 = 0.0f
		var f2 = 0.0f
		val flag = true
		val k = this.seaLevel
		val f3 = b.getBiome(i, k, j).h()
		for (l in -2..2) {
			for (i1 in -2..2) {
				val biomebase = b.getBiome(i + l, k, j + i1)
				val f4 = biomebase.h()
				val f5 = biomebase.j()
				var f6: Float
				var f7: Float
				if (noiseSettings.l() && f4 > 0.0f) {
					f6 = 1.0f + f4 * 2.0f
					f7 = 1.0f + f5 * 4.0f
				} else {
					f6 = f4
					f7 = f5
				}
				if (f6 < -1.8f) {
					f6 = -1.8f
				}
				val f8 = if (f4 > f3) 0.5f else 1.0f
				val f9 = f8 * jj[l + 2 + (i1 + 2) * 5] / (f6 + 2.0f)
				f += f7 * f9
				f1 += f6 * f9
				f2 += f9
			}
		}
		val f10 = f1 / f2
		val f11 = f / f2
		d2 = (f10 * 0.5f - 0.125f).toDouble()
		d3 = (f11 * 0.9f + 0.1f).toDouble()
		d0 = d2 * 0.265625
		d1 = 96.0 / d3

		val d4 = 684.412 * noiseSettings.b().a()
		val d5 = 684.412 * noiseSettings.b().b()
		val d6 = d4 / noiseSettings.b().c()
		val d7 = d5 / noiseSettings.b().d()
		d2 = noiseSettings.c().a().toDouble()
		d3 = noiseSettings.c().b().toDouble()
		val d8 = noiseSettings.c().c().toDouble()
		val d9 = noiseSettings.d().a().toDouble()
		val d10 = noiseSettings.d().b().toDouble()
		val d11 = noiseSettings.d().c().toDouble()
		//val d12 = 0.0 /* if (noiseSettings.j()) c(i, j).toDouble() else 0.0 */
		val d13 = noiseSettings.g()
		val d14 = noiseSettings.h()
		for (j1 in 0..o) {
			var d15: Double = this.a(i, j1, j, d4, d5, d6, d7)
			val d16 = 1.0 - j1.toDouble() * 2.0 / o.toDouble()// + d12
			val d17 = d16 * d13 + d14
			val d18 = (d17 + d0) * d1
			d15 += if (d18 > 0.0) {
				d18 * 4.0
			} else {
				d18
			}
			var d19: Double
			if (d3 > 0.0) {
				d19 = ((o - j1) as Double - d8) / d3
				d15 = MathHelper.b(d2, d15, d19)
			}
			if (d10 > 0.0) {
				d19 = (j1.toDouble() - d11) / d10
				d15 = MathHelper.b(d9, d15, d19)
			}
			adouble[j1] = d15
		}
	}

	private fun a(i: Int, j: Int, k: Int, d0: Double, d1: Double, d2: Double, d3: Double): Double {
		var d4 = 0.0
		var d5 = 0.0
		var d6 = 0.0
		var d7 = 1.0

		for (l in 0..15) {
			val d8 = NoiseGeneratorOctaves.a(i.toDouble() * d0 * d7)
			val d9 = NoiseGeneratorOctaves.a(j.toDouble() * d1 * d7)
			val d10 = NoiseGeneratorOctaves.a(k.toDouble() * d0 * d7)
			val d11 = d1 * d7

			val noisegeneratorperlin = this.noise0.a(l)
			if (noisegeneratorperlin != null) {
				d4 += noisegeneratorperlin.a(d8, d9, d10, d11, j.toDouble() * d11) / d7
			}

			val noisegeneratorperlin1 = this.noise1.a(l)
			if (noisegeneratorperlin1 != null) {
				d5 += noisegeneratorperlin1.a(d8, d9, d10, d11, j.toDouble() * d11) / d7
			}

			if (l < 8) {
				val noisegeneratorperlin2 = this.noise2.a(l)
				if (noisegeneratorperlin2 != null) {
					d6 += noisegeneratorperlin2.a(NoiseGeneratorOctaves.a(i.toDouble() * d2 * d7), NoiseGeneratorOctaves.a(j.toDouble() * d3 * d7), NoiseGeneratorOctaves.a(k.toDouble() * d2 * d7), d3 * d7, j.toDouble() * d3 * d7) / d7
				}
			}

			d7 /= 2.0
		}

		return MathHelper.b(d4 / 512.0, d5 / 512.0, (d6 / 10.0 + 1.0) / 2.0)
	}

	/* delegated to ChunkGeneratorAbstract */
	override fun a(): Codec<out ChunkGenerator> {
		return ChunkGeneratorAbstract.d
	}

	/* delegated to ChunkGeneratorAbstract */
	override fun buildBase(p0: RegionLimitedWorldAccess, p1: IChunkAccess) {
		return old.buildBase(p0, p1)
	}

	/* delegated to ChunkGeneratorAbstract */
	override fun buildNoise(p0: GeneratorAccess, p1: StructureManager, p2: IChunkAccess) {
		return old.buildNoise(p0, p1, p2)
	}

	/* delegated to ChunkGeneratorAbstract */
	override fun getBaseHeight(p0: Int, p1: Int, p2: HeightMap.Type): Int {
		return old.getBaseHeight(p0, p1, p2)
	}

}

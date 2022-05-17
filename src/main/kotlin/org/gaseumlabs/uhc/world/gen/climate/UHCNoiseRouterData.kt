package org.gaseumlabs.uhc.world.gen.climate

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction
import net.minecraft.core.Holder
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext
import net.minecraft.world.level.levelgen.DensityFunction.SimpleFunction
import net.minecraft.world.level.levelgen.DensityFunctions.*
import net.minecraft.world.level.levelgen.DensityFunctions.TerrainShaperSpline.SplineType
import net.minecraft.world.level.levelgen.DensityFunctions.WeirdScaledSampler.RarityValueMapper
import net.minecraft.world.level.levelgen.DensityFunctions.WeirdScaledSampler.RarityValueMapper.CUSTOM0
import net.minecraft.world.level.levelgen.synth.BlendedNoise
import net.minecraft.world.level.levelgen.synth.NormalNoise
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters
import org.gaseumlabs.uhc.world.gen.CustomNoise

object UHCNoiseRouterData {
	private fun getNoise(resourceKey: ResourceKey<NoiseParameters>): Holder<NoiseParameters> {
		return BuiltinRegistries.NOISE.getHolderOrThrow(resourceKey)
	}

	private fun pillars(): DensityFunction {
		val d = 25.0
		val e = 0.3
		val densityFunction = noise(getNoise(Noises.PILLAR), 25.0, 0.3) //25.0, 0.3
		val densityFunction2 = mappedNoise(getNoise(Noises.PILLAR_RARENESS), 0.0, -2.0)//0.0, -2.0
		val densityFunction3 = mappedNoise(getNoise(Noises.PILLAR_THICKNESS), 0.0, 1.1)
		val densityFunction4 =
			add(
				mul(densityFunction, constant(2.0)),
				densityFunction2)
		return DensityFunctions.cacheOnce(mul(densityFunction4, densityFunction3.cube()))
	}

	private fun splineWithBlending(
		densityFunction: DensityFunction,
		densityFunction2: DensityFunction,
		densityFunction3: DensityFunction,
		splineType: SplineType,
		d: Double,
		e: Double,
		densityFunction4: DensityFunction,
	): DensityFunction {
		val densityFunction5 =
			DensityFunctions.terrainShaperSpline(densityFunction, densityFunction2, densityFunction3, splineType, d, e)
		val densityFunction6 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), densityFunction4, densityFunction5)
		return DensityFunctions.flatCache(DensityFunctions.cache2d(densityFunction6))
	}

	private fun noiseGradientDensity(
		densityFunction: DensityFunction,
		densityFunction2: DensityFunction,
	): DensityFunction {
		val densityFunction3 = mul(densityFunction2, densityFunction)
		return mul(constant(4.0), densityFunction3.quarterNegative())
	}

	private fun postProcess(noiseSettings: NoiseSettings, densityFunction: DensityFunction): DensityFunction {
		val densityFunction2 = slide(noiseSettings, densityFunction)
		val densityFunction3 = blendDensity(densityFunction2)
		return mul(interpolated(densityFunction3), constant(0.64)).squeeze()
	}

	private fun slopes(
		continents: DensityFunction,
		erosion: DensityFunction,
		ridges: DensityFunction,
		factor: DensityFunction,
		depth: DensityFunction,
		scale: DensityFunction,
	): DensityFunction {
		val blended = splineWithBlending(
			continents,
			erosion,
			ridges,
			SplineType.JAGGEDNESS,
			0.0,
			1.28,
			zero()
		)
		val scaled = mul(
			blended,
			scale.halfNegative()
		)
		val gradient = noiseGradientDensity(
			factor,
			add(depth, scaled)
		)

		return DensityFunctions.cache2d(add(gradient, BlendedNoise.UNSEEDED))
	}

	private fun uhcPillars(): DensityFunction {
		return mappedNoise(
			getNoise(Noises.PILLAR),
			50.0,
			0.3,
			-2.0,
			1.0,
		).clamp(-1.0, 1.0)

		///* pillars but blanket thicker */
		//return DensityFunctions.add(
		//	/* pillars but thinned */
		//	DensityFunctions.mul(
		//		/* pillars with pockets removed */
		//		DensityFunctions.add(
		//			/* base pillars */
		//			DensityFunctions.mul(
		//				/* long tall noise */
		//				DensityFunctions.noise(getNoise(Noises.PILLAR), 25.0, 0.3),
		//				/* which is sharpened to the range [-2.0, 2.0] */
		//				DensityFunctions.constant(2.0)
		//			),
		//			/* pockets where the pillars will be deleted (all the way up to -2.0) */
		//			DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_RARENESS), 0.0, -3.0)//-2.0
		//		).clamp(0.0, 1.0),
		//		/* pillars are multiplied from 0.0 to 1.331 but skewed heavily towards 0.0 */
		//		DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_THICKNESS), 0.0, 1.0).square()
		//	),
		//	/* add a constant to pillars */
		//	DensityFunctions.constant(0.05)//0.25
		//)
	}

	private fun uhcSpaghetti(
		low: Double,
		high: Double,
		caveHeight: Double,
		noiseIndex: Int,
		mapper: RarityValueMapper,
	): DensityFunction {
		//val baseSpaghetti = weirdScaledSampler(
		//	noise(getNoise(CustomNoise.UHC_SAPGHETTI_MODULATOR_KEYS[noiseIndex]), 2.5, 0.8), //0.9
		//	getNoise(CustomNoise.UHC_SAPGHETTI_KEYS[noiseIndex]),
		//	mapper
		//)

		val baseSpaghetti = noise(getNoise(CustomNoise.UHC_SAPGHETTI_KEYS[noiseIndex]), 1.5, 0.5) //2.5, 0.9

		val spaghettiThickness = mappedNoise(
			getNoise(CustomNoise.UHC_SAPGHETTI_THICKNESS_KEYS[noiseIndex]), 2.0, 1.0, -1.0, -1.5 //-0.6, -1.3
		)

		val elevation = add(
			mul(
				mappedNoise(
					/* makes the elevation level move up or down */
					getNoise(CustomNoise.UHC_SAPGHETTI_ELEVATION_KEYS[noiseIndex]),
					1.0,
					0.0,
					low,
					high,
				),
				constant(-1.0)
			),
			YContinualGradient(1.0, 0.0)
		).abs()

		return add(
			mul(elevation, constant(1.0 / caveHeight)).cube(),
			add(
				baseSpaghetti,
				mul(constant(0.083), spaghettiThickness)
			)
		).clamp(-1.0, 1.0)
	}

	class YContinualGradient(
		val slope: Double,
		val intercept: Double,
	) : SimpleFunction {
		override fun compute(pos: FunctionContext): Double {
			return pos.blockY() * slope + intercept
		}

		override fun minValue(): Double {
			return -10000.0
		}

		override fun maxValue(): Double {
			return 10000.0
		}

		override fun codec(): Codec<out DensityFunction> {
			return CODEC
		}

		companion object {
			val CODEC: Codec<YContinualGradient> = makeCodec(RecordCodecBuilder.mapCodec { instance ->
				instance.group(
					Codec.doubleRange(-10000.0, 10000.0)
						.fieldOf("slope").forGetter { obj -> obj.slope },
					Codec.doubleRange(-10000.0, 10000.0)
						.fieldOf("intercept").forGetter { obj -> obj.intercept },
				).apply(instance) { slope, intercept ->
					YContinualGradient(slope, intercept)
				}
			})
		}
	}

	class FullWeirdNoise(
		val baseNoise: NormalNoise,
		val scaleNoise: NormalNoise,
		val minScale: Double,
		val maxScale: Double,
	) : SimpleFunction {
		override fun compute(pos: FunctionContext): Double {
			val baseScale = scaleNoise.getValue(
				pos.blockX().toDouble(),
				pos.blockY().toDouble(),
				pos.blockZ().toDouble()
			)

			val scale = ((baseScale / (scaleNoise.maxValue() * 2.0)) + 0.5) * (maxScale - minScale) + minScale

			return baseNoise.getValue(pos.blockX() * scale, pos.blockY().toDouble(), pos.blockZ() * scale)
		}

		override fun minValue(): Double {
			return -baseNoise.maxValue()
		}

		override fun maxValue(): Double {
			return baseNoise.maxValue()
		}

		override fun codec(): Codec<out DensityFunction> {
			return CODEC
		}

		companion object {
			val CODEC: Codec<FullWeirdNoise> = makeCodec(RecordCodecBuilder.mapCodec { instance ->
				instance.group(
					Codec.doubleRange(-10000.0, 10000.0).fieldOf("scale").forGetter { obj -> obj.minScale },
				).apply(instance) { scale ->
					FullWeirdNoise(null as NormalNoise, null as NormalNoise, scale, scale)
				}
			})
		}
	}

	private fun uhcSpaghettiRoughnessFunction(): DensityFunction {
		return mul(
			mappedNoise(
				getNoise(Noises.SPAGHETTI_ROUGHNESS_MODULATOR),
				0.0,
				-0.5,//-0.1
			),
			add(
				noise(getNoise(Noises.SPAGHETTI_ROUGHNESS)).abs(),
				constant(-0.4)
			)
		)
	}

	private fun uhcCheese(): DensityFunction {
		return add(
			constant(0.05),
			noise(getNoise(Noises.CAVE_CHEESE), 2.25, 15.0),
		).clamp(-2.0, 0.5)
	}

	private fun minChain(vararg densityFunctions: DensityFunction): DensityFunction {
		if (densityFunctions.size < 2) throw Exception("Need 2 to make a chain")

		var ret = min(densityFunctions[1], densityFunctions[0])

		for (i in 2..densityFunctions.lastIndex) {
			ret = min(densityFunctions[i], ret)
		}

		return ret
	}

	private fun uhcUnderground(slopes: DensityFunction): DensityFunction {
		CUSTOM0.mapper = Double2DoubleFunction { 1.0 }
		CUSTOM0.maxRarity = 1.0

		//val spaghetties = add(
		//	minChain(
		//		uhcSpaghetti(-64.0, -48.0, 16.0, 0, CUSTOM0),
		//		uhcSpaghetti(-48.0, -32.0, 16.0, 1, CUSTOM0),
		//		uhcSpaghetti(-32.0, -16.0, 16.0, 2, CUSTOM0),
		//		uhcSpaghetti(-16.0, 0.0, 16.0, 3, CUSTOM0),
		//		uhcSpaghetti(0.0, 16.0, 16.0, 4, CUSTOM0),
		//		uhcSpaghetti(16.0, 32.0, 16.0, 5, CUSTOM0),
		//		uhcSpaghetti(32.0, 48.0, 16.0, 6, CUSTOM0),
		//		uhcSpaghetti(48.0, 64.0, 16.0, 7, CUSTOM0),
		//	),
		//	uhcSpaghettiRoughnessFunction()
		//)

		//75

		var spaghetties =
			add(
				constant(-1.0),
				mul(
					mappedNoise(getNoise(CustomNoise.UHC_SAPGHETTI_KEYS[0]), 2.0, 0.9, -1.0, 1.0).abs(),
					constant(8.0)
				)
			).clamp(-1.0, 1.0)

		val elevation = mul(
			add(
				mul(
					mappedNoise(
						/* makes the elevation level move up or down */
						getNoise(CustomNoise.UHC_SAPGHETTI_ELEVATION_KEYS[0]),
						1.0,
						0.0,
						-32.0, //low
						32.0, //high
					),
					constant(-1.0)
				),
				YContinualGradient(1.0, 0.0)
			).abs(),
			constant(1.0 / 8.0 /* cave height*/)
		).cube()

		spaghetties = add(spaghetties, elevation)

		//return uhcPillars()

		//return DensityFunctions.max(
		//	uhcPillars(),
		//	DensityFunctions.min(
		//		slopes,
		//		spaghetties
		//	)
		//)
		//return DensityFunctions.add(
		//	/* pillars minus air */
		//	DensityFunctions.add(
		//		uhcPillars(),
		//		DensityFunctions.add(slopes, DensityFunctions.constant(-0.6)).clamp(-100.0, 0.0)
		//	).clamp(0.0, 100.0),
		//	/* land minus caves */
		//	DensityFunctions.min(slopes, spaghetties)
		//)

		return min(slopes, spaghetties)
	}

	private fun customOverworldWith(
		noiseSettings: NoiseSettings,
		createCaves: (slopes: DensityFunction) -> DensityFunction,
	): NoiseRouterWithOnlyNoises {
		/* create aquifers */
		val aquiferBarrier = noise(getNoise(Noises.AQUIFER_BARRIER), 1.0, 1.0)
		val aquiferFloodedness = noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 1.0, 8.0)
		val aquiferSpread = noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 2.0, 1.0)
		val aquiferLava = noise(getNoise(Noises.AQUIFER_LAVA), 8.0, 1.0)

		/* shift */
		val shiftX = DensityFunctions.flatCache(
			DensityFunctions.cache2d(DensityFunctions.shiftA(getNoise(Noises.SHIFT)))
		)
		val shiftZ = DensityFunctions.flatCache(
			DensityFunctions.cache2d(DensityFunctions.shiftB(getNoise(Noises.SHIFT)))
		)

		/* create normal terrain */
		val temperature = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, getNoise(Noises.TEMPERATURE))
		val vegetation = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, getNoise(Noises.VEGETATION))
		val continents = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, getNoise(Noises.CONTINENTALNESS))
		val erosion = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, getNoise(Noises.EROSION))
		val ridges = DensityFunctions.shiftedNoise2d(shiftX, shiftZ, 0.25, getNoise(Noises.RIDGE))

		/* factor and depth */
		val factor = splineWithBlending(
			continents,
			erosion,
			ridges,
			SplineType.FACTOR,
			0.0,
			8.0,
			constant(10.0)
		)
		val depth = add(
			DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5),
			splineWithBlending(
				continents,
				erosion,
				ridges,
				SplineType.OFFSET,
				-0.81,
				2.5,
				blendOffset()
			)
		)

		val slopes = slopes(
			continents,
			erosion,
			ridges,
			factor,
			depth,
			noise(getNoise(Noises.JAGGED), 500.0, 0.4)
		)

		/* put it all together */
		val initialDensity = noiseGradientDensity(cache2d(factor), depth)
		val finalDensity = postProcess(noiseSettings, createCaves(slopes))

		return NoiseRouterWithOnlyNoises(
			aquiferBarrier, //barrierNoise
			zero(), //fluidLevelFloodednessNoise
			aquiferSpread, //fluidLevelSpreadNoise
			aquiferLava, //lavaNoise

			temperature, //temperature
			vegetation, //vegetation
			continents, //continents
			erosion, //erosion
			depth, //depth
			ridges, //ridges

			initialDensity, //initialDensityWithoutJaggedness
			finalDensity, //finalDensity

			zero(), //veinToggle
			zero(), //veinRidged
			zero(), //veinGap
		)
	}

	fun customOverworldGame(noiseSettings: NoiseSettings): NoiseRouterWithOnlyNoises {
		return customOverworldWith(noiseSettings, ::uhcUnderground)
	}

	fun customOverworldGameNoCaves(noiseSettings: NoiseSettings): NoiseRouterWithOnlyNoises {
		return customOverworldWith(noiseSettings) { it }
	}
}
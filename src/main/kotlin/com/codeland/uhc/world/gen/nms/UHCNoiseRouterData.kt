package net.minecraft.world.level.levelgen

import net.minecraft.core.Holder
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.levelgen.DensityFunctions.TerrainShaperSpline.SplineType
import net.minecraft.world.level.levelgen.DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1
import net.minecraft.world.level.levelgen.DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE2
import net.minecraft.world.level.levelgen.synth.BlendedNoise
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters

object UHCNoiseRouterData {
	private const val ORE_THICKNESS = 0.08f
	private const val VEININESS_FREQUENCY = 1.5
	private const val NOODLE_SPACING_AND_STRAIGHTNESS = 1.5
	private const val SURFACE_DENSITY_THRESHOLD = 1.5625

	private val BLENDING_FACTOR = DensityFunctions.constant(10.0)
	private val BLENDING_JAGGEDNESS = DensityFunctions.zero()

	private val ZERO: DensityFunction
	private val Y: DensityFunction
	private val SHIFT_X: DensityFunction
	private val SHIFT_Z: DensityFunction
	private val BASE_3D_NOISE: DensityFunction
	private val CONTINENTS: DensityFunction
	private val EROSION: DensityFunction
	private val RIDGES: DensityFunction
	private val FACTOR: DensityFunction
	private val DEPTH: DensityFunction
	private val SLOPED_CHEESE: DensityFunction
	private val CONTINENTS_LARGE: DensityFunction
	private val EROSION_LARGE: DensityFunction
	private val FACTOR_LARGE: DensityFunction
	private val DEPTH_LARGE: DensityFunction
	private val SLOPED_CHEESE_LARGE: DensityFunction
	private val SLOPED_CHEESE_END: DensityFunction
	private val SPAGHETTI_ROUGHNESS_FUNCTION: DensityFunction
	private val ENTRANCES: DensityFunction
	private val NOODLE: DensityFunction
	private val PILLARS: DensityFunction
	private val SPAGHETTI_2D_THICKNESS_MODULATOR: DensityFunction
	private val SPAGHETTI_2D: DensityFunction

	init {
		ZERO = DensityFunctions.zero()

		val i = DimensionType.MIN_Y * 2
		val j = DimensionType.MAX_Y * 2

		Y = DensityFunctions.yClampedGradient(i, j, i.toDouble(), j.toDouble())

		SHIFT_X = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(getNoise(Noises.SHIFT))))
		SHIFT_Z = DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(getNoise(Noises.SHIFT))))

		BASE_3D_NOISE = BlendedNoise.UNSEEDED

		CONTINENTS = DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(SHIFT_X,
			SHIFT_Z,
			0.25,
			getNoise(Noises.CONTINENTALNESS)))
		EROSION = DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(SHIFT_X,
			SHIFT_Z,
			0.25,
			getNoise(Noises.EROSION)))
		RIDGES =
			DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(SHIFT_X, SHIFT_Z, 0.25, getNoise(Noises.RIDGE)))

		val densityFunction6 = DensityFunctions.noise(getNoise(Noises.JAGGED), 100.0, 0.0) //1500, 0.0
		val densityFunction7 =
			splineWithBlending(CONTINENTS,
				EROSION,
				RIDGES,
				SplineType.OFFSET,
				-0.81,
				2.5,
				DensityFunctions.blendOffset())

		FACTOR = splineWithBlending(
			CONTINENTS,
			EROSION,
			RIDGES,
			SplineType.FACTOR,
			-2.0,//0.0
			64.0,//8.0
			BLENDING_FACTOR
		)
		DEPTH =
			DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 5.0, -5.0), densityFunction7)// 1.5 -1.5

		SLOPED_CHEESE = slopes(
			CONTINENTS,
			EROSION,
			RIDGES,
			FACTOR,
			DEPTH,
			densityFunction6
		)

		CONTINENTS_LARGE =
			DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(SHIFT_X,
				SHIFT_Z,
				0.25,
				getNoise(Noises.CONTINENTALNESS_LARGE)))
		EROSION_LARGE =
			DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(SHIFT_X,
				SHIFT_Z,
				0.25,
				getNoise(Noises.EROSION_LARGE)))

		val densityFunction12 = splineWithBlending(
			CONTINENTS_LARGE,
			EROSION_LARGE,
			RIDGES,
			SplineType.OFFSET,
			-0.81,
			2.5,
			DensityFunctions.blendOffset()
		)

		FACTOR_LARGE = splineWithBlending(
			CONTINENTS_LARGE,
			EROSION_LARGE,
			RIDGES,
			SplineType.FACTOR,
			-2.0,//0.0
			64.0,//8.0
			BLENDING_FACTOR)

		DEPTH_LARGE =
			DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 5.0, -5.0), densityFunction12) //1.5 -1.5

		SLOPED_CHEESE_LARGE = slopes(
			CONTINENTS_LARGE,
			EROSION_LARGE,
			RIDGES,
			FACTOR_LARGE,
			DEPTH_LARGE,
			densityFunction6
		)

		SLOPED_CHEESE_END = DensityFunctions.add(DensityFunctions.endIslands(0L), BASE_3D_NOISE)

		SPAGHETTI_ROUGHNESS_FUNCTION = spaghettiRoughnessFunction()
		SPAGHETTI_2D_THICKNESS_MODULATOR =
			DensityFunctions.cacheOnce(DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_2D_THICKNESS),
				2.0,
				1.0,
				-0.6,
				-1.3))
		SPAGHETTI_2D = spaghetti2D()
		ENTRANCES = entrances()
		NOODLE = noodle()
		PILLARS = pillars()
	}

	private fun getNoise(resourceKey: ResourceKey<NoiseParameters>): Holder<NoiseParameters> {
		return BuiltinRegistries.NOISE.getHolderOrThrow(resourceKey)
	}

	internal fun overworld(noiseSettings: NoiseSettings, bl: Boolean): NoiseRouterWithOnlyNoises {
		return overworldWithNewCaves(noiseSettings, bl)
	}

	private fun spaghettiRoughnessFunction(): DensityFunction {
		val densityFunction = DensityFunctions.noise(getNoise(Noises.SPAGHETTI_ROUGHNESS))
		val densityFunction2 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1)
		return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction2,
			DensityFunctions.add(densityFunction.abs(), DensityFunctions.constant(-0.4))))
	}

	private fun entrances(): DensityFunction {
		val densityFunction =
			DensityFunctions.cacheOnce(DensityFunctions.noise(getNoise(Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0))
		val densityFunction2 = DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_3D_THICKNESS), -0.065, -0.088)
		val densityFunction3 =
			DensityFunctions.weirdScaledSampler(densityFunction, getNoise(Noises.SPAGHETTI_3D_1), TYPE1)
		val densityFunction4 =
			DensityFunctions.weirdScaledSampler(densityFunction, getNoise(Noises.SPAGHETTI_3D_2), TYPE2)
		val densityFunction5 = DensityFunctions.add(DensityFunctions.max(densityFunction3, densityFunction4),
			densityFunction2).clamp(-1.0, 1.0)
		val densityFunction6 = SPAGHETTI_ROUGHNESS_FUNCTION
		val densityFunction7 = DensityFunctions.noise(getNoise(Noises.CAVE_ENTRANCE), 0.75, 0.5)
		val densityFunction8 =
			DensityFunctions.add(DensityFunctions.add(densityFunction7, DensityFunctions.constant(0.37)),
				DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0))
		return DensityFunctions.cacheOnce(DensityFunctions.min(densityFunction8,
			DensityFunctions.add(densityFunction6, densityFunction5)))
	}

	private fun noodle(): DensityFunction {
		val densityFunction = Y
		val i = -64
		val j = -60
		val k = 320
		val densityFunction2 =
			yLimitedInterpolatable(densityFunction,
				DensityFunctions.noise(getNoise(Noises.NOODLE), 3.0, 3.0), -60, 320, -1)//1.0, 1.0
		val densityFunction3 = yLimitedInterpolatable(densityFunction,
			DensityFunctions.mappedNoise(getNoise(Noises.NOODLE_THICKNESS),
				3.0,
				3.0,
				-0.05,
				-0.5),//1.0, 1.0, -0.05, -0.1
			-60,
			320,
			0)
		val d = 2.6666666666666665
		val densityFunction4 = yLimitedInterpolatable(densityFunction,
			DensityFunctions.noise(getNoise(Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665),
			-60,
			320,
			0)
		val densityFunction5 = yLimitedInterpolatable(densityFunction,
			DensityFunctions.noise(getNoise(Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665),
			-60,
			320,
			0)
		val densityFunction6 = DensityFunctions.mul(DensityFunctions.constant(2.5),//1.5
			DensityFunctions.max(densityFunction4.abs(), densityFunction5.abs()))
		return DensityFunctions.rangeChoice(densityFunction2,
			-1000000.0,
			0.0,
			DensityFunctions.constant(64.0),
			DensityFunctions.add(densityFunction3, densityFunction6))
	}

	private fun pillars(): DensityFunction {
		val d = 25.0
		val e = 0.3
		val densityFunction = DensityFunctions.noise(getNoise(Noises.PILLAR), 25.0, 0.3) //25.0, 0.3
		val densityFunction2 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_RARENESS), 0.0, -2.0)//0.0, -2.0
		val densityFunction3 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_THICKNESS), 0.0, 1.1)
		val densityFunction4 =
			DensityFunctions.add(DensityFunctions.mul(densityFunction, DensityFunctions.constant(2.0)),
				densityFunction2)
		return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction4, densityFunction3.cube()))
	}

	private fun spaghetti2D(): DensityFunction {
		val densityFunction = DensityFunctions.noise(getNoise(Noises.SPAGHETTI_2D_MODULATOR), 2.0, 0.1) //2.0, 1.0
		val densityFunction2 =
			DensityFunctions.weirdScaledSampler(densityFunction, getNoise(Noises.SPAGHETTI_2D), TYPE2)
		val densityFunction3 =
			DensityFunctions.mappedNoise(getNoise(Noises.SPAGHETTI_2D_ELEVATION),
				0.0,
				Math.floorDiv(-64, 8).toDouble(),
				8.0)
		val densityFunction4 = SPAGHETTI_2D_THICKNESS_MODULATOR
		val densityFunction5 = DensityFunctions.add(densityFunction3,
			DensityFunctions.yClampedGradient(-64, 320, 8.0, -40.0)).abs()
		val densityFunction6 = DensityFunctions.add(densityFunction5, densityFunction4).cube()
		val d = 0.083
		val densityFunction7 = DensityFunctions.add(densityFunction2,
			DensityFunctions.mul(DensityFunctions.constant(0.083), densityFunction4))
		return DensityFunctions.max(densityFunction7, densityFunction6).clamp(-1.0, 1.0)
	}

	private fun underground(densityFunction: DensityFunction): DensityFunction {
		val densityFunction2 = SPAGHETTI_2D
		val densityFunction3 = SPAGHETTI_ROUGHNESS_FUNCTION
		val densityFunction4 = DensityFunctions.noise(getNoise(Noises.CAVE_LAYER), 4.0) //8.0
		val densityFunction5 = DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction4.square())
		val densityFunction6 = DensityFunctions.noise(getNoise(Noises.CAVE_CHEESE), 0.2) //0.666_
		val densityFunction7 =
			DensityFunctions.add(DensityFunctions.add(DensityFunctions.constant(0.27), densityFunction6)
				.clamp(-1.0, 1.0), DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(
				DensityFunctions.constant(-0.64), densityFunction)).clamp(0.0, 0.5))
		val densityFunction8 = DensityFunctions.add(densityFunction5, densityFunction7)
		val densityFunction9 =
			DensityFunctions.min(DensityFunctions.min(densityFunction8, ENTRANCES),
				DensityFunctions.add(densityFunction2, densityFunction3))
		val densityFunction10 = PILLARS
		val densityFunction11 =
			DensityFunctions.rangeChoice(densityFunction10,
				-1000000.0,
				0.03,
				DensityFunctions.constant(-1000000.0),
				densityFunction10)
		return DensityFunctions.max(densityFunction9, densityFunction11)
	}

	private fun postProcess(noiseSettings: NoiseSettings, densityFunction: DensityFunction): DensityFunction {
		val densityFunction2 = DensityFunctions.slide(noiseSettings, densityFunction)
		val densityFunction3 = DensityFunctions.blendDensity(densityFunction2)
		return DensityFunctions.mul(DensityFunctions.interpolated(densityFunction3), DensityFunctions.constant(0.64))
			.squeeze()
	}

	private fun overworldWithNewCaves(noiseSettings: NoiseSettings, bl: Boolean): NoiseRouterWithOnlyNoises {
		val densityFunction = DensityFunctions.noise(getNoise(Noises.AQUIFER_BARRIER), 0.5)
		val densityFunction2 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67)
		val densityFunction3 = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143)
		val densityFunction4 = DensityFunctions.noise(getNoise(Noises.AQUIFER_LAVA))
		val densityFunction5 = SHIFT_X
		val densityFunction6 = SHIFT_Z
		val densityFunction7 = DensityFunctions.shiftedNoise2d(densityFunction5,
			densityFunction6,
			0.25,
			getNoise(if (bl) Noises.TEMPERATURE_LARGE else Noises.TEMPERATURE))
		val densityFunction8 = DensityFunctions.shiftedNoise2d(densityFunction5,
			densityFunction6,
			0.25,
			getNoise(if (bl) Noises.VEGETATION_LARGE else Noises.VEGETATION))
		val densityFunction9 = if (bl) FACTOR_LARGE else FACTOR
		val densityFunction10 = if (bl) DEPTH_LARGE else DEPTH
		val densityFunction11 = noiseGradientDensity(DensityFunctions.cache2d(densityFunction9), densityFunction10)
		val densityFunction12 = if (bl) SLOPED_CHEESE_LARGE else SLOPED_CHEESE
		val densityFunction13 =
			DensityFunctions.min(densityFunction12,
				DensityFunctions.mul(DensityFunctions.constant(2.5), ENTRANCES)) //5.0
		val densityFunction14 =
			DensityFunctions.rangeChoice(densityFunction12,
				-1000000.0,
				1.5625,
				densityFunction13,
				underground(densityFunction12))
		val densityFunction15 = DensityFunctions.min(postProcess(noiseSettings, densityFunction14), NOODLE)
		val densityFunction16 = Y
		val i = noiseSettings.minY()
		//val j = Stream.of(*VeinType.values()).mapToInt { veinType: VeinType -> veinType.minY }
		//	.min().orElse(i)
		//val k = Stream.of(*VeinType.values()).mapToInt { veinType: VeinType -> veinType.maxY }
		//	.max().orElse(i)
		val j = i
		val k = i
		val densityFunction17 =
			yLimitedInterpolatable(densityFunction16,
				DensityFunctions.noise(getNoise(Noises.ORE_VEININESS), 1.5, 1.5), j, k, 0)
		val f = 4.0f
		val densityFunction18 =
			yLimitedInterpolatable(densityFunction16,
				DensityFunctions.noise(getNoise(Noises.ORE_VEIN_A), 4.0, 4.0), j, k, 0).abs()
		val densityFunction19 =
			yLimitedInterpolatable(densityFunction16,
				DensityFunctions.noise(getNoise(Noises.ORE_VEIN_B), 4.0, 4.0), j, k, 0).abs()
		val densityFunction20 = DensityFunctions.add(DensityFunctions.constant(-0.08),
			DensityFunctions.max(densityFunction18, densityFunction19))
		val densityFunction21 = DensityFunctions.noise(getNoise(Noises.ORE_GAP))
		return NoiseRouterWithOnlyNoises(densityFunction,
			densityFunction2,
			densityFunction3,
			densityFunction4,
			densityFunction7,
			densityFunction8,
			if (bl) CONTINENTS_LARGE else CONTINENTS,
			if (bl) EROSION_LARGE else EROSION,
			if (bl) DEPTH_LARGE else DEPTH,
			RIDGES,
			densityFunction11,
			densityFunction15,
			densityFunction17,
			densityFunction20,
			densityFunction21)
	}

	private fun noNewCaves(noiseSettings: NoiseSettings): NoiseRouterWithOnlyNoises {
		val densityFunction = SHIFT_X
		val densityFunction2 = SHIFT_Z
		val densityFunction3 =
			DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.TEMPERATURE))
		val densityFunction4 =
			DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, getNoise(Noises.VEGETATION))
		val densityFunction5 = noiseGradientDensity(DensityFunctions.cache2d(FACTOR), DEPTH)
		val densityFunction6 = postProcess(noiseSettings, SLOPED_CHEESE)
		return NoiseRouterWithOnlyNoises(
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			densityFunction3,
			densityFunction4,
			CONTINENTS,
			EROSION,
			DEPTH,
			RIDGES,
			densityFunction5,
			densityFunction6,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero()
		)
	}

	internal fun nether(noiseSettings: NoiseSettings): NoiseRouterWithOnlyNoises {
		return noNewCaves(noiseSettings)
	}

	internal fun end(noiseSettings: NoiseSettings): NoiseRouterWithOnlyNoises {
		val densityFunction = DensityFunctions.cache2d(DensityFunctions.endIslands(0L))
		val densityFunction2 = postProcess(noiseSettings, SLOPED_CHEESE_END)
		return NoiseRouterWithOnlyNoises(DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			densityFunction,
			densityFunction2,
			DensityFunctions.zero(),
			DensityFunctions.zero(),
			DensityFunctions.zero())
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
		val densityFunction3 = DensityFunctions.mul(densityFunction2, densityFunction)
		return DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction3.quarterNegative())
	}

	private fun yLimitedInterpolatable(
		densityFunction: DensityFunction,
		densityFunction2: DensityFunction,
		i: Int,
		j: Int,
		k: Int,
	): DensityFunction {
		return DensityFunctions.interpolated(DensityFunctions.rangeChoice(densityFunction,
			i.toDouble(),
			(j + 1).toDouble(),
			densityFunction2,
			DensityFunctions.constant(k.toDouble())))
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
			DensityFunctions.zero()
		)
		val scaled = DensityFunctions.mul(
			blended,
			scale.halfNegative()
		)
		val gradient = noiseGradientDensity(
			factor,
			DensityFunctions.add(depth, scaled)
		)

		return DensityFunctions.cache2d(DensityFunctions.add(gradient, BASE_3D_NOISE))
	}

	private fun uhcPillars(): DensityFunction {
		val densityFunction = DensityFunctions.noise(getNoise(Noises.PILLAR), 25.0, 0.3)
		val densityFunction2 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_RARENESS), 0.0, -2.0)
		val densityFunction3 = DensityFunctions.mappedNoise(getNoise(Noises.PILLAR_THICKNESS), 0.0, 1.1)
		val densityFunction4 =
			DensityFunctions.add(DensityFunctions.mul(densityFunction, DensityFunctions.constant(2.0)),
				densityFunction2)
		return DensityFunctions.cacheOnce(
			DensityFunctions.add(
				DensityFunctions.mul(densityFunction4, densityFunction3.cube()),
				DensityFunctions.constant(0.25)
			)
		)
	}

	private fun uhcSpaghetti(): DensityFunction {
		val baseSpaghetti = DensityFunctions.weirdScaledSampler(
			DensityFunctions.noise(getNoise(Noises.SPAGHETTI_2D_MODULATOR), 1.0, 16.0),
			getNoise(Noises.SPAGHETTI_2D),
			TYPE2
		)

		val spaghettiThickness = DensityFunctions.cacheOnce(
			DensityFunctions.mappedNoise(
				getNoise(Noises.SPAGHETTI_2D_THICKNESS),
				2.0,
				1.0,
				-0.6,
				-1.3
			)
		)

		val elevation = DensityFunctions.add(
			DensityFunctions.mappedNoise(
				getNoise(Noises.SPAGHETTI_2D_ELEVATION),
				0.0,
				Math.floorDiv(-64, 8).toDouble(),
				8.0
			),
			DensityFunctions.yClampedGradient(-64, 64, 0.0, 32.0)
		)

		val spaghettiElevationThickener = DensityFunctions.add(elevation, spaghettiThickness).cube()
		val spaghettiThickener = DensityFunctions.add(
			baseSpaghetti,
			DensityFunctions.mul(DensityFunctions.constant(0.083), spaghettiThickness)
		)

		val roughness = DensityFunctions.noise(getNoise(Noises.SPAGHETTI_ROUGHNESS), 1.5, 1.5)
		val roughnessModulated = DensityFunctions.mappedNoise(
			getNoise(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1
		)
		val finalRoughness = DensityFunctions.cacheOnce(
			DensityFunctions.mul(
				roughnessModulated,
				DensityFunctions.add(roughness.abs(), DensityFunctions.constant(-0.4))
			)
		)

		return DensityFunctions.add(
			finalRoughness,
			DensityFunctions.max(spaghettiThickener, spaghettiElevationThickener).clamp(-1.0, 1.0)
		).clamp(-1.0, 0.5)
	}

	private fun uhcCheese(): DensityFunction {
		return DensityFunctions.mul(
			DensityFunctions.add(
				DensityFunctions.constant(0.05),
				DensityFunctions.noise(getNoise(Noises.CAVE_CHEESE), 2.25, 15.0),
			),
			DensityFunctions.constant(1.5),
		).clamp(-2.0, 0.5)
	}

	private fun uhcUnderground(slopes: DensityFunction): DensityFunction {
		val lowerCaves = DensityFunctions.add(
			uhcCheese(),
			DensityFunctions.yClampedGradient(0, 70, 0.0, 0.5)
		)

		val allCaves = DensityFunctions.min(lowerCaves, entrances())

		return DensityFunctions.add(
			/* pillars minus air */
			DensityFunctions.add(
				uhcPillars(),
				DensityFunctions.add(slopes, DensityFunctions.constant(-0.6)).clamp(-1000000.0, 0.0)
			).clamp(0.0, 10.0),
			/* land minus caves */
			DensityFunctions.min(slopes, allCaves).clamp(-0.25, 2.0)
		)
	}

	fun customOverworldGame(noiseSettings: NoiseSettings): NoiseRouterWithOnlyNoises {
		/* create aquifers */
		val aquiferBarrier = DensityFunctions.noise(getNoise(Noises.AQUIFER_BARRIER), 1.0, 1.0)
		val aquiferFloodedness = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 1.0, 8.0)
		val aquiferSpread = DensityFunctions.noise(getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 2.0, 1.0)
		val aquiferLava = DensityFunctions.noise(getNoise(Noises.AQUIFER_LAVA), 8.0, 1.0)

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
			BLENDING_FACTOR
		)
		val depth = DensityFunctions.add(
			DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5),
			splineWithBlending(
				continents,
				erosion,
				ridges,
				SplineType.OFFSET,
				-0.81,
				2.5,
				DensityFunctions.blendOffset()
			)
		)

		val slopes = slopes(
			continents,
			erosion,
			ridges,
			factor,
			depth,
			DensityFunctions.noise(getNoise(Noises.JAGGED), 500.0, 0.4)
		)

		/* put it all together */
		val initialDensity = noiseGradientDensity(DensityFunctions.cache2d(factor), depth)
		val finalDensity = postProcess(noiseSettings, uhcUnderground(slopes))

		return NoiseRouterWithOnlyNoises(
			aquiferBarrier, //barrierNoise
			aquiferFloodedness, //fluidLevelFloodednessNoise
			aquiferSpread, //fluidLevelSpreadNoise
			aquiferLava, //lavaNoise

			temperature, //temperature
			vegetation, //vegetation
			continents, //continents
			erosion, //erosion
			DEPTH, //depth
			ridges, //ridges

			initialDensity, //initialDensityWithoutJaggedness
			finalDensity, //finalDensity

			DensityFunctions.zero(), //veinToggle
			DensityFunctions.zero(), //veinRidged
			DensityFunctions.zero(), //veinGap
		)
	}
}
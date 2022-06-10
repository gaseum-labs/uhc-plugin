package org.gaseumlabs.uhc.world.gen

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext
import net.minecraft.world.level.levelgen.DensityFunction.SimpleFunction
import net.minecraft.world.level.levelgen.DensityFunctions.*
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters

object UHCNoiseRouterData {
	private fun getNoise(resourceKey: ResourceKey<NoiseParameters>): Holder<NoiseParameters> {
		return BuiltinRegistries.NOISE.getHolderOrThrow(resourceKey)
	}

	private fun uhcSpaghetti(
		low: Double,
		high: Double,
		noiseIndex: Int,
	): DensityFunction {
		val spaghetties =
			add(
				constant(-1.0),
				mul(
					mappedNoise(getNoise(CustomNoise.UHC_SAPGHETTI_KEYS[noiseIndex]), 2.0, 0.9, -1.0, 1.0).abs(),
					constant(8.0)
				)
			).clamp(-1.0, 1.0)

		val elevation = mul(
			add(
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
			).abs(),
			constant(1.0 / 8.0 /* cave height*/)
		).cube()

		return add(spaghetties, elevation)
	}

	private class YContinualGradient(
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

	private fun minChain(vararg densityFunctions: DensityFunction): DensityFunction {
		if (densityFunctions.size < 2) throw Exception("Need 2 to make a chain")

		var ret = min(densityFunctions[1], densityFunctions[0])

		for (i in 2..densityFunctions.lastIndex) {
			ret = min(densityFunctions[i], ret)
		}

		return ret
	}

	private fun allSpaghettis(): DensityFunction {
		return minChain(
			uhcSpaghetti(-64.0, -48.0, 0),
			uhcSpaghetti(-48.0, -32.0, 1),
			uhcSpaghetti(-32.0, -16.0, 2),
			uhcSpaghetti(-16.0, 0.0, 3),
			uhcSpaghetti(0.0, 16.0, 4),
			uhcSpaghetti(16.0, 32.0, 5),
		)
	}

	private fun underground(densityFunction: DensityFunction): DensityFunction {
		/* spaghetti caves */
		val densityFunction2 = allSpaghettis() //NoiseRouterData.getFunction(NoiseRouterData.SPAGHETTI_2D)

		val densityFunction3 = NoiseRouterData.getFunction(NoiseRouterData.SPAGHETTI_ROUGHNESS_FUNCTION)

		val densityFunction4 = noise(NoiseRouterData.getNoise(Noises.CAVE_LAYER), 8.0)
		val densityFunction5 = mul(constant(4.0), densityFunction4.square())
		val densityFunction6 = noise(NoiseRouterData.getNoise(Noises.CAVE_CHEESE), 0.6666666666666666)

		val densityFunction7 = add(add(constant(0.27), densityFunction6).clamp(-1.0, 1.0),
			add(constant(1.5), mul(constant(-0.64), densityFunction)).clamp(0.0, 0.5))

		val densityFunction8 = add(densityFunction5, densityFunction7)

		val densityFunction9 = min(
			min(densityFunction8, NoiseRouterData.getFunction(NoiseRouterData.ENTRANCES)),
			add(densityFunction2, densityFunction3)
		)

		val densityFunction10 = NoiseRouterData.getFunction(NoiseRouterData.PILLARS)
		val densityFunction11 =
			rangeChoice(densityFunction10, -1000000.0, 0.03, constant(-1000000.0), densityFunction10)

		return max(densityFunction9, densityFunction11)
	}

	fun customOverworldGame(noiseSettings: NoiseSettings): NoiseRouterWithOnlyNoises {
		val densityFunction = noise(NoiseRouterData.getNoise(Noises.AQUIFER_BARRIER), 0.5)
		val densityFunction2 = noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67)
		val densityFunction3 = noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143)
		val densityFunction4 = noise(NoiseRouterData.getNoise(Noises.AQUIFER_LAVA))
		val densityFunction5 = NoiseRouterData.getFunction(NoiseRouterData.SHIFT_X)
		val densityFunction6 = NoiseRouterData.getFunction(NoiseRouterData.SHIFT_Z)
		val densityFunction7 = shiftedNoise2d(densityFunction5,
			densityFunction6,
			0.25,
			NoiseRouterData.getNoise(Noises.TEMPERATURE))
		val densityFunction8 = shiftedNoise2d(densityFunction5,
			densityFunction6,
			0.25,
			NoiseRouterData.getNoise(Noises.VEGETATION))
		val densityFunction9 =
			NoiseRouterData.getFunction(NoiseRouterData.FACTOR)
		val densityFunction10 =
			NoiseRouterData.getFunction(NoiseRouterData.DEPTH)
		val densityFunction11 = NoiseRouterData.noiseGradientDensity(cache2d(densityFunction9), densityFunction10)
		val densityFunction12 =
			NoiseRouterData.getFunction(NoiseRouterData.SLOPED_CHEESE)
		val densityFunction13 =
			min(densityFunction12, mul(constant(5.0), NoiseRouterData.getFunction(NoiseRouterData.ENTRANCES)))

		/* ADD UNDERGROUND */
		val densityFunction14 = rangeChoice(
			densityFunction12,
			-1000000.0,
			1.5625,
			densityFunction13,
			underground(densityFunction12)
		)

		val densityFunction15 = min(NoiseRouterData.postProcess(noiseSettings, densityFunction14),
			NoiseRouterData.getFunction(NoiseRouterData.NOODLE))

		return NoiseRouterWithOnlyNoises(
			densityFunction,
			densityFunction2,
			densityFunction3,
			densityFunction4,
			densityFunction7,
			densityFunction8,
			NoiseRouterData.getFunction(NoiseRouterData.CONTINENTS),
			NoiseRouterData.getFunction(NoiseRouterData.EROSION),
			NoiseRouterData.getFunction(NoiseRouterData.DEPTH),
			NoiseRouterData.getFunction(NoiseRouterData.RIDGES),
			densityFunction11,
			densityFunction15,
			zero(),
			zero(),
			zero()
		)
	}
}
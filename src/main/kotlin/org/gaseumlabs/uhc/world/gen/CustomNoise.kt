package org.gaseumlabs.uhc.world.gen

import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.Noises
import net.minecraft.world.level.levelgen.synth.NormalNoise

object CustomNoise {
	val UHC_SAPGHETTI_MODULATOR_KEYS = noiseKeyArray("uhc_spaghetti_modulator", 7)
	val UHC_SAPGHETTI_KEYS = noiseKeyArray("uhc_spaghetti", 7)
	val UHC_SAPGHETTI_ELEVATION_KEYS = noiseKeyArray("uhc_spaghetti_elevation", 7)
	val UHC_SAPGHETTI_THICKNESS_KEYS = noiseKeyArray("uhc_spaghetti_thickness", 7)

	init {
		createNoiseCloneArray(UHC_SAPGHETTI_MODULATOR_KEYS, Noises.SPAGHETTI_2D_MODULATOR)
		createNoiseCloneArray(UHC_SAPGHETTI_KEYS, Noises.SPAGHETTI_2D)
		createNoiseCloneArray(UHC_SAPGHETTI_ELEVATION_KEYS, Noises.SPAGHETTI_2D_ELEVATION)
		createNoiseCloneArray(UHC_SAPGHETTI_THICKNESS_KEYS, Noises.SPAGHETTI_2D_THICKNESS)
	}

	fun noiseKey(name: String): ResourceKey<NormalNoise.NoiseParameters> {
		return ResourceKey.create(Registry.NOISE_REGISTRY, ResourceLocation(name))
	}

	fun noiseKeyArray(baseName: String, length: Int): Array<ResourceKey<NormalNoise.NoiseParameters>> {
		return Array(length) { i ->
			noiseKey(baseName + "_${i}")
		}
	}

	fun registerNoiseClone(key: ResourceKey<NormalNoise.NoiseParameters>, original: NormalNoise.NoiseParameters) {
		val doubleArray = DoubleArray(original.amplitudes.size - 1) { i ->
			original.amplitudes.getDouble(i + 1)
		}

		BuiltinRegistries.register(
			BuiltinRegistries.NOISE,
			key,
			NormalNoise.NoiseParameters(
				original.firstOctave,
				original.amplitudes.first(),
				*doubleArray
			)
		)
	}

	fun createNoiseCloneArray(keys: Array<ResourceKey<NormalNoise.NoiseParameters>>, originalKey: ResourceKey<NormalNoise.NoiseParameters>) {
		val original = BuiltinRegistries.NOISE.getOrCreateHolder(originalKey).value()

		for (key in keys) {
			registerNoiseClone(key, original)
		}
	}
}

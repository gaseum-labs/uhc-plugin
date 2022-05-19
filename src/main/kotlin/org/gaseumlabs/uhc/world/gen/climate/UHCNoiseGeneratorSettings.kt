package org.gaseumlabs.uhc.world.gen.climate

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.data.worldgen.SurfaceRuleData
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters
import org.gaseumlabs.uhc.util.reflect.UHCReflect

object UHCNoiseGeneratorSettings {
	private fun subCreateGame(amplified: Boolean): NoiseSettings {
		return NoiseSettings.create(
			-64,
			384,
			NoiseSamplingSettings(1.0, 1.0, 80.0, 160.0),
			NoiseSlider(-0.078125, 2, if (amplified) 0 else 8),
			NoiseSlider(if (amplified) 0.4 else 0.1171875, 3, 0),
			1,
			2,
			UHCTerrainShaper.createGame(amplified)
		)
	}

	fun createGame(amplified: Boolean): NoiseGeneratorSettings {
		val noiseSettings = subCreateGame(amplified)

		return NoiseGeneratorSettings(
			noiseSettings,
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			UHCNoiseRouterData.customOverworldGame(noiseSettings),
			SurfaceRuleData.overworld(),
			63,
			false,
			true,
			true,
			false
		)
	}

	private val settingsField = UHCReflect<NoiseBasedChunkGenerator, Holder<NoiseGeneratorSettings>>(
		NoiseBasedChunkGenerator::class,
		"settings"
	)
	private val routerField = UHCReflect<NoiseBasedChunkGenerator, NoiseRouter>(
		NoiseBasedChunkGenerator::class,
		"router"
	)
	private val seedField = UHCReflect<NoiseBasedChunkGenerator, Long>(
		NoiseBasedChunkGenerator::class,
		"seed"
	)
	private val noisesField = UHCReflect<NoiseBasedChunkGenerator, Registry<NoiseParameters>>(
		NoiseBasedChunkGenerator::class,
		"noises"
	)

	fun inject(
		chunkGenerator: NoiseBasedChunkGenerator,
		settings: NoiseGeneratorSettings,
		modifyNoise: Boolean,
		surfaceRule: RuleSource,
	) {
		settingsField.set(chunkGenerator, Holder.direct(settings))

		settings.surfaceRule = surfaceRule

		if (modifyNoise) routerField.set(
			chunkGenerator,
			settings.createNoiseRouter(
				noisesField.get(chunkGenerator),
				seedField.get(chunkGenerator)
			)
		)
	}
}
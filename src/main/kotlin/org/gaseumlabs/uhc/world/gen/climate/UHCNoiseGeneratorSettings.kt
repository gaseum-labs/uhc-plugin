package org.gaseumlabs.uhc.world.gen.climate

import com.google.common.collect.ImmutableList
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.data.worldgen.SurfaceRuleData
import net.minecraft.data.worldgen.SurfaceRuleData.PaperBedrockConditionSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.*
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters
import org.gaseumlabs.uhc.util.reflect.UHCReflect
import java.util.function.*

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

		val loader = Thread.currentThread().contextClassLoader

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

	fun uhcOverworldSurfaceRule(): RuleSource {
		val conditionSource = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(97), 2)
		val conditionSource2 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(256), 0)
		val conditionSource3 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(63), -1)
		val conditionSource4 = SurfaceRules.yStartCheck(VerticalAnchor.absolute(74), 1)
		val conditionSource5 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0)
		val conditionSource6 = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0)
		val conditionSource7 = SurfaceRules.waterBlockCheck(-1, 0)
		val conditionSource8 = SurfaceRules.waterBlockCheck(0, 0)
		val conditionSource9 = SurfaceRules.waterStartCheck(-6, -1)
		val conditionSource10 = SurfaceRules.hole()
		val conditionSource11 = SurfaceRules.isBiome(
			Biomes.FROZEN_OCEAN, Biomes.UHC_FROZEN_OCEAN,
			Biomes.DEEP_FROZEN_OCEAN, Biomes.UHC_DEEP_COLD_OCEAN,
		)
		val conditionSource12 = SurfaceRules.steep()
		val ruleSource = SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.GRASS_BLOCK),
			SurfaceRuleData.DIRT)
		val ruleSource2 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SurfaceRuleData.SANDSTONE),
			SurfaceRuleData.SAND)
		val ruleSource3 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING, SurfaceRuleData.STONE),
			SurfaceRuleData.GRAVEL)
		val conditionSource13 = SurfaceRules.isBiome(
			Biomes.WARM_OCEAN, Biomes.UHC_WARM_OCEAN,
			Biomes.BEACH, Biomes.UHC_BEACH,
			Biomes.SNOWY_BEACH, Biomes.UHC_SNOWY_BEACH,
		)
		val conditionSource14 = SurfaceRules.isBiome(Biomes.DESERT, Biomes.UHC_DESERT)
		val ruleSource4 =
			SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.STONY_PEAKS, Biomes.UHC_STONY_PEAKS),
				SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.CALCITE, -0.0125, 0.0125),
					SurfaceRuleData.CALCITE), SurfaceRuleData.STONE)),
				SurfaceRules.ifTrue(SurfaceRules.isBiome(
					Biomes.STONY_SHORE, Biomes.UHC_STONY_SHORE),
					SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.GRAVEL, -0.05, 0.05),
						ruleSource3), SurfaceRuleData.STONE)),
				SurfaceRules.ifTrue(SurfaceRules.isBiome(
					Biomes.WINDSWEPT_HILLS, Biomes.UHC_WINDSWEPT_HILLS),
					SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(1.0), SurfaceRuleData.STONE)),
				SurfaceRules.ifTrue(conditionSource13, ruleSource2),
				SurfaceRules.ifTrue(conditionSource14, ruleSource2),
				SurfaceRules.ifTrue(SurfaceRules.isBiome(
					Biomes.DRIPSTONE_CAVES, Biomes.UHC_DRIPSTONE_CAVES), SurfaceRuleData.STONE))
		val ruleSource5 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.45, 0.58),
			SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.POWDER_SNOW))
		val ruleSource6 = SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.POWDER_SNOW, 0.35, 0.6),
			SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.POWDER_SNOW))
		val ruleSource7 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.FROZEN_PEAKS,
			Biomes.UHC_FROZEN_PEAKS),
			SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource12, SurfaceRuleData.PACKED_ICE),
				SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, -0.5, 0.2),
					SurfaceRuleData.PACKED_ICE),
				SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, -0.0625, 0.025), SurfaceRuleData.ICE),
				SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.SNOW_BLOCK))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.SNOWY_SLOPES, Biomes.UHC_SNOWY_SLOPES),
				SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource12, SurfaceRuleData.STONE),
					ruleSource5,
					SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.SNOW_BLOCK))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.JAGGED_PEAKS, Biomes.UHC_JAGGED_PEAKS), SurfaceRuleData.STONE),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.GROVE),
				SurfaceRules.sequence(ruleSource5, SurfaceRuleData.DIRT)),
			ruleSource4,
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.WINDSWEPT_SAVANNA, Biomes.UHC_WINDSWEPT_SAVANNA),
				SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(1.75), SurfaceRuleData.STONE)),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.UHC_WINDSWEPT_GRAVELLY_HILLS),
				SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(2.0), ruleSource3),
					SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(1.0), SurfaceRuleData.STONE),
					SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(-1.0), SurfaceRuleData.DIRT),
					ruleSource3)),
			SurfaceRuleData.DIRT)
		val ruleSource8 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(Biomes.FROZEN_PEAKS,
			Biomes.UHC_FROZEN_PEAKS),
			SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource12, SurfaceRuleData.PACKED_ICE),
				SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.PACKED_ICE, 0.0, 0.2),
					SurfaceRuleData.PACKED_ICE),
				SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.ICE, 0.0, 0.025), SurfaceRuleData.ICE),
				SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.SNOW_BLOCK))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.SNOWY_SLOPES, Biomes.UHC_SNOWY_SLOPES),
				SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource12, SurfaceRuleData.STONE),
					ruleSource6,
					SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.SNOW_BLOCK))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.JAGGED_PEAKS, Biomes.UHC_JAGGED_PEAKS),
				SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource12, SurfaceRuleData.STONE),
					SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.SNOW_BLOCK))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.GROVE, Biomes.UHC_GROVE),
				SurfaceRules.sequence(ruleSource6, SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.SNOW_BLOCK))),
			ruleSource4,
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.WINDSWEPT_SAVANNA, Biomes.UHC_WINDSWEPT_SAVANNA),
				SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(1.75),
					SurfaceRuleData.STONE),
					SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(-0.5), SurfaceRuleData.COARSE_DIRT))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.UHC_WINDSWEPT_GRAVELLY_HILLS),
				SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(2.0), ruleSource3),
					SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(1.0), SurfaceRuleData.STONE),
					SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(-1.0), ruleSource),
					ruleSource3)),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.UHC_OLD_GROWTH_PINE_TAIGA,
				Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.UHC_OLD_GROWTH_SPRUCE_TAIGA
			),
				SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(1.75),
					SurfaceRuleData.COARSE_DIRT),
					SurfaceRules.ifTrue(SurfaceRuleData.surfaceNoiseAbove(-0.95), SurfaceRuleData.PODZOL))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.ICE_SPIKES, Biomes.UHC_ICE_SPIKES),
				SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.SNOW_BLOCK)),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.MUSHROOM_FIELDS, Biomes.UHC_MUSHROOM_FIELDS), SurfaceRuleData.MYCELIUM),
			ruleSource)
		val conditionSource15 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.909, -0.5454)
		val conditionSource16 = SurfaceRules.noiseCondition(Noises.SURFACE, -0.1818, 0.1818)
		val conditionSource17 = SurfaceRules.noiseCondition(Noises.SURFACE, 0.5454, 0.909)
		val ruleSource9 = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
			SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.WOODED_BADLANDS, Biomes.UHC_WOODED_BADLANDS),
				SurfaceRules.ifTrue(conditionSource,
					SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource15, SurfaceRuleData.COARSE_DIRT),
						SurfaceRules.ifTrue(conditionSource16, SurfaceRuleData.COARSE_DIRT),
						SurfaceRules.ifTrue(conditionSource17, SurfaceRuleData.COARSE_DIRT),
						ruleSource))), SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.SWAMP, Biomes.UHC_SWAMP),
				SurfaceRules.ifTrue(conditionSource5,
					SurfaceRules.ifTrue(SurfaceRules.not(conditionSource6),
						SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), SurfaceRuleData.WATER)))))),
			SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.BADLANDS, Biomes.UHC_BADLANDS,
				Biomes.ERODED_BADLANDS, Biomes.UHC_ERODED_BADLANDS,
				Biomes.WOODED_BADLANDS, Biomes.UHC_WOODED_BADLANDS,
			),
				SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
					SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource2, SurfaceRuleData.ORANGE_TERRACOTTA),
						SurfaceRules.ifTrue(conditionSource4,
							SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource15, SurfaceRuleData.TERRACOTTA),
								SurfaceRules.ifTrue(conditionSource16, SurfaceRuleData.TERRACOTTA),
								SurfaceRules.ifTrue(conditionSource17, SurfaceRuleData.TERRACOTTA),
								SurfaceRules.bandlands())),
						SurfaceRules.ifTrue(conditionSource7,
							SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_CEILING,
								SurfaceRuleData.RED_SANDSTONE), SurfaceRuleData.RED_SAND)),
						SurfaceRules.ifTrue(SurfaceRules.not(conditionSource10), SurfaceRuleData.ORANGE_TERRACOTTA),
						SurfaceRules.ifTrue(conditionSource9, SurfaceRuleData.WHITE_TERRACOTTA),
						ruleSource3)),
					SurfaceRules.ifTrue(conditionSource3,
						SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource6,
							SurfaceRules.ifTrue(SurfaceRules.not(conditionSource4), SurfaceRuleData.ORANGE_TERRACOTTA)),
							SurfaceRules.bandlands())),
					SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR,
						SurfaceRules.ifTrue(conditionSource9, SurfaceRuleData.WHITE_TERRACOTTA)))),
			SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
				SurfaceRules.ifTrue(conditionSource7,
					SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource11,
						SurfaceRules.ifTrue(conditionSource10,
							SurfaceRules.sequence(SurfaceRules.ifTrue(conditionSource8, SurfaceRuleData.AIR),
								SurfaceRules.ifTrue(SurfaceRules.temperature(), SurfaceRuleData.ICE),
								SurfaceRuleData.WATER))), ruleSource8))),
			SurfaceRules.ifTrue(conditionSource9,
				SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
					SurfaceRules.ifTrue(conditionSource11,
						SurfaceRules.ifTrue(conditionSource10, SurfaceRuleData.WATER))),
					SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, ruleSource7),
					SurfaceRules.ifTrue(conditionSource13,
						SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, SurfaceRuleData.SANDSTONE)),
					SurfaceRules.ifTrue(conditionSource14,
						SurfaceRules.ifTrue(SurfaceRules.VERY_DEEP_UNDER_FLOOR, SurfaceRuleData.SANDSTONE)))),
			SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.isBiome(
				Biomes.FROZEN_PEAKS, Biomes.UHC_FROZEN_PEAKS,
				Biomes.JAGGED_PEAKS, Biomes.UHC_JAGGED_PEAKS,
			), SurfaceRuleData.STONE),
				SurfaceRules.ifTrue(SurfaceRules.isBiome(
					Biomes.WARM_OCEAN, Biomes.UHC_WARM_OCEAN,
					Biomes.LUKEWARM_OCEAN, Biomes.UHC_LUKEWARM_OCEAN,
					Biomes.DEEP_LUKEWARM_OCEAN, Biomes.UHC_DEEP_LUKEWARM_OCEAN
				), ruleSource2),
				ruleSource3)))
		val builder = ImmutableList.builder<RuleSource>()

		/* bedrock floor */
		builder.add(SurfaceRules.ifTrue(PaperBedrockConditionSource("bedrock_floor",
			VerticalAnchor.bottom(),
			VerticalAnchor.aboveBottom(5),
			false), SurfaceRuleData.BEDROCK)) // Paper

		val ruleSource10 = SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(), ruleSource9)
		builder.add(ruleSource10)
		builder.add(SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate",
			VerticalAnchor.absolute(0),
			VerticalAnchor.absolute(8)), SurfaceRuleData.DEEPSLATE))

		val builderArray = builder.build().toTypedArray()
		return SurfaceRules.sequence(*builderArray)
	}

	fun inject(chunkGenerator: NoiseBasedChunkGenerator, seed: Long, settings: NoiseGeneratorSettings) {
		settingsField.set(chunkGenerator, Holder.direct(settings))

		settings.surfaceRule = uhcOverworldSurfaceRule()

		routerField.set(
			chunkGenerator,
			settings.createNoiseRouter(
				noisesField.get(chunkGenerator),
				seedField.get(chunkGenerator)
			)
		)
	}
}
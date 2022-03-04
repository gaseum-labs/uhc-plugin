package com.codeland.uhc.world.gen

import com.codeland.uhc.world.UHCReflect
import net.minecraft.data.worldgen.placement.OrePlacements
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.GenerationStep.Carving
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.feature.configurations.*
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import java.lang.reflect.Constructor
import java.util.function.*

object ModifiedBiomes {
	/* fields of Biome */
	val generationSettingsField = UHCReflect<Biome, BiomeGenerationSettings>(
		Biome::class, "generationSettings"
	)
	val mobsSettingsField = UHCReflect<Biome, MobSpawnSettings>(
		Biome::class, "mobSettings"
	)

	val climateSettingsField = UHCReflect<Biome, Any>(
		Biome::class, "climateSettings"
	)
	val biomeCategoryField = UHCReflect<Biome, Any>(
		Biome::class, "biomeCategory"
	)
	val specialEffectsField = UHCReflect<Biome, Any>(
		Biome::class, "specialEffects"
	)

	/* fields of BiomeGenerationSettings */
	val featuresField = UHCReflect<BiomeGenerationSettings, List<List<Supplier<PlacedFeature>>>>(
		BiomeGenerationSettings::class, "features"
	)
	val carversField =
		UHCReflect<BiomeGenerationSettings, Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<*>>>>>(
			BiomeGenerationSettings::class, "carvers"
		)

	val biomeGenerationSettingsConstructor = BiomeGenerationSettings::class.java.constructors[0]
	val mobSpawnSettingsConstructor = MobSpawnSettings::class.java.constructors[0]
	val biomeConstructor = Biome::class.java.constructors[0] as Constructor<Biome>

	init {
		biomeGenerationSettingsConstructor.isAccessible = true
		mobSpawnSettingsConstructor.isAccessible = true
		biomeConstructor.isAccessible = true
	}

	fun genBiomes(replaceFeatures: Boolean, replaceMobs: Boolean): Map<Int, Biome> {
		val ret = HashMap<Int, Biome>()

		BiomeNo.biomeRegistry.entrySet().forEach { (key, original) ->
			val biomeId = BiomeNo.toId(key)
			val originalSettings = generationSettingsField.get(original)

			val newSettings = if (replaceFeatures) {
				val originalFeatures = featuresField.get(originalSettings)
				val originalCarvers = carversField.get(originalSettings)

				val newCarvers = HashMap<Carving, List<Supplier<ConfiguredWorldCarver<*>>>>()

				originalCarvers.forEach { (key) ->
					newCarvers[key] = if (BiomeNo.isNetherBiome(biomeId)) {
						arrayListOf(
							Supplier { ModifiedBiomesRegistry.netherSuperCaveCarver },
							Supplier { ModifiedBiomesRegistry.netherUpperCaveCarver },
						)
					} else {
						arrayListOf(
							Supplier { ModifiedBiomesRegistry.caveLevels[0] },
							Supplier { ModifiedBiomesRegistry.caveLevels[1] },
							Supplier { ModifiedBiomesRegistry.caveLevels[2] },
							Supplier { ModifiedBiomesRegistry.caveLevels[3] },
							Supplier { ModifiedBiomesRegistry.superCanyonCarver },
						)
					}
				}

				val newFeatures = ArrayList<List<Supplier<PlacedFeature>>>()

				originalFeatures.forEach { originalSubList ->
					val newSubList = ArrayList<Supplier<PlacedFeature>>()

					originalSubList.forEach { supplier ->
						val configured = supplier.get()

						if (
							configured === OrePlacements.ORE_GOLD || // gold ores (excluding badlands)
							configured === OrePlacements.ORE_GOLD_LOWER ||

							configured === OrePlacements.ORE_LAPIS || // lapis ore
							configured === OrePlacements.ORE_LAPIS_BURIED ||

							configured === OrePlacements.ORE_DIAMOND || // diamond ores
							configured === OrePlacements.ORE_DIAMOND_LARGE ||
							configured === OrePlacements.ORE_DIAMOND_BURIED ||

							configured === VegetationPlacements.PATCH_MELON || // melons
							configured === VegetationPlacements.PATCH_MELON_SPARSE ||

							configured === VegetationPlacements.PATCH_SUGAR_CANE || // sugar cane (excluding badlands)
							configured === VegetationPlacements.PATCH_SUGAR_CANE_DESERT ||
							configured === VegetationPlacements.PATCH_SUGAR_CANE_SWAMP
						) {
							//
						} else {
							newSubList.add(supplier)
						}
					}
					newFeatures.add(newSubList)
				}

				biomeGenerationSettingsConstructor.newInstance(
					newCarvers,
					newFeatures
				)
			} else {
				originalSettings
			}

			val originalMobs = mobsSettingsField.get(original)

			val newMobs = if (replaceMobs) {
				mobSpawnSettingsConstructor.newInstance(
					-1.0f, /* prevent all chunk mobs */
					emptyMap<Any, Any>(),
					emptyMap<Any, Any>(),
				)
			} else {
				originalMobs
			}

			ret[biomeId] = biomeConstructor.newInstance(
				climateSettingsField.get(original),
				newSettings,
				newMobs,
				biomeCategoryField.get(original),
				specialEffectsField.get(original)
			)
		}

		return ret
	}
}

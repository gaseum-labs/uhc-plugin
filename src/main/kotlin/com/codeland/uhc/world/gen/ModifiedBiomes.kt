package com.codeland.uhc.world.gen

import com.codeland.uhc.reflect.UHCReflect
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.data.worldgen.placement.OrePlacements
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.levelgen.GenerationStep.Carving
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
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
	val featuresField = UHCReflect<BiomeGenerationSettings, List<HolderSet<PlacedFeature>>>(
		BiomeGenerationSettings::class, "features"
	)
	val carversField =
		UHCReflect<BiomeGenerationSettings, Map<Carving, HolderSet<ConfiguredWorldCarver<*>>>>(
			BiomeGenerationSettings::class, "carvers"
		)

	val biomeGenerationSettingsConstructor = BiomeGenerationSettings::class.java.declaredConstructors[0]
	val mobSpawnSettingsConstructor = MobSpawnSettings::class.java.declaredConstructors[0]
	val biomeConstructor = Biome::class.java.declaredConstructors[0] as Constructor<Biome>

	init {
		biomeGenerationSettingsConstructor.isAccessible = true
		mobSpawnSettingsConstructor.isAccessible = true
		biomeConstructor.isAccessible = true
	}

	fun genBiomes(replaceFeatures: Boolean, replaceMobs: Boolean): Map<Int, Holder<Biome>> {
		val ret = HashMap<Int, Holder<Biome>>()

		BiomeNo.biomeRegistry.entrySet().forEach { (key, original) ->
			val biomeId = BiomeNo.toId(key)
			val originalSettings = generationSettingsField.get(original)

			val newSettings = if (replaceFeatures) {
				val originalFeatures = featuresField.get(originalSettings)
				val originalCarvers = carversField.get(originalSettings)

				val newCarvers = HashMap<Carving, HolderSet<ConfiguredWorldCarver<*>>>()

				originalCarvers.forEach { (key) ->
					newCarvers[key] = if (BiomeNo.isNetherBiome(biomeId)) {
						HolderSet.direct(
							Holder.direct(CustomCarvers.netherSuperCaveCarver),
							Holder.direct(CustomCarvers.netherUpperCaveCarver)
						)
					} else {
						HolderSet.direct(
							Holder.direct(CustomCarvers.caveLevels[0]),
							Holder.direct(CustomCarvers.caveLevels[1]),
							Holder.direct(CustomCarvers.caveLevels[2]),
							Holder.direct(CustomCarvers.caveLevels[3]),
							Holder.direct(CustomCarvers.superCanyonCarver),
						)
					}
				}

				val newFeatures = ArrayList<HolderSet<PlacedFeature>>()

				originalFeatures.forEach { originalSubList ->
					val tempList = ArrayList<Holder<PlacedFeature>>()

					originalSubList.forEach { holder ->
						if (
							holder === OrePlacements.ORE_GOLD || // gold ores (excluding badlands)
							holder === OrePlacements.ORE_GOLD_LOWER ||

							holder === OrePlacements.ORE_LAPIS || // lapis ore
							holder === OrePlacements.ORE_LAPIS_BURIED ||

							holder === OrePlacements.ORE_DIAMOND || // diamond ores
							holder === OrePlacements.ORE_DIAMOND_LARGE ||
							holder === OrePlacements.ORE_DIAMOND_BURIED ||

							holder === VegetationPlacements.PATCH_MELON || // melons
							holder === VegetationPlacements.PATCH_MELON_SPARSE ||

							holder === VegetationPlacements.PATCH_SUGAR_CANE || // sugar cane (excluding badlands)
							holder === VegetationPlacements.PATCH_SUGAR_CANE_DESERT ||
							holder === VegetationPlacements.PATCH_SUGAR_CANE_SWAMP
						) {
							//
						} else {
							tempList.add(holder)
						}
					}
					newFeatures.add(HolderSet.direct(tempList))
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

			ret[biomeId] = Holder.direct(biomeConstructor.newInstance(
				climateSettingsField.get(original),
				biomeCategoryField.get(original),
				specialEffectsField.get(original),
				newSettings,
				newMobs,
			))
		}

		return ret
	}
}

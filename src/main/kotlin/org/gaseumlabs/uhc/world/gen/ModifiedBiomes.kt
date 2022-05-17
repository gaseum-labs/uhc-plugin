package org.gaseumlabs.uhc.world.gen

import net.minecraft.core.*
import net.minecraft.data.worldgen.placement.OrePlacements
import net.minecraft.data.worldgen.placement.VegetationPlacements
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object ModifiedBiomes {
	val bannedPlacements = arrayOf(
		OrePlacements.ORE_DIAMOND,
		OrePlacements.ORE_DIAMOND_LARGE,
		OrePlacements.ORE_DIAMOND_BURIED,
		OrePlacements.ORE_GOLD,
		OrePlacements.ORE_GOLD_LOWER,
		OrePlacements.ORE_LAPIS,
		OrePlacements.ORE_LAPIS_BURIED,
		VegetationPlacements.PATCH_MELON,
		VegetationPlacements.PATCH_MELON_SPARSE,
		VegetationPlacements.PATCH_SUGAR_CANE,
		VegetationPlacements.PATCH_SUGAR_CANE_DESERT,
		VegetationPlacements.PATCH_SUGAR_CANE_SWAMP,
	)

	fun genBiomes(biomeRegistry: Registry<Biome>, replaceFeatures: Boolean, replaceMobs: Boolean) {
		val biomeHolders = BiomeNo.featureBiomeKeys.map {
			it to biomeRegistry.getOrCreateHolder(it)
		}.forEach { (key, originalBiomeHolder) ->
			val originalBiome = originalBiomeHolder.value()
			val biomeId = BiomeNo.toId(key)
			val originalSettings = originalBiome.generationSettings

			val newSettings = if (!replaceFeatures) {
				originalSettings
			} else {
				val originalFeatures = originalSettings.features
				val newFeatures = ArrayList<HolderSet<PlacedFeature>>()

				originalFeatures.forEach { originalSubList ->
					newFeatures.add(HolderSet.direct(
						originalSubList.filter { holder ->
							val holderKey = holder.unwrapKey().get()
							bannedPlacements.none { banned -> banned.`is`(holderKey) }
						}
					))
				}

				val newCarvers = hashMapOf<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<*>>>(
					GenerationStep.Carving.LIQUID to HolderSet.direct(),
					GenerationStep.Carving.AIR to HolderSet.direct(
						Holder.direct(CustomCarvers.newUhcCarver),
						Holder.direct(CustomCarvers.superCanyonCarver)
					)
				)

				/* do not modify carvers */
				BiomeGenerationSettings(newCarvers, newFeatures)
			}

			val originalMobs = originalBiome.mobSettings

			val newMobs = if (replaceMobs) {
				MobSpawnSettings.Builder().build()
			} else {
				originalMobs
			}

			originalBiome.generationSettings = newSettings
			originalBiome.mobSettings = newMobs
		}
	}
}

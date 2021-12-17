package com.codeland.uhc.world.gen

import com.google.common.collect.ImmutableCollection
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import net.minecraft.core.IRegistry
import net.minecraft.data.RegistryGeneration
import net.minecraft.data.worldgen.BiomeDecoratorGroups
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.WorldGenStage
import net.minecraft.world.level.levelgen.carver.WorldGenCarverWrapper
import net.minecraft.world.level.levelgen.feature.WorldGenFeatureConfigured
import net.minecraft.world.level.levelgen.feature.configurations.*
import net.minecraft.world.level.levelgen.feature.stateproviders.WorldGenFeatureStateProviderSimpl
import java.lang.reflect.Constructor
import java.util.function.*

object ModifiedBiomes {
	val biomeRegistryField = RegistryGeneration::class.java.getDeclaredField("i")
	val biomeMapField = BiomeRegistry::class.java.getDeclaredField("c")

	val biomeBaseConstructor = BiomeBase::class.java.declaredConstructors[0] as Constructor<BiomeBase>

	val kField = BiomeBase::class.java.getDeclaredField("k")
	val pField = BiomeBase::class.java.getDeclaredField("p")
	val nField = BiomeBase::class.java.getDeclaredField("n")
	val oField = BiomeBase::class.java.getDeclaredField("o")
	val qField = BiomeBase::class.java.getDeclaredField("q")
	val lField = BiomeBase::class.java.getDeclaredField("l")
	val mField = BiomeBase::class.java.getDeclaredField("m")

	val biomeSettingsGenerationConstructor =
		BiomeSettingsGeneration::class.java.declaredConstructors[0] as Constructor<BiomeSettingsGeneration>

	val biomeSettingsMobsConstructor =
		BiomeSettingsMobs::class.java.declaredConstructors[0] as Constructor<BiomeSettingsMobs>

	val dField = BiomeSettingsGeneration::class.java.getDeclaredField("d")
	val eField = BiomeSettingsGeneration::class.java.getDeclaredField("e")
	val fField = BiomeSettingsGeneration::class.java.getDeclaredField("f")
	val gField = BiomeSettingsGeneration::class.java.getDeclaredField("g")
	val hField = BiomeSettingsGeneration::class.java.getDeclaredField("h")

	init {
		biomeRegistryField.isAccessible = true
		biomeMapField.isAccessible = true

		biomeBaseConstructor.isAccessible = true

		kField.isAccessible = true
		pField.isAccessible = true
		nField.isAccessible = true
		oField.isAccessible = true
		qField.isAccessible = true
		lField.isAccessible = true
		mField.isAccessible = true

		biomeSettingsGenerationConstructor.isAccessible = true
		biomeSettingsMobsConstructor.isAccessible = true

		dField.isAccessible = true
		eField.isAccessible = true
		fField.isAccessible = true
		gField.isAccessible = true
		hField.isAccessible = true
	}

	fun genBiomes(replaceFeatures: Boolean, replaceMobs: Boolean): Map<Int, BiomeBase> {
		val biomeMap = biomeMapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>
		val biomeRegistry = biomeRegistryField[null] as IRegistry<BiomeBase>

		val ret = HashMap<Int, BiomeBase>()

		biomeMap.forEach { (id, key) ->
			val original = biomeRegistry.d(key)

			val originalSettings = lField[original] as BiomeSettingsGeneration
			val newSettings = if (replaceFeatures) {
				val originalCarverMap =
					eField[originalSettings] as Map<WorldGenStage.Features, ImmutableCollection<Supplier<WorldGenCarverWrapper<*>>>>
				val originalFeatures =
					fField[originalSettings] as List<List<Supplier<WorldGenFeatureConfigured<*, *>>>>

				val newCarverMap = HashMap<WorldGenStage.Features, ArrayList<Supplier<WorldGenCarverWrapper<*>>>>()
				val newFeatures = ArrayList<ArrayList<Supplier<WorldGenFeatureConfigured<*, *>>>>()

				originalCarverMap.forEach { (key, value) ->
					newCarverMap[key] = if (BiomeNo.isNetherBiome(id)) {
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

				originalFeatures.forEach { list0 ->
					val newSubList = ArrayList<Supplier<WorldGenFeatureConfigured<*, *>>>()
					list0.forEach { supplier ->
						val configured = supplier.get()

						if (
							configured === BiomeDecoratorGroups.bV || // gold ores (excluding badlands)
							configured === BiomeDecoratorGroups.bW ||
							configured === BiomeDecoratorGroups.cd || // lapis ore
							configured === BiomeDecoratorGroups.ce ||
							configured === BiomeDecoratorGroups.cf ||
							configured === BiomeDecoratorGroups.ca || // diamond ores
							configured === BiomeDecoratorGroups.cb ||
							configured === BiomeDecoratorGroups.cc ||
							configured === BiomeDecoratorGroups.al || // melons
							configured === BiomeDecoratorGroups.aJ ||
							configured === BiomeDecoratorGroups.aT || // sugar cane (excluding badlands)
							configured === BiomeDecoratorGroups.aU ||
							configured === BiomeDecoratorGroups.aW
						) {
							//
						} else {
							newSubList.add(supplier)
						}
					}
					newFeatures.add(newSubList)
				}

				biomeSettingsGenerationConstructor.newInstance(
					dField[originalSettings],
					newCarverMap,
					newFeatures,
					gField[originalSettings],
				)
			} else {
				originalSettings
			}

			val originalMobs = mField[original] as BiomeSettingsMobs
			val newMobs = if (replaceMobs) {
				biomeSettingsMobsConstructor.newInstance(
					-1.0f, /* prevent all chunk mobs */
					emptyMap<Any, Any>(),
					emptyMap<Any, Any>(),
					originalMobs.b()
				)
			} else {
				originalMobs
			}

			ret[id] = biomeBaseConstructor.newInstance(
				kField[original],
				pField[original],
				nField[original],
				oField[original],
				qField[original],
				newSettings,
				newMobs,
			)
		}

		return ret
	}
}

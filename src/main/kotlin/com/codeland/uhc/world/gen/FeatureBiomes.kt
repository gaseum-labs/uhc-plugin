package com.codeland.uhc.world.gen

import com.google.common.collect.ImmutableCollection
import net.minecraft.core.IRegistry
import net.minecraft.data.RegistryGeneration
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.util.valueproviders.ConstantFloat
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.BiomeSettingsGeneration
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.WorldGenStage
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration
import net.minecraft.world.level.levelgen.carver.WorldGenCarverAbstract
import net.minecraft.world.level.levelgen.carver.WorldGenCarverWrapper
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap
import java.lang.reflect.Constructor
import java.util.function.Supplier

object FeatureBiomes {
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

	val biomeSettingsGenerationConstructor = BiomeSettingsGeneration::class.java.declaredConstructors[0] as Constructor<BiomeSettingsGeneration>

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

		dField.isAccessible = true
		eField.isAccessible = true
		fField.isAccessible = true
		gField.isAccessible = true
		hField.isAccessible = true
	}

	val caveCarverMaster = WorldGenCarverAbstract.a

	val superCaveCarver = caveCarverMaster.a(
		CaveCarverConfiguration(
			0.29283719f,
			BiasedToBottomHeight.a(VerticalAnchor.a(0), VerticalAnchor.a(127), 64),
			ConstantFloat.a(1.0f),
			VerticalAnchor.b(0),
			false,
			CarverDebugSettings.a(false, Blocks.ne.blockData),
			ConstantFloat.a(2.0f),
			ConstantFloat.a(2.0f),
			ConstantFloat.a(-1.7f)
		)
	)

	val biomes = genBiomes()

	fun genBiomes(): Map<Int, BiomeBase> {
		val biomeMap = biomeMapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>
		val biomeRegistry = biomeRegistryField[null] as IRegistry<BiomeBase>

		val ret = HashMap<Int, BiomeBase>()

		biomeMap.forEach { (id, key) ->
			try {
				val original = biomeRegistry.d(key)
				val originalSettings = lField[original] as BiomeSettingsGeneration

				val originalCarverMap = eField[originalSettings] as Map<WorldGenStage.Features, ImmutableCollection<Supplier<WorldGenCarverWrapper<*>>>>
				val newCarverMap = HashMap<WorldGenStage.Features, ArrayList<Supplier<WorldGenCarverWrapper<*>>>>()

				originalCarverMap.forEach { (key, value) ->
					newCarverMap[key] = ArrayList(value.asList())
				}

				newCarverMap[WorldGenStage.Features.a]?.add { superCaveCarver }

				val newSettings = biomeSettingsGenerationConstructor.newInstance(
					dField[originalSettings],
					newCarverMap,
					fField[originalSettings],
					gField[originalSettings],
				)

				ret[id] = biomeBaseConstructor.newInstance(
					kField[original],
					pField[original],
					nField[original],
					oField[original],
					qField[original],
					newSettings,
					mField[original],
				)
			} catch (ex: Exception) {
				ex.printStackTrace()
			}
		}

		return ret
	}
}

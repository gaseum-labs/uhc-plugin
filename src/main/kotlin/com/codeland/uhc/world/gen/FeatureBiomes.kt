package com.codeland.uhc.world.gen

import com.codeland.uhc.world.gen.cave.WorldGenCavesSuperNether
import com.google.common.collect.ImmutableCollection
import net.minecraft.core.IRegistry
import net.minecraft.data.RegistryGeneration
import net.minecraft.data.worldgen.biome.BiomeRegistry
import net.minecraft.resources.ResourceKey
import net.minecraft.util.valueproviders.*
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.biome.BiomeSettingsGeneration
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.WorldGenStage
import net.minecraft.world.level.levelgen.carver.*
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap
import java.lang.reflect.Constructor
import java.util.function.*

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

	val biomeSettingsGenerationConstructor =
		BiomeSettingsGeneration::class.java.declaredConstructors[0] as Constructor<BiomeSettingsGeneration>

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
	val netherCavesMaster = WorldGenCavesSuperNether(CaveCarverConfiguration.a)
	val canyonCarverMaster = WorldGenCarverAbstract.c

	val caveLevels = arrayOf(
		caveCarverMaster.a(CaveCarverConfiguration(
			0.075f,
			UniformHeight.a(VerticalAnchor.a(6), VerticalAnchor.a(16)),
			ConstantFloat.a(0.3f),
			VerticalAnchor.b(6),
			false,
			CarverDebugSettings.a(false, Blocks.ne.blockData),
			ConstantFloat.a(3.4f),//UniformFloat.b(2.0f, 3.4f),
			ConstantFloat.a(2.0f),//UniformFloat.b(1.5f, 2.0f),
			ConstantFloat.a(0.0f)
		)),
		caveCarverMaster.a(CaveCarverConfiguration(
			0.100f,
			UniformHeight.a(VerticalAnchor.a(17), VerticalAnchor.a(32)),
			ConstantFloat.a(0.4f),
			VerticalAnchor.b(6),
			false,
			CarverDebugSettings.a(false, Blocks.ne.blockData),
			ConstantFloat.a(2.6f),//UniformFloat.b(1.5f, 2.6f),
			ConstantFloat.a(1.5f),//UniformFloat.b(1.0f, 1.5f),
			ConstantFloat.a(-0.5f)
		)),
		caveCarverMaster.a(CaveCarverConfiguration(
			0.125f,
			UniformHeight.a(VerticalAnchor.a(33), VerticalAnchor.a(48)),
			ConstantFloat.a(0.5f),
			VerticalAnchor.b(6),
			false,
			CarverDebugSettings.a(false, Blocks.ne.blockData),
			ConstantFloat.a(1.8f),//UniformFloat.b(1.25f, 1.8f),
			ConstantFloat.a(1.25f),//UniformFloat.b(1.0f, 1.25f),
			ConstantFloat.a(-1.0f)
		)),
		caveCarverMaster.a(CaveCarverConfiguration(
			0.150f,
			UniformHeight.a(VerticalAnchor.a(49), VerticalAnchor.a(64)),
			ConstantFloat.a(0.5f),
			VerticalAnchor.b(6),
			false,
			CarverDebugSettings.a(false, Blocks.ne.blockData),
			ConstantFloat.a(1.0f),
			ConstantFloat.a(1.0f),
			ConstantFloat.a(-1.0f)
		)),
	)

	val superCanyonCarver = canyonCarverMaster.a(
		CanyonCarverConfiguration(
			0.02F,
			UniformHeight.a(VerticalAnchor.a(0), VerticalAnchor.a(48)),
			ConstantFloat.a(3.0F),
			VerticalAnchor.b(6),
			false,
			CarverDebugSettings.a(false, Blocks.nf.blockData),
			UniformFloat.b(-2.0F, 2.0F),
			CanyonCarverConfiguration.a(
				UniformFloat.b(0.25F, 1.75F),
				TrapezoidFloat.a(0.0F, 5.0F, 2.0F),
				2,
				UniformFloat.b(0.25F, 1.75F),
				1.1f,
				0.5F
			)
		)
	)

	val netherSuperCaveCarver = netherCavesMaster.a(
		CaveCarverConfiguration(
			1.0f,
			BiasedToBottomHeight.a(VerticalAnchor.a(-8), VerticalAnchor.a(32), 8),
			ConstantFloat.a(0.0f),
			VerticalAnchor.b(6),
			false,
			CarverDebugSettings.a(false, Blocks.ne.blockData),
			ConstantFloat.a(2.0f),
			ConstantFloat.a(2.0f),
			UniformFloat.b(-1.0f, 0.0f)
		)
	)

	val netherUpperCaveCarver = netherCavesMaster.a(
		CaveCarverConfiguration(
			0.45f,
			UniformHeight.a(VerticalAnchor.a(29), VerticalAnchor.a(112)),
			ConstantFloat.a(2.5f),
			VerticalAnchor.b(6),
			false,
			CarverDebugSettings.a(false, Blocks.ne.blockData),
			ConstantFloat.a(1.0f),
			ConstantFloat.a(1.0f),
			ConstantFloat.a(-1.0f)
		)
	)

	val biomes = genBiomes()

	fun isNetherBiome(no: Int): Boolean {
		return when (no) {
			BiomeNo.NETHER_WASTES,
			BiomeNo.SOUL_SAND_VALLEY,
			BiomeNo.CRIMSON_FOREST,
			BiomeNo.WARPED_FOREST,
			BiomeNo.BASALT_DELTAS,
			-> true
			else -> false
		}
	}

	fun genBiomes(): Map<Int, BiomeBase> {
		val biomeMap = biomeMapField[null] as Int2ObjectMap<ResourceKey<BiomeBase>>
		val biomeRegistry = biomeRegistryField[null] as IRegistry<BiomeBase>

		val ret = HashMap<Int, BiomeBase>()

		biomeMap.forEach { (id, key) ->
			val original = biomeRegistry.d(key)
			val originalSettings = lField[original] as BiomeSettingsGeneration

			val originalCarverMap =
				eField[originalSettings] as Map<WorldGenStage.Features, ImmutableCollection<Supplier<WorldGenCarverWrapper<*>>>>
			val newCarverMap = HashMap<WorldGenStage.Features, ArrayList<Supplier<WorldGenCarverWrapper<*>>>>()

			originalCarverMap.forEach { (key, value) ->
				newCarverMap[key] = if (isNetherBiome(id)) {
					arrayListOf(
						Supplier { netherSuperCaveCarver },
						Supplier { netherUpperCaveCarver },
					)
				} else {
					arrayListOf(
						Supplier { caveLevels[0] },
						Supplier { caveLevels[1] },
						Supplier { caveLevels[2] },
						Supplier { caveLevels[3] },
						Supplier { superCanyonCarver },
					)
				}
			}

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
		}

		return ret
	}
}

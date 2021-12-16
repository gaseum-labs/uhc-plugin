package com.codeland.uhc.world.gen

import com.codeland.uhc.world.gen.cave.WorldGenCavesSuperNether
import net.minecraft.util.valueproviders.*
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.carver.*
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration.a
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight

object ModifiedBiomesRegistry {
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
			a(
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
			UniformHeight.a(VerticalAnchor.a(-8), VerticalAnchor.a(32)),
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

	val featureBiomes = ModifiedBiomes.genBiomes(replaceFeatures = true, replaceMobs = true)
}

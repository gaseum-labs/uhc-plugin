package com.codeland.uhc.world.gen

import com.codeland.uhc.world.gen.cave.WorldGenCavesSuperNether
import net.minecraft.data.worldgen.Carvers
import net.minecraft.util.valueproviders.*
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.carver.*
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration.CanyonShapeConfiguration
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight

object ModifiedBiomesRegistry {
	val caveCarverMaster = WorldCarver.CAVE
	val netherCavesMaster = WorldGenCavesSuperNether()
	val canyonCarverMaster = WorldCarver.CANYON

	val caveLevels = arrayOf(
		caveCarverMaster.configured(CaveCarverConfiguration(
			0.075f,
			UniformHeight.of(VerticalAnchor.aboveBottom(6), VerticalAnchor.aboveBottom(16)),
			ConstantFloat.of(0.3f),
			VerticalAnchor.aboveBottom(6),
			true,
			ConstantFloat.of(3.4f),
			ConstantFloat.of(2.0f),
			ConstantFloat.of(0.0f)
		)),
		caveCarverMaster.configured(CaveCarverConfiguration(
			0.100f,
			UniformHeight.of(VerticalAnchor.aboveBottom(17), VerticalAnchor.aboveBottom(32)),
			ConstantFloat.of(0.4f),
			VerticalAnchor.aboveBottom(6),
			true,
			ConstantFloat.of(2.6f),
			ConstantFloat.of(1.5f),
			ConstantFloat.of(-0.5f)
		)),
		caveCarverMaster.configured(CaveCarverConfiguration(
			0.125f,
			UniformHeight.of(VerticalAnchor.aboveBottom(33), VerticalAnchor.aboveBottom(48)),
			ConstantFloat.of(0.5f),
			VerticalAnchor.aboveBottom(6),
			true,
			ConstantFloat.of(1.8f),
			ConstantFloat.of(1.25f),
			ConstantFloat.of(-1.0f)
		)),
		caveCarverMaster.configured(CaveCarverConfiguration(
			0.150f,
			UniformHeight.of(VerticalAnchor.aboveBottom(49), VerticalAnchor.aboveBottom(64)),
			ConstantFloat.of(0.5f),
			VerticalAnchor.aboveBottom(6),
			true,
			ConstantFloat.of(1.0f),
			ConstantFloat.of(1.0f),
			ConstantFloat.of(-1.0f)
		)),
	)

	val superCanyonCarver = canyonCarverMaster.configured(
		CanyonCarverConfiguration(
			0.02F,
			UniformHeight.of(VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(48)),
			ConstantFloat.of(3.0F),
			VerticalAnchor.aboveBottom(6),
			CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()),
			UniformFloat.of(-2.0F, 2.0F),
			CanyonShapeConfiguration(
				UniformFloat.of(0.25F, 1.75F),
				TrapezoidFloat.of(0.0F, 5.0F, 2.0F),
				2,
				UniformFloat.of(0.25F, 1.75F),
				1.1f,
				0.5F
			)
		)
	)

	val netherSuperCaveCarver = netherCavesMaster.configured(
		CaveCarverConfiguration(
			1.0f,
			UniformHeight.of(VerticalAnchor.aboveBottom(-8), VerticalAnchor.aboveBottom(32)),
			ConstantFloat.of(0.0f),
			VerticalAnchor.aboveBottom(6),
			false,
			ConstantFloat.of(2.0f),
			ConstantFloat.of(2.0f),
			UniformFloat.of(-1.0f, 0.0f)
		)
	)

	val netherUpperCaveCarver = netherCavesMaster.configured(
		CaveCarverConfiguration(
			0.45f,
			UniformHeight.of(VerticalAnchor.aboveBottom(29), VerticalAnchor.aboveBottom(112)),
			ConstantFloat.of(2.5f),
			VerticalAnchor.aboveBottom(6),
			false,
			ConstantFloat.of(1.0f),
			ConstantFloat.of(1.0f),
			ConstantFloat.of(-1.0f)
		)
	)

	val featureBiomes = ModifiedBiomes.genBiomes(replaceFeatures = true, replaceMobs = true)
}

package org.gaseumlabs.uhc.world.gen

import org.gaseumlabs.uhc.world.gen.cave.WorldGenCavesSuperNether
import net.minecraft.data.worldgen.Carvers
import net.minecraft.util.valueproviders.*
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.carver.*
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration.CanyonShapeConfiguration
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight

object CustomCarvers {
	val caveCarverMaster = WorldCarver.CAVE
	val netherCavesMaster = WorldGenCavesSuperNether()
	val canyonCarverMaster = WorldCarver.CANYON

	val newUhcCarver = caveCarverMaster.configured(CaveCarverConfiguration(
		0.6f, /* commonness */
		UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(90)), /* y levels */
		UniformFloat.of(0.0f, 2.0f), /* y scale */
		VerticalAnchor.aboveBottom(10), /* lava fill */
		false,
		UniformFloat.of(1.0f, 2.0f), /* horizontal radius */
		UniformFloat.of(1.0f, 2.0f), /* vertical radius */
		UniformFloat.of(-1.0f, 0.0f), /* floor level */
	))

	val superCanyonCarver = canyonCarverMaster.configured(
		CanyonCarverConfiguration(
			0.05F,
			UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.absolute(90)),
			UniformFloat.of(1.5f, 3.0f),
			VerticalAnchor.aboveBottom(10),
			CarverDebugSettings.of(false, Blocks.OAK_BUTTON.defaultBlockState()),
			UniformFloat.of(-1.0F, 1.0F),
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
}

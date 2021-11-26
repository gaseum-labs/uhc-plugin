package com.codeland.uhc.world.gen.cave

import com.google.common.collect.ImmutableSet
import com.mojang.serialization.Codec
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration
import net.minecraft.world.level.levelgen.carver.WorldGenCaves
import net.minecraft.world.level.material.FluidType
import net.minecraft.world.level.material.FluidTypes

class WorldGenCavesSuperNether(codec: Codec<CaveCarverConfiguration>) : WorldGenCaves(codec) {
	init {
		k = ImmutableSet.of(
			Blocks.b,
			Blocks.c,
			Blocks.e,
			Blocks.g,
			Blocks.j,
			Blocks.k,
			/* -- */
			Blocks.l,
			Blocks.i,
			Blocks.cT,
			Blocks.cU,
			Blocks.cV,
			Blocks.mI,
			Blocks.mz,
			Blocks.iY,
			Blocks.mB,
			Blocks.cW,
			Blocks.nD,
			/* -- */
			Blocks.B
		)
		l = ImmutableSet.of<FluidType>(FluidTypes.e, FluidTypes.c)
	}
}
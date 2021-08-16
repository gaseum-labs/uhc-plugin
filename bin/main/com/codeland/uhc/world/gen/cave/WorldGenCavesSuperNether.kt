package com.codeland.uhc.world.gen.cave

import com.google.common.collect.ImmutableSet
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPosition
import net.minecraft.core.BlockPosition.MutableBlockPosition
import net.minecraft.world.level.biome.BiomeBase
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.IChunkAccess
import net.minecraft.world.level.levelgen.Aquifer
import net.minecraft.world.level.levelgen.carver.CarvingContext
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration
import net.minecraft.world.level.levelgen.carver.WorldGenCaves
import net.minecraft.world.level.material.FluidType
import net.minecraft.world.level.material.FluidTypes
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.mutable.MutableBoolean
import java.util.*
import java.util.function.Function

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
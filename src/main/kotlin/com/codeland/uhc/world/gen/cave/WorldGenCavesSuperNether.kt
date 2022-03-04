package com.codeland.uhc.world.gen.cave

import com.google.common.collect.ImmutableSet
import com.mojang.serialization.Codec
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.CarvingMask
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.levelgen.Aquifer
import net.minecraft.world.level.levelgen.carver.*
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import java.util.*
import java.util.function.Function

class WorldGenCavesSuperNether() : CaveWorldCarver(
	CaveCarverConfiguration.CODEC
) {
	init {
		replaceableBlocks = ImmutableSet.of(
			Blocks.STONE,
			Blocks.GRANITE,
			Blocks.DIORITE,
			Blocks.ANDESITE,
			Blocks.DIRT,
			Blocks.COARSE_DIRT,
			Blocks.PODZOL,
			Blocks.GRASS_BLOCK,
			Blocks.NETHERRACK,
			Blocks.SOUL_SAND,
			Blocks.SOUL_SOIL,
			Blocks.CRIMSON_NYLIUM,
			Blocks.WARPED_NYLIUM,
			Blocks.NETHER_WART_BLOCK,
			Blocks.WARPED_WART_BLOCK,
			Blocks.BASALT,
			Blocks.BLACKSTONE
		)
		liquids = ImmutableSet.of<Fluid>(Fluids.WATER, Fluids.LAVA)
	}
}

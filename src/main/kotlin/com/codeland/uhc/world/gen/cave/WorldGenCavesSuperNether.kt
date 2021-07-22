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

class WorldGenCavesSuperNether(
	codec: Codec<CaveCarverConfiguration>,
	val open: Boolean
) : WorldGenCaves(codec) {
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

	fun boundaryBlock(chunkAccess: IChunkAccess, blockPosition: BlockPosition): Boolean {
		val block = chunkAccess.getType(blockPosition).block
		return block === Blocks.a || block === Blocks.B
	}

	override fun a(
		var0: CarvingContext,
		var1: CaveCarverConfiguration,
		chunkAccess: IChunkAccess,
		var3: Function<BlockPosition, BiomeBase>,
		var4: BitSet,
		var5: Random,
		carvePosition: MutableBlockPosition,
		var7: MutableBlockPosition,
		var8: Aquifer,
		var9: MutableBoolean
	): Boolean {
		val block = chunkAccess.getType(carvePosition)

		return if (this.a(block)) {
			val carveBlock = if (
				!open && (
					boundaryBlock(chunkAccess, carvePosition.up()) ||
					boundaryBlock(chunkAccess, carvePosition.down()) ||
					boundaryBlock(chunkAccess, carvePosition.east()) ||
					boundaryBlock(chunkAccess, carvePosition.west()) ||
					boundaryBlock(chunkAccess, carvePosition.north()) ||
					boundaryBlock(chunkAccess, carvePosition.south())
				)
			) {
				Blocks.cT.blockData
			} else {
				h
			}

			chunkAccess.setType(carvePosition, carveBlock, false)
			true

		} else {
			false
		}
	}
}
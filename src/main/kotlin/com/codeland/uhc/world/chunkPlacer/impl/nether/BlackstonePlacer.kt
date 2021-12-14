package com.codeland.uhc.world.chunkPlacer.impl.nether

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import kotlin.random.Random

class BlackstonePlacer : ImmediateChunkPlacer(1) {
	override fun place(chunk: Chunk) {
		val random = Random(chunk.world.seed.xor(chunk.chunkKey))

		for (y in 30 downTo 26) {
			doLayer(chunk, y, random, 1 - Util.invInterp(25.0f, 31.0f, y.toFloat()))
		}

		for (y in 25 downTo 2) {
			doLayer(chunk, y)
		}
	}

	fun setType(block: Block) {
		when {
			block.getRelative(BlockFace.UP).type === Material.FIRE -> block.setType(Material.MAGMA_BLOCK, false)
			block.type === Material.NETHERRACK -> block.setType(Material.BLACKSTONE, false)
			block.type === Material.NETHER_GOLD_ORE -> block.setType(Material.GILDED_BLACKSTONE, false)
		}
	}

	fun doLayer(chunk: Chunk, y: Int, random: Random, chance: Float) {
		for (x in 0..15) for (z in 0..15) {
			if (random.nextFloat() < chance) {
				setType(chunk.getBlock(x, y, z))
			}
		}
	}

	fun doLayer(chunk: Chunk, y: Int) {
		for (x in 0..15) for (z in 0..15) {
			setType(chunk.getBlock(x, y, z))
		}
	}
}

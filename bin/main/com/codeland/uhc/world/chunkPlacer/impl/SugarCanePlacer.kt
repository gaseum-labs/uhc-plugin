package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

class SugarCanePlacer(size: Int, val lowBound: Int, val highBound: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return world.isChunkGenerated(chunkX + 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX + 1, chunkZ - 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ - 1)
	}

	fun gensPerChunk(chunk: Chunk): Int {
		return when (chunk.getBlock(7, lowBound, 7).biome) {
			Biome.SWAMP,
			Biome.SWAMP_HILLS,
			Biome.STONE_SHORE,
			Biome.DESERT,
			Biome.DESERT_HILLS,
			Biome.DESERT_LAKES -> 2
			else -> 1
		}
	}

	fun isWater(block: Block): Boolean {
		return block.type == Material.WATER || block.type == Material.ICE
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		for (i in 0 until gensPerChunk(chunk)) {
			randomPosition(chunk, lowBound, highBound) { block, x, y, z ->
				val down = block.getRelative(BlockFace.DOWN)

				if (
					(block.type.isAir || block.type == Material.GRASS) && (
						down.type == Material.GRASS_BLOCK ||
						down.type == Material.DIRT ||
						down.type == Material.SAND ||
						down.type == Material.PODZOL ||
						down.type == Material.RED_SAND ||
						down.type == Material.COARSE_DIRT
					) && (
						isWater(down.getRelative(BlockFace.WEST)) ||
						isWater(down.getRelative(BlockFace.EAST)) ||
						isWater(down.getRelative(BlockFace.NORTH)) ||
						isWater(down.getRelative(BlockFace.SOUTH))
					)
				) {
					var current = block

					for (i in 0 until 3) {
						current.setType(Material.SUGAR_CANE, false)
						current = current.getRelative(BlockFace.UP)
					}

					true
				} else {
					false
				}
			}
		}
	}

	companion object {
		fun removeSugarCane(chunk: Chunk) {
			for (x in 0..15) {
				for (z in 0..15) {
					for (y in 60..86) {
						val block = chunk.getBlock(x, y, z)
						if (block.type == Material.SUGAR_CANE) block.setType(Material.AIR, false)
					}
				}
			}
		}
	}
}

package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Axis
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Orientable
import org.bukkit.block.data.type.Lantern
import java.awt.Event.UP
import java.util.concurrent.Delayed

class SugarCanePlacer(size: Int, uniqueSeed: Int, val lowBound: Int, val highBound: Int) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return world.isChunkGenerated(chunkX + 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX + 1, chunkZ - 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ - 1)
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, lowBound, highBound) { block, x, y, z ->
			val down = block.getRelative(BlockFace.DOWN)

			if (
				(block.type == Material.AIR || block.type == Material.CAVE_AIR || block.type == Material.GRASS || (block.type == Material.WATER && GameRunner.waterWorld)) &&
				(
					down.type == Material.GRASS_BLOCK ||
					down.type == Material.DIRT ||
					down.type == Material.SAND ||
					down.type == Material.PODZOL ||
					down.type == Material.RED_SAND ||
					down.type == Material.COARSE_DIRT
				) &&
				(
					down.getRelative(BlockFace.WEST).type == Material.WATER ||
					down.getRelative(BlockFace.EAST).type == Material.WATER ||
					down.getRelative(BlockFace.NORTH).type == Material.WATER ||
					down.getRelative(BlockFace.SOUTH).type == Material.WATER
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

package com.codeland.uhc.world.chunkPlacer.impl

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
import java.lang.Math.pow
import java.lang.Math.sqrt

class BricksPlacer(size: Int, uniqueSeed: Int) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -1..1) {
			for (j in -1..1) {
				if (!world.isChunkGenerated(chunkX + i, chunkZ + j)) return false
			}
		}

		return true
	}

	override fun place(chunk: Chunk) {
		randomPosition(chunk, 8, 99) { block, x, y, z ->
			val world = chunk.world
			val maxDistance = sqrt(pow(5.0, 2.0) * 3)

			if (block.type == Material.STONE) {
				for (i in -5..5) {
					for (j in -5..5) {
						for (k in -5..5) {
							val placeBlock = world.getBlockAt(chunk.x * 16 + i + x, j + y, chunk.z * 16 + k + z)

							if (placeBlock.type == Material.STONE) {
								val distance = sqrt((i * i) + (j * j) + (k * k.toDouble()))
								val chance = (maxDistance - distance) / maxDistance

								if (Math.random() < chance) {
									val random = Math.random()

									placeBlock.setType(when {
										random < 0.33 -> Material.STONE_BRICKS
										random < 0.6 -> Material.MOSSY_STONE_BRICKS
										else -> Material.CRACKED_STONE_BRICKS
									}, false)
								}
							}

						}
					}
				}

				true

			} else {
				false
			}
		}
	}
}

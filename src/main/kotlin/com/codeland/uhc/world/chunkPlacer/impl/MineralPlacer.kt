package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.OreFix
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World

class MineralPlacer(size: Int, uniqueSeed: Int) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -3..3)
			if (
				!world.isChunkGenerated(chunkX + i, chunkZ) ||
				!world.isChunkGenerated(chunkX, chunkZ + i)
			) return false

		return true
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		fun doChunkLayer(y: Int, chance: Float) {
			for (x in 0..15) {
				for (z in 0..15) {
					val block = chunk.getBlock(x, y, z)

					if (block.type == Material.STONE && Math.random() < chance) {
						val random = Math.random()

						val replaceType: Material
						val moveX: Int
						val moveZ: Int

						when {
							random < 0.25 -> { replaceType = Material.GRANITE; moveX = 0; moveZ = -4 }
							random < 0.5 -> { replaceType = Material.DIORITE; moveX = 4; moveZ = 0 }
							random < 0.75 -> { replaceType = Material.ANDESITE; moveX = 0; moveZ = 4 }
							else -> { replaceType = Material.DIRT; moveX = -4; moveZ = 0 }
						}

						val searchTries = Util.lowBiasRandom(12)

						var worldX = block.x
						var worldZ = block.z

						for (i in 0 until searchTries) {
							worldX += moveX
							worldZ += moveZ

							if (chunk.world.getBlockAt(worldX, y, worldZ).isPassable) {
								block.setType(replaceType, false)
								break
							}
						}
					}
				}
			}
		}

		for (y in (OreFix.highLimit + 1)..OreFix.gradientLimit) {
			doChunkLayer(y, (1 - Util.invInterp(OreFix.highLimit.toFloat(), OreFix.gradientLimit + 1f, y.toFloat())) / 2)
		}

		for (y in 1..OreFix.highLimit) {
			doChunkLayer(y, 0.5f)
		}
	}
}

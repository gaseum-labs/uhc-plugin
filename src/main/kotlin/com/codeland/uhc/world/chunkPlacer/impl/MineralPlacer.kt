package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacerHolder.type.OreFix
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import com.codeland.uhc.world.chunkPlacerHolder.type.OreFix.Companion.random
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import kotlin.random.Random

class MineralPlacer(size: Int) : DelayedChunkPlacer(size) {
	val random = Random(size + uniqueSeed)

	val BASE_CHANCE = 0.25f

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

					if (
						(block.type == Material.STONE || block.type == Material.DEEPSLATE) &&
						random.nextFloat() < chance
					) {
						val replaceType: Material
						val moveX: Int
						val moveZ: Int

						when (random.nextInt(0, 4)) {
							0 -> { replaceType = Material.GRANITE; moveX = 0; moveZ = -4 }
							1 -> { replaceType = Material.DIORITE; moveX = 4; moveZ = 0 }
							2 -> { replaceType = Material.ANDESITE; moveX = 0; moveZ = 4 }
							else -> { replaceType = Material.DIRT; moveX = -4; moveZ = 0 }
						}

						var worldX = block.x
						var worldZ = block.z

						for (i in 0 until 12) {
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

		for (y in (OreFix.HEIGHT_LIMIT + 1)..OreFix.GRADIENT_LIMIT) {
			doChunkLayer(y, (1 - Util.invInterp(OreFix.HEIGHT_LIMIT.toFloat() - 1f, OreFix.GRADIENT_LIMIT + 2f, y.toFloat())) * BASE_CHANCE)
		}

		for (y in 1..OreFix.HEIGHT_LIMIT) {
			doChunkLayer(y, BASE_CHANCE)
		}
	}
}

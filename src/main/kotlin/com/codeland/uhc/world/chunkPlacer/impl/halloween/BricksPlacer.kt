package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import kotlin.math.pow
import kotlin.math.sqrt

class BricksPlacer(size: Int, uniqueSeed: Int) : DelayedChunkPlacer(size, uniqueSeed) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (i in -1..1) {
			for (j in -1..1) {
				if (!world.isChunkGenerated(chunkX + i, chunkZ + j)) return false
			}
		}

		return true
	}

	private val replaceable = arrayOf(
		Material.STONE,
		Material.DIRT,
		Material.ANDESITE,
		Material.GRANITE,
		Material.DIORITE,
		Material.GRASS_BLOCK,
		Material.NETHERRACK,
		Material.WARPED_NYLIUM,
		Material.CRIMSON_NYLIUM
	)

	init { replaceable.sort() }

	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 8, 99) { block, x, y, z ->
			val world = chunk.world
			val maxDistance = sqrt(5.0.pow(2.0) * 3)

			if (Util.binarySearch(block.type, replaceable)) {
				for (i in -5..5) {
					for (j in -5..5) {
						for (k in -5..5) {
							val placeBlock = world.getBlockAt(chunk.x * 16 + i + x, j + y, chunk.z * 16 + k + z)

							if (Util.binarySearch(placeBlock.type, replaceable)) {
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

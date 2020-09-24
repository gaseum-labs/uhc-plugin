package com.codeland.uhc.world

import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import kotlin.math.ceil
import kotlin.math.floor

object CaveLocator {
	val gradientLimit = 42
	val highLimit = 32

	val minerals = arrayOf(
		Material.GRANITE,
		Material.DIORITE,
		Material.ANDESITE,
		Material.DIRT
	)

	fun removeMinerals(chunk: Chunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				/* remove minerals in a gradient */
				for (y in (highLimit + 1)..gradientLimit) {
					val chance = Util.invInterp(highLimit.toFloat(), gradientLimit + 1f, y.toFloat())

					if (Math.random() > chance) {
						val block = chunk.getBlock(x, y, z)

						if (minerals.contains(block.type))
							block.setType(Material.STONE, false)
					}
				}

				/* remove all minerals below and at high limit */
				for (y in 1..highLimit) {
					val block = chunk.getBlock(x, y, z)

					if (minerals.contains(block.type))
						block.setType(Material.STONE, false)
				}
			}
		}
	}

	fun locateChunkCaves(chunk: Chunk, radius: Int) {
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

							if (worldX > radius || worldZ > radius || worldX < -radius || worldZ < -radius)
								break

							if (chunk.world.getBlockAt(worldX, y, worldZ).isPassable) {
								block.setType(replaceType, false)
								break
							}
						}
					}
				}
			}
		}

		for (y in (highLimit + 1)..gradientLimit) {
			doChunkLayer(y, (1 - Util.invInterp(highLimit.toFloat(), gradientLimit + 1f, y.toFloat())) / 2)
		}

		for (y in 1..highLimit) {
			doChunkLayer(y, 0.5f)
		}
	}

	/*fun locateCaves(world: World, radius: Int, onComplete: () -> Unit) {
		var chunkIndex = 0

		val left = floor(-radius / 16.0).toInt()
		val right = ceil(radius / 16.0).toInt()

		val width = (right - left) + 1
		val total = width * width

		fun doChunk() {
			val chunkX = (chunkIndex % width) + left
			val chunkZ = (chunkIndex / width) + left

			world.getChunkAtAsync(chunkX, chunkZ).thenAccept { chunk ->
				for (x in 0..15) {
					for (z in 0..15) {
						for (y in 1..highLimit) {
							val block = chunk.getBlock(x, y, z)

							if (block.type == Material.STONE && Math.random() < 0.5) {
								val random = Math.random()

								val replaceType: Material
								val moveX: Int
								val moveZ: Int

								when {
									random < 0.25 -> { replaceType = Material.DIAMOND_BLOCK; moveX = 0; moveZ = -4 }
									random < 0.5 -> { replaceType = Material.GOLD_BLOCK; moveX = 4; moveZ = 0 }
									random < 0.75 -> { replaceType = Material.EMERALD_BLOCK; moveX = 0; moveZ = 4 }
									else -> { replaceType = Material.LAPIS_BLOCK; moveX = -4; moveZ = 0 }
								}

								val searchTries = Util.lowBiasRandom(12)

								var worldX = block.x
								var worldZ = block.z

								for (i in 0 until searchTries) {
									worldX += moveX
									worldZ += moveZ

									if (worldX > radius || worldZ > radius || worldX < -radius || worldZ < -radius)
										break

									val blockAt = world.getBlockAt(worldX, y, worldZ)
									if (!blockAt.chunk.isLoaded) blockAt.chunk.load(true)

									if (blockAt.isPassable) {
										block.setType(replaceType, false)
										break
									}
								}
							}
						}
					}
				}

				Util.log("completed chunk $chunkIndex out of $total")

				if (++chunkIndex == total) {
					onComplete()
				} else {
					doChunk()
				}
			}
		}

		doChunk()
	}*/
}

package com.codeland.uhc.world.chunkPlacer.impl.structure

import com.codeland.uhc.extensions.BlockFaceExtensions.left
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Stairs
import org.bukkit.util.Vector
import kotlin.random.Random

class TowerPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		val floorY = determineFloorY(chunk) ?: return

		val stories = random.nextInt(8, 24)

		/* below the first floor */
		for (x in 0..15) for (z in 0..15) {
			for (y in floorY - 1 downTo floorY - MAX_BELOW) {
				val block = chunk.getBlock(x, y, z)

				/* once we hit solid ground, put 1 more below then stop */
				if (!block.isPassable) {
					block.getRelative(BlockFace.DOWN).setType(wallBlock(), false)
					break
				}

				block.setType(wallBlock(), false)
			}
		}

		/* each floor */
		for (story in 0 until stories) {
			for (x in 2..13) {
				for (z in 2..13) {
					/* create floor */
					for (y in floorY + (story * STORY_HEIGHT) .. floorY + (story * STORY_HEIGHT) + 1) {
						chunk.getBlock(x, y, z).setType(floorBlock(), false)
					}

					/* air in the room */
					for (y in floorY + (story * STORY_HEIGHT) + 2 .. floorY + (story * STORY_HEIGHT) + 4) {
						chunk.getBlock(x, y, z).setType(Material.AIR, false)
					}
				}
			}

			/* create walls */
			for (y in floorY + (story * STORY_HEIGHT) .. floorY + (story * STORY_HEIGHT) + 4) {
				for (x in 0..15) {
					for (z in 0..1) chunk.getBlock(x, y, z).setType(wallBlock(), false)
					for (z in 14..15) chunk.getBlock(x, y, z).setType(wallBlock(), false)
				}
				for (z in 2..13) {
					for (x in 0..1) chunk.getBlock(x, y, z).setType(wallBlock(), false)
					for (x in 14..15) chunk.getBlock(x, y, z).setType(wallBlock(), false)
				}
			}
		}

		/* create roof */
		for (y in floorY + (stories * STORY_HEIGHT) .. floorY + (stories * STORY_HEIGHT) + 1) {
			for (x in 0..15) {
				for (z in 0..15) {
					chunk.getBlock(x, y, z).setType(wallBlock(), false)
				}
			}
		}

		/* cut in the front door */
		for (direction in BlockFace.values().copyOfRange(0, 4)) {
			for (y in floorY + 2..floorY + 4) {
				for (x in 0..1) {
					for (z in 0..1) {
						chunk.getBlock(7 + (direction.modX * 7) + x, y, 7 + (direction.modZ * 7) + z).setType(Material.AIR, false)
					}
				}
			}
		}

		/* build stairs */
		for (story in 0 until stories - 1) {
			val outward = BlockFace.values()[Random.nextInt(4)]
			val stairward = outward.left()

			val start = Vector(7.5, 0.0, 7.5).add(outward.direction.multiply(5.5))

			var stairBlock = chunk.getBlock(start.blockX, floorY + (story * STORY_HEIGHT) + 2, start.blockZ)

			for (i in 0..4) {
				stairBlock.setType(Material.COBBLED_DEEPSLATE_STAIRS, false)
				val data = stairBlock.blockData as Stairs
				data.facing = stairward
				stairBlock.blockData = data

				for (y in floorY + (story * STORY_HEIGHT) + 2 + i + 1..floorY + (story * STORY_HEIGHT) + 6) {
					chunk.getBlock(Util.mod(stairBlock.x, 16), y, Util.mod(stairBlock.z, 16)).setType(Material.AIR, false)
				}

				stairBlock = stairBlock.getRelative(stairward.modX, 1, stairward.modZ)
			}
		}
	}

	fun findTop(chunk: Chunk, x: Int, z: Int): Int? {
		for (y in 100 downTo 50) {
			val block = chunk.getBlock(x, y, z)

			if (block.isLiquid) return null

			if (
				block.type === Material.GRASS_BLOCK ||
				block.type === Material.DIRT ||
				block.type === Material.COARSE_DIRT ||
				block.type === Material.STONE ||
				block.type === Material.SAND ||
				block.type === Material.PODZOL ||
				block.type === Material.RED_SAND ||
				block.type === Material.TERRACOTTA
			) return y
		}

		return null
	}

	fun determineFloorY(chunk: Chunk): Int? {
		var total = 0
		var misses = 0

		for (x in 0 until 8) {
			for (z in 0 until 8) {
				val y = findTop(chunk, x, z)

				if (y == null) {
					if (++misses == 8) return null
				} else {
					total += y
				}
			}
		}

		return total / 64
	}

	companion object {
		val STORY_HEIGHT = 5
		val MAX_BELOW = 20

		val floorBlocks = arrayOf(
			Material.SPRUCE_PLANKS,
			Material.BLACKSTONE,
			Material.DRIPSTONE_BLOCK
		)

		val wallBlocks = arrayOf(
			Material.COBBLED_DEEPSLATE,
			Material.COBBLESTONE,
			Material.TUFF,
			Material.ANDESITE,
		)

		fun floorBlock(): Material {
			return floorBlocks[random.nextInt(floorBlocks.size)]
		}

		fun wallBlock(): Material {
			return wallBlocks[random.nextInt(wallBlocks.size)]
		}
	}
}

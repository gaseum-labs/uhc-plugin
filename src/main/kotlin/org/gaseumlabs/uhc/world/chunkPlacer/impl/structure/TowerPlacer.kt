package org.gaseumlabs.uhc.world.chunkPlacer.impl.structure

import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.extensions.BlockFaceExtensions.left
import org.gaseumlabs.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.CreatureSpawner
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.EntityType
import org.bukkit.inventory.Inventory
import org.bukkit.util.Vector
import kotlin.random.Random

class TowerPlacer : ChunkPlacer(1, 5) {
	override fun place(chunk: Chunk) {
		val floorY = determineFloorY(chunk) ?: return

		val numStories = Random.nextInt(8, 24)

		fun storyY(story: Int) = floorY + (story * STORY_HEIGHT)

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
		for (story in 0 until numStories) {
			for (x in 2..13) {
				for (z in 2..13) {
					/* create floor */
					for (y in storyY(story)..storyY(story) + 1) {
						chunk.getBlock(x, y, z).setType(floorBlock(), false)
					}

					/* air in the room */
					for (y in storyY(story) + 2..storyY(story) + 4) {
						chunk.getBlock(x, y, z).setType(Material.AIR, false)
					}
				}
			}

			/* create walls */
			for (y in storyY(story)..storyY(story) + 4) {
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
		for (y in storyY(numStories)..storyY(numStories) + 1) {
			for (x in 0..15) {
				for (z in 0..15) {
					chunk.getBlock(x, y, z).setType(wallBlock(), false)
				}
			}
		}

		/* cut in the front door */
		for (direction in BlockFace.values().copyOfRange(0, 4)) {
			for (y in storyY(0) + 2..storyY(0) + 4) {
				for (x in 0..1) {
					for (z in 0..1) {
						chunk.getBlock(7 + (direction.modX * 7) + x, y, 7 + (direction.modZ * 7) + z)
							.setType(Material.AIR, false)
					}
				}
			}
		}

		/* fill in stuff into the stories */
		for (story in 0 until numStories) {
			/* place stairs */
			val outward = BlockFace.values()[Random.nextInt(4)]
			val stairward = outward.left()

			val start = Vector(7.5, 0.0, 7.5).add(outward.direction.multiply(5.5))

			var stairBlock = chunk.getBlock(start.blockX, storyY(story) + 2, start.blockZ)

			for (i in 0..4) {
				stairBlock.setType(Material.COBBLED_DEEPSLATE_STAIRS, false)
				val data = stairBlock.blockData as Stairs
				data.facing = stairward
				stairBlock.blockData = data

				for (y in storyY(story) + 3 + i..storyY(story) + 6) {
					chunk.getBlock(Util.mod(stairBlock.x, 16), y, Util.mod(stairBlock.z, 16))
						.setType(Material.AIR, false)
				}

				stairBlock = stairBlock.getRelative(stairward.modX, 1, stairward.modZ)
			}

			/* type of floor */
			fun placeChest(level: Int) {
				val x = Random.nextInt(2, 13)
				val z = Random.nextInt(2, 13)

				val floorBlock = chunk.getBlock(x, storyY(story) + 1, z)

				if (floorBlock.type !== Material.AIR && floorBlock.type !== Material.COBBLED_DEEPSLATE_STAIRS) {
					chunk.getBlock(x, storyY(story) + level, z).setType(Material.TRAPPED_CHEST, false)
				}
			}

			fun placeSpawner() {
				val raise = Random.nextInt(3)
				val x = Random.nextInt(3, 12)
				val z = Random.nextInt(3, 12)

				val block = chunk.getBlock(x, storyY(story) + 2 + raise, z)

				block.setType(Material.SPAWNER, false)
				val blockState = block.getState(false) as CreatureSpawner
				blockState.spawnedType = spawnedTypes[Random.nextInt(spawnedTypes.size)]

				for (y in storyY(story) + 2 + raise - 1 downTo storyY(story) + 2) {
					chunk.getBlock(x, y, z).setType(Material.MOSSY_COBBLESTONE, false)
				}
			}

			fun placePillar() {
				val x = Random.nextInt(2, 13)
				val z = Random.nextInt(2, 13)

				var floorBlock = chunk.getBlock(x, storyY(story) + 1, z)

				if (floorBlock.type !== Material.AIR && floorBlock.type !== Material.COBBLED_DEEPSLATE_STAIRS) {
					for (i in 0..2) {
						floorBlock = floorBlock.getRelative(BlockFace.UP)
						floorBlock.setType(wallBlock(), false)
					}
				}
			}

			fun fillWithJunk() {
				for (x in 2..13) {
					for (z in 2..13) {
						for (y in storyY(story) + 2..storyY(story) + 4) {
							when (Random.nextInt(32)) {
								0, 1, 2, 3, 4, 5 -> chunk.getBlock(x, y, z).setType(Material.COBBLESTONE, false)
								6 -> chunk.getBlock(x, y, z).setType(Material.INFESTED_COBBLESTONE, false)
							}
						}
					}
				}
			}

			fun tntFloor() {
				for (x in 2..13) {
					for (z in 2..13) {
						if (Random.nextInt(12) == 0) chunk.getBlock(x, storyY(story) + 1, z)
							.setType(Material.TNT, false)
						chunk.getBlock(x, storyY(story) + 2, z)
							.setType(if (Random.nextInt(5) == 0) Material.STONE_PRESSURE_PLATE else Material.REDSTONE_WIRE,
								false)
					}
				}
			}

			fun bisect() {
				for (x in 2..13) {
					for (z in 2..13) {
						val rand = Random.nextInt(6)

						chunk.getBlock(x, storyY(story) + 3, z).setType(when (rand) {
							0 -> Material.DARK_OAK_TRAPDOOR
							1 -> Material.SPRUCE_PLANKS
							2 -> Material.DARK_OAK_PLANKS
							3 -> Material.LADDER
							4 -> Material.STRIPPED_SPRUCE_LOG
							else -> Material.STRIPPED_DARK_OAK_LOG
						}, false)

						if (rand == 3) {
							val block = chunk.getBlock(x, storyY(story) + 4, z)
							block.setType(Material.SPRUCE_TRAPDOOR, false)
							val data = block.blockData as Bisected
							data.half = Bisected.Half.TOP
							block.blockData = data
						}
					}
				}
			}

			when (Random.nextInt(4)) {
				/* mob spawner nightmare */
				0 -> {
					placePillar()
					placePillar()
					placePillar()
					placePillar()
					placeSpawner()
					placeChest(2)
				}
				/* tnt trap */
				1 -> {
					tntFloor()
					placePillar()
					placePillar()
					placePillar()
					placePillar()
					placePillar()
					placePillar()
					placeChest(2)
				}
				/* infested */
				2 -> {
					fillWithJunk()
					placeSpawner()
					placeChest(2)
				}
				/* bisected */
				3 -> {
					bisect()
					placePillar()
					placePillar()
					placeChest(2)
					placeSpawner()
					placeChest(4)
				}
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

		val spawnedTypes = arrayOf(
			EntityType.CREEPER,
			EntityType.ZOMBIE,
			EntityType.SPIDER,
			EntityType.SKELETON,
		)

		fun floorBlock(): Material {
			return floorBlocks[Random.nextInt(floorBlocks.size)]
		}

		fun wallBlock(): Material {
			return wallBlocks[Random.nextInt(wallBlocks.size)]
		}

		fun fillChestContents(inventory: Inventory) {
			
		}
	}
}

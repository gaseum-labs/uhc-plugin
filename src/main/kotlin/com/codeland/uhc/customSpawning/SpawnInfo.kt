package com.codeland.uhc.customSpawning

import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

abstract class SpawnInfo {
	abstract fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>?

	open fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {}

	companion object {
		val leaves = arrayOf(
			Material.OAK_LEAVES,
			Material.SPRUCE_LEAVES,
			Material.BIRCH_LEAVES,
			Material.JUNGLE_LEAVES,
			Material.ACACIA_LEAVES,
			Material.DARK_OAK_LEAVES,
		)

		fun isWater(block: Block): Boolean {
			return block.type == Material.WATER ||
				block.type == Material.KELP ||
				block.type == Material.SEAGRASS ||
				block.type == Material.TALL_SEAGRASS ||
				((block.blockData as? Waterlogged)?.isWaterlogged == true)
		}

		fun spawnObstacle(block: Block): Boolean {
			return !block.isPassable || block.type == Material.LAVA
		}

		fun spawnFloor(block: Block): Boolean {
			return !block.isPassable && leaves.none { it === block.type }
		}

		fun animalSpawnFloor(block: Block): Boolean {
			return block.type === Material.GRASS_BLOCK || block.type === Material.SAND
		}

		fun regularAllowSpawn(block: Block, lightLevel: Int): Boolean {
			if (block.lightLevel > lightLevel) return false

			return spawnSpace(block, 1, 2, 1)
		}

		fun desert(biome: Biome) = biome === Biome.DESERT ||
			biome === Biome.DESERT_HILLS ||
			biome === Biome.DESERT_LAKES

		fun mountains(biome: Biome) = biome === Biome.MOUNTAINS ||
			biome === Biome.GRAVELLY_MOUNTAINS ||
			biome === Biome.WOODED_MOUNTAINS ||
			biome === Biome.MODIFIED_GRAVELLY_MOUNTAINS

		fun snowy(biome: Biome) = biome === Biome.SNOWY_TUNDRA ||
			biome === Biome.SNOWY_MOUNTAINS ||
			biome === Biome.ICE_SPIKES

		fun plains(biome: Biome) = biome === Biome.PLAINS ||
			biome === Biome.SUNFLOWER_PLAINS

		fun taiga(biome: Biome) = biome === Biome.GIANT_SPRUCE_TAIGA ||
			biome === Biome.GIANT_TREE_TAIGA ||
			biome === Biome.SNOWY_TAIGA ||
			biome === Biome.SNOWY_TAIGA_MOUNTAINS ||
			biome === Biome.TAIGA

		fun jungle(biome: Biome) = biome === Biome.JUNGLE ||
			biome === Biome.JUNGLE_EDGE ||
			biome === Biome.JUNGLE_HILLS ||
			biome === Biome.MODIFIED_JUNGLE_EDGE ||
			biome === Biome.MODIFIED_JUNGLE ||
			biome === Biome.BAMBOO_JUNGLE ||
			biome === Biome.BAMBOO_JUNGLE_HILLS

		fun animalAllowSpawn(type: EntityType, block: Block): Pair<EntityType, Boolean>? {
			return if (when (type) {
				EntityType.POLAR_BEAR,
				EntityType.DONKEY,
				EntityType.PANDA,
				EntityType.HORSE,
				EntityType.TURTLE -> animalAllowSpawn(block, 3, 2, 3)
				else -> animalAllowSpawn(block, 1, 2, 1)
			}) {
				Pair(type, false)
			} else {
				null
			}
		}

		fun animalAllowSpawn(block: Block, xBox: Int, yHeight: Int, zBox: Int): Boolean {
			return animalSpawnFloor(block.getRelative(BlockFace.DOWN)) && spawnBox(block, xBox, yHeight, zBox)
		}

		fun spawnSpace(block: Block, xBox: Int, yHeight: Int, zBox: Int): Boolean {
			return spawnFloor(block.getRelative(BlockFace.DOWN)) && spawnBox(block, xBox, yHeight, zBox)
		}

		fun spawnBox(block: Block, xBox: Int, yHeight: Int, zBox: Int): Boolean {
			val xRadius = (xBox - 1) / 2
			val zRadius = (zBox - 1) / 2

			/* in a radius around check if all empty */
			for (x in -xRadius..xRadius)
				for (z in -zRadius..zRadius)
					for (y in 0 until yHeight) {
						val block = block.world.getBlockAt(block.x + x, block.y + y, block.z + z)
						if (spawnObstacle(block) || isWater(block)) return false
					}

			return true
		}

		fun onCycle(spawnCycle: Int, n: Int): Boolean {
			return spawnCycle % n == n - 1
		}

		fun reg(entityType: EntityType): Pair<EntityType, Boolean>? {
			return Pair(entityType, false)
		}
	}
}

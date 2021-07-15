package com.codeland.uhc.customSpawning

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

abstract class SpawnInfo {
	abstract fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>?

	open fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {}

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

		fun regularAllowSpawn(block: Block, lightLevel: Int): Boolean {
			if (block.lightLevel > lightLevel) return false

			return spawnSpace(block, 1, 2, 1)
		}

		fun spawnSpace(block: Block, xBox: Int, yHeight: Int, zBox: Int): Boolean {
			val xRadius = (xBox - 1) / 2
			val zRadius = (zBox - 1) / 2

			/* standing on a solid block */
			if (!spawnFloor(block.getRelative(BlockFace.DOWN))) return false

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

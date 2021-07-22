package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.EntityType

class SpawnGhast : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (block.y > NETHER_CAVE_Y) {
			if (spawnSpace(block, 5, 4, 5)) reg(EntityType.GHAST) else null
		} else {
			if (spawnSpace(block, 3, 3, 3)) reg(EntityType.MAGMA_CUBE) else null
		}
	}
}

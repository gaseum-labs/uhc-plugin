package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.Blaze
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class SpawnBlaze : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (block.y > NETHER_CAVE_Y) {
			if (spawnSpace(block, 1, 2, 1))
				Pair(EntityType.BLAZE, true)
			else
				null
		} else {
			if (spawnSpace(block, 3, 3, 3))
				reg(EntityType.MAGMA_CUBE)
			else
				null
		}
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		(entity as? Blaze)?.removeWhenFarAway = false
	}
}
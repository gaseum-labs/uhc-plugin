package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.Blaze
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

class SpawnBlaze : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (spawnSpace(block, 1, 2, 1)) Pair(EntityType.BLAZE, true) else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
		(entity as Blaze).removeWhenFarAway = false
	}
}

package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.PigZombie

class SpawnZombiePiglin : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (regularAllowSpawn(block, 11)) reg(EntityType.ZOMBIFIED_PIGLIN) else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
		if (entity is PigZombie) entity.setAdult()
	}
}

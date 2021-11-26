package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.*

class SpawnZombiePiglin : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (spawnSpace(block, 1, 2, 1)) reg(EntityType.ZOMBIFIED_PIGLIN) else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		if (entity is PigZombie) entity.setAdult()
	}
}

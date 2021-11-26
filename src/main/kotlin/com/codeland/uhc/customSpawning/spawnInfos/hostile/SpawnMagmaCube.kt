package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.*

class SpawnMagmaCube : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (spawnSpace(block, 3, 2, 3)) reg(EntityType.MAGMA_CUBE) else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		if (entity is MagmaCube) entity.size = (spawnCycle % 2) + 1
	}
}

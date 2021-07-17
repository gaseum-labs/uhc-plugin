package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class SpawnSpider : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		if (block.lightLevel > 7) return null
		return if (spawnSpace(block, 3, 1, 3)) reg(EntityType.SPIDER) else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		val passengers = entity.passengers
		if (passengers.isNotEmpty()) entity.removePassenger(passengers[0])
	}
}

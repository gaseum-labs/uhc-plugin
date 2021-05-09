package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Spider

class SpawnSpider : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		if (block.lightLevel > 7) return null
		return if (spawnSpace(block, 3, 1, 3)) reg(EntityType.SPIDER) else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
		val passengers = entity.passengers
		if (passengers.isNotEmpty()) entity.removePassenger(passengers[0])
	}
}

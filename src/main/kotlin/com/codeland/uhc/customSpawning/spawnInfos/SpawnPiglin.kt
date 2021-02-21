package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Piglin

class SpawnPiglin : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (regularAllowSpawn(block, 11)) EntityType.PIGLIN else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
		entity as Piglin

		entity.isBaby = false
		entity.canPickupItems = true
	}
}

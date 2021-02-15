package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.EntityType

class SpawnCreeper : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (!regularAllowSpawn(block, 7)) return null

		return if (onCycle(spawnCycle, 40)) EntityType.WITCH else EntityType.CREEPER
	}
}

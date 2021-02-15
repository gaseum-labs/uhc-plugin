package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.EntityType

class SpawnEnderman : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (block.lightLevel <= 7 && spawnSpace(block, 1, 3, 1)) EntityType.ENDERMAN else null
	}
}

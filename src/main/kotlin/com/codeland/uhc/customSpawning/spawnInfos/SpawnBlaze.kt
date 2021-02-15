package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.EntityType

class SpawnBlaze : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (spawnSpace(block, 1, 2, 1)) EntityType.BLAZE else null
	}
}

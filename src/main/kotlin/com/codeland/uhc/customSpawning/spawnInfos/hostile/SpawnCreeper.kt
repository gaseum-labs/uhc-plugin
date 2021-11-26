package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.EntityType

class SpawnCreeper : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		if (!regularAllowSpawn(block, 7)) return null

		return if (onCycle(spawnCycle, 30)) reg(EntityType.WITCH) else reg(EntityType.CREEPER)
	}
}

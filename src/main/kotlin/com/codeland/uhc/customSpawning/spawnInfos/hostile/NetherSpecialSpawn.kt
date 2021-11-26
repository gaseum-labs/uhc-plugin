package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnInfoType
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class NetherSpecialSpawn : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return getSpawnInfo(spawnCycle).allowSpawn(block, spawnCycle)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		return getSpawnInfo(spawnCycle).onSpawn(block, spawnCycle, entity)
	}

	companion object {
		fun getSpawnInfo(spawnCycle: Int): SpawnInfo {
			return when (spawnCycle % 5) {
				0 -> SpawnInfoType.GHAST
				1 -> SpawnInfoType.MAGMA_CUBE
				2 -> SpawnInfoType.ENDERMAN
				3 -> SpawnInfoType.MAGMA_CUBE
				else -> SpawnInfoType.ENDERMAN
			}.spawnInfo
		}
	}
}
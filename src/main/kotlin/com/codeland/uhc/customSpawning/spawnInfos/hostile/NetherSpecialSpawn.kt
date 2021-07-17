package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnInfoType
import org.bukkit.block.Block
import org.bukkit.entity.Entity
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
			return when (spawnCycle % 10) {
				0 -> SpawnInfoType.BLAZE
				1 -> SpawnInfoType.GHAST
				2 -> SpawnInfoType.MAGMA_CUBE
				3 -> SpawnInfoType.ZOMBIE_PIGLIN
				4 -> SpawnInfoType.ENDERMAN
				5 -> SpawnInfoType.ZOMBIE_PIGLIN
				6 -> SpawnInfoType.MAGMA_CUBE
				7 -> SpawnInfoType.ZOMBIE_PIGLIN
				8 -> SpawnInfoType.ENDERMAN
				else -> SpawnInfoType.ZOMBIE_PIGLIN
			}.spawnInfo
		}
	}
}
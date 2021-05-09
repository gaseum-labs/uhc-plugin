package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnInfoType
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

class NetherSpecialSpawn : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return getSpawnInfo(spawnCycle).allowSpawn(block, spawnCycle)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
		return getSpawnInfo(spawnCycle).onSpawn(block, spawnCycle, entity)
	}

	companion object {
		fun getSpawnInfo(spawnCycle: Int): SpawnInfo {
			return when (spawnCycle % 12) {
				0 -> SpawnInfoType.BLAZE
				1 -> SpawnInfoType.GHAST
				2 -> SpawnInfoType.MAGMA_CUBE
				3 -> SpawnInfoType.BLAZE
				4 -> SpawnInfoType.ZOMBIE_PIGLIN
				5 -> SpawnInfoType.ENDERMAN
				6 -> SpawnInfoType.BLAZE
				7 -> SpawnInfoType.ZOMBIE_PIGLIN
				8 -> SpawnInfoType.MAGMA_CUBE
				9 -> SpawnInfoType.ZOMBIE_PIGLIN
				10 -> SpawnInfoType.ZOMBIE_PIGLIN
				else -> SpawnInfoType.ENDERMAN
			}.spawnInfo
		}
	}
}
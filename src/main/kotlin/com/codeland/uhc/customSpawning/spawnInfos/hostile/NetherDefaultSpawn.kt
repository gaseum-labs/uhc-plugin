package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnInfoType
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class NetherDefaultSpawn : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return getSpawnInfo(block, spawnCycle).allowSpawn(block, spawnCycle)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		return getSpawnInfo(block, spawnCycle).onSpawn(block, spawnCycle, entity)
	}

	companion object {
		fun getSpawnInfo(block: Block, spawnCycle: Int): SpawnInfo {
			return when (block.biome) {
				Biome.CRIMSON_FOREST -> when (spawnCycle % 5) {
					0 -> SpawnInfoType.PIGLIN
					1 -> SpawnInfoType.PIGLIN
					2 -> SpawnInfoType.PIGLIN
					3 -> SpawnInfoType.ZOMBIE_PIGLIN
					else -> SpawnInfoType.HOGLIN
				}
				Biome.BASALT_DELTAS -> SpawnInfoType.MAGMA_CUBE
				Biome.SOUL_SAND_VALLEY -> SpawnInfoType.SKELETON
				Biome.WARPED_FOREST -> SpawnInfoType.ENDERMAN
				else -> when (spawnCycle % 5) {
					0 -> SpawnInfoType.ZOMBIE_PIGLIN
					1 -> SpawnInfoType.ZOMBIE_PIGLIN
					2 -> SpawnInfoType.ZOMBIE_PIGLIN
					3 -> SpawnInfoType.ZOMBIE_PIGLIN
					else -> SpawnInfoType.PIGLIN
				}
			}.spawnInfo
		}
	}
}

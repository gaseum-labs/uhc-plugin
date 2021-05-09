package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnInfoType
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType

class NetherDefaultSpawn : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return getSpawnInfo(block, spawnCycle).allowSpawn(block, spawnCycle)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
		return getSpawnInfo(block, spawnCycle).onSpawn(block, spawnCycle, entity)
	}

	companion object {
		fun getSpawnInfo(block: Block, spawnCycle: Int): SpawnInfo {
			return when (block.biome) {
				Biome.CRIMSON_FOREST -> if (onCycle(spawnCycle, 3)) SpawnInfoType.HOGLIN else SpawnInfoType.PIGLIN
				Biome.BASALT_DELTAS -> SpawnInfoType.MAGMA_CUBE
				Biome.SOUL_SAND_VALLEY -> SpawnInfoType.SKELETON
				Biome.WARPED_FOREST -> SpawnInfoType.ENDERMAN
				else -> SpawnInfoType.ZOMBIE_PIGLIN
			}.spawnInfo
		}
	}
}

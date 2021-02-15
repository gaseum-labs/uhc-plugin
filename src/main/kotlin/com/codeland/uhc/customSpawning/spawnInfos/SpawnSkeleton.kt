package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.EntityType

class SpawnSkeleton : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (block.lightLevel > 7) return null

		return if (onCycle(spawnCycle, 10)) {
			if (spawnSpace(block, 1, 3, 1)) EntityType.ENDERMAN else null

		} else if (spawnSpace(block, 1, 2, 1)) {
			if (block.biome == Biome.SNOWY_TUNDRA || block.biome == Biome.SNOWY_MOUNTAINS || block.biome == Biome.ICE_SPIKES || block.biome == Biome.FROZEN_RIVER) EntityType.STRAY
			else EntityType.SKELETON

		} else {
			null
		}
	}
}

package com.codeland.uhc.customSpawning.spawnInfos.passive

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class SpawnCow : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		val type = when {
			desert(block.biome) -> EntityType.RABBIT
			mountains(block.biome) -> EntityType.LLAMA
			plains(block.biome) -> when (spawnCycle % 6) {
				0 -> EntityType.HORSE
				1 -> EntityType.HORSE
				2 -> EntityType.HORSE
				3 -> EntityType.COW
				4 -> EntityType.COW
				else -> EntityType.DONKEY
			}
			else -> EntityType.COW
		}

		return animalAllowSpawn(type, block)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		(entity as Ageable).setAdult()
	}
}

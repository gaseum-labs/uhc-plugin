package com.codeland.uhc.customSpawning.spawnInfos.passive

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.block.Block
import org.bukkit.entity.*

class SpawnChicken : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		if (!animalAllowSpawn(block, 1, 1, 1)) return null

		return when {
			desert(block.biome) -> reg(EntityType.RABBIT)
			jungle(block.biome) -> reg(when (spawnCycle % 4) {
				0 -> EntityType.PARROT
				1 -> EntityType.PARROT
				2 -> EntityType.PARROT
				else -> EntityType.CHICKEN
			})
			else -> reg(EntityType.CHICKEN)
		}
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		(entity as Ageable).setAdult()
	}
}

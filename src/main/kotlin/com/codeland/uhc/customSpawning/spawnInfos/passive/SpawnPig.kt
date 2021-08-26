package com.codeland.uhc.customSpawning.spawnInfos.passive

import com.codeland.uhc.customSpawning.*
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class SpawnPig : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		val type = when {
			jungle(block.biome) -> when (spawnCycle % 4) {
				0 -> EntityType.PIG
				1 -> EntityType.PIG
				2 -> EntityType.PIG
				else -> EntityType.PANDA
			}
			snowy(block.biome) -> when (spawnCycle % 4) {
				0 -> EntityType.RABBIT
				1 -> EntityType.RABBIT
				2 -> EntityType.RABBIT
				else -> EntityType.POLAR_BEAR
			}
			taiga(block.biome) -> when (spawnCycle % 3) {
				0 -> EntityType.RABBIT
				1 -> EntityType.RABBIT
				else -> EntityType.PIG
			}
			mountains(block.biome) -> when (spawnCycle % 2) {
				0 -> EntityType.PIG
				else -> EntityType.LLAMA
			}
			desert(block.biome) -> EntityType.RABBIT
			block.biome === Biome.BEACH -> EntityType.TURTLE
			else -> EntityType.PIG
		}

		return animalAllowSpawn(type, block)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		(entity as Ageable).setAdult()
	}
}

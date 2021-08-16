package com.codeland.uhc.customSpawning.spawnInfos.passive

import com.codeland.uhc.customSpawning.*
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class SpawnUselessAnimal : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		val type = when {
			jungle(block.biome) -> when (spawnCycle % 6) {
				0 -> EntityType.PIG
				1 -> EntityType.PIG
				2 -> EntityType.SHEEP
				3 -> EntityType.SHEEP
				4 -> EntityType.OCELOT
				else -> EntityType.PANDA
			}
			mountains(block.biome) -> when (spawnCycle % 2) {
				0 -> EntityType.GOAT
				else -> EntityType.LLAMA
			}
			desert(block.biome) -> EntityType.RABBIT
			snowy(block.biome) -> when (spawnCycle % 5) {
				0 -> EntityType.RABBIT
				1 -> EntityType.RABBIT
				2 -> EntityType.RABBIT
				3 -> EntityType.RABBIT
				else -> EntityType.POLAR_BEAR
			}
			block.biome === Biome.BEACH -> EntityType.TURTLE
			taiga(block.biome) -> when (spawnCycle % 5) {
				0 -> EntityType.FOX
				1 -> EntityType.WOLF
				2 -> EntityType.RABBIT
				3 -> EntityType.PIG
				else -> EntityType.SHEEP
			}
			else -> when (spawnCycle % 2) {
				0 -> EntityType.PIG
				else -> EntityType.SHEEP
			}
		}

		return animalAllowSpawn(type, block)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		(entity as Ageable).setAdult()
	}
}

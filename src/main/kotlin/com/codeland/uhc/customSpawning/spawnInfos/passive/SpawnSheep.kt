package com.codeland.uhc.customSpawning.spawnInfos.passive

import com.codeland.uhc.customSpawning.*
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

class SpawnSheep : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		val type = when {
			jungle(block.biome) -> when (spawnCycle % 4) {
				0 -> EntityType.SHEEP
				1 -> EntityType.SHEEP
				2 -> EntityType.SHEEP
				else -> EntityType.OCELOT
			}
			snowy(block.biome) -> when (spawnCycle % 4) {
				0 -> EntityType.RABBIT
				1 -> EntityType.RABBIT
				2 -> EntityType.RABBIT
				else -> EntityType.POLAR_BEAR
			}
			taiga(block.biome) -> when (spawnCycle % 3) {
				0 -> EntityType.FOX
				1 -> EntityType.WOLF
				else -> EntityType.SHEEP
			}
			mountains(block.biome) -> EntityType.GOAT
			desert(block.biome) -> EntityType.RABBIT
			block.biome === Biome.BEACH -> EntityType.TURTLE
			else -> EntityType.SHEEP
		}

		return animalAllowSpawn(type, block)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		(entity as Ageable).setAdult()
	}
}

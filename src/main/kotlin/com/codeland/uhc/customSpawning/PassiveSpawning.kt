package com.codeland.uhc.customSpawning

import com.codeland.uhc.world.WorldManager
import org.bukkit.block.Biome
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.CreatureSpawnEvent

object PassiveSpawning {
	const val REPLACE_EVERY = 4
	var spawnCount = 0

	fun modifyMob(entity: Entity) {
		val world = entity.world

		if (
			entity.entitySpawnReason === CreatureSpawnEvent.SpawnReason.NATURAL &&
			world.name == WorldManager.GAME_WORLD_NAME &&
			(
				entity.type == EntityType.SHEEP ||
				entity.type == EntityType.GOAT  ||
				entity.type == EntityType.PIG
			) &&
			++spawnCount % REPLACE_EVERY == 0
		) {
			val location = entity.location

			val replaceType = when (location.block.biome) {
				Biome.PLAINS -> {
					EntityType.HORSE
				}
				Biome.SUNFLOWER_PLAINS -> {
					EntityType.DONKEY
				}
				Biome.MOUNTAINS,
				Biome.GRAVELLY_MOUNTAINS,
				Biome.MODIFIED_GRAVELLY_MOUNTAINS,
				Biome.WOODED_MOUNTAINS -> {
					EntityType.LLAMA
				}
				else -> {
					EntityType.COW
				}
			}

			world.spawnEntity(location, replaceType, CreatureSpawnEvent.SpawnReason.CUSTOM)

			entity.remove()
		}
	}
}

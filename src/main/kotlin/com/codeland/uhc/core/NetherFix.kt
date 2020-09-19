package com.codeland.uhc.core;

import com.codeland.uhc.chunkPlacer.WartPlacer
import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent

object NetherFix {
	val wartPlacer = WartPlacer(4, 993907)

	fun replaceSpawn(entity: Entity): Boolean {
		val location = entity.location
		var world = entity.world

		val chance = if (world.getBiome(location.blockX, location.blockY, location.blockZ) == Biome.BASALT_DELTAS) 0.08 else 0.05

		if (entity is LivingEntity && (entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) && Math.random() < chance) {
			world.spawnEntity(location, EntityType.BLAZE)

			return true
		}

		return false
	}
}

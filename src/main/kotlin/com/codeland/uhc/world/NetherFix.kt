package com.codeland.uhc.world;

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.impl.WartPlacer
import org.bukkit.block.Biome
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.MagmaCube
import org.bukkit.event.entity.CreatureSpawnEvent

object NetherFix {
	val wartPlacer = WartPlacer(3, 993907)

	fun replaceSpawn(entity: Entity): Boolean {
		val location = entity.location
		var world = entity.world

		val chance = if (world.getBiome(location.blockX, location.blockY, location.blockZ) == Biome.BASALT_DELTAS) 0.085 else 0.05

		return if (entity is LivingEntity && (entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) && Math.random() < chance) {
			world.spawnEntity(location, EntityType.BLAZE)
			true

		} else if (entity is MagmaCube  && entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL) {
			entity.size = Util.randRange(0, 1)
			false

		} else {
			false
		}
	}
}

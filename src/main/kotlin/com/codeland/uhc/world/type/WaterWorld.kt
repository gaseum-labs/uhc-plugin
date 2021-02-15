package com.codeland.uhc.world.type

import com.codeland.uhc.world.chunkPlacer.impl.WaterPlacer
import org.bukkit.Chunk
import org.bukkit.entity.Player

object WaterWorld {
	val waterPlacer = WaterPlacer(1, 0)

	fun removeEntities(chunk: Chunk) {
		chunk.entities.forEach { entity -> if (entity !is Player) entity.remove() }
	}
}

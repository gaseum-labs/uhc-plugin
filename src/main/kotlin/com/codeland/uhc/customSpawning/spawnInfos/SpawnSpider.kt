package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.EntityType.SKELETON
import org.bukkit.entity.Player
import org.bukkit.entity.Spider
import org.bukkit.util.Vector

class SpawnSpider : SpawnInfo<Spider>(Spider::class.java, Vector(1.0, 0.0, 1.0), false) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.lightFilter(block, SpawnUtil.MONSTER_LIGHT_LEVEL) &&
		SpawnUtil.wideSpawnFloor(block.getRelative(DOWN)) &&
		SpawnUtil.wideSpawnBox(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: Spider) {
		val passengers = entity.passengers
		if (passengers.isNotEmpty()) entity.removePassenger(passengers[0])

		/* spider jockey */
		if (SpawnUtil.onCycle(count, 100, 55)) {
			for (x in 0..1) for (z in 0..1) {
				if (!block.getRelative(x, 1, z).isPassable) return
			}

			entity.addPassenger(block.world.spawnEntity(block.location, SKELETON))
		}
	}
}
package com.codeland.uhc.customSpawning

import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.util.Vector

abstract class SpawnInfo<E : LivingEntity>(val type: Class<E>, val offset: Vector, val lineOfSight: Boolean) {
	abstract fun allowSpawn(block: Block, spawnCycle: Int): Boolean
	abstract fun onSpawn(block: Block, count: Int, player: Player?, entity: E)

	fun spawn(block: Block, count: Int = 0, player: Player? = null): E {
		val entity = block.world.spawn(block.location.add(offset), type)
		onSpawn(block, count, player, entity)

		return entity
	}
}

package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.*
import org.bukkit.util.Vector

open class SpawnEnderman : SpawnInfo<Enderman>(Enderman::class.java, Vector(0.5, 0.0, 0.5), false) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.lightFilter(block, SpawnUtil.MONSTER_LIGHT_LEVEL) &&
		SpawnUtil.spawnFloor(block.getRelative(DOWN)) &&
		SpawnUtil.tallSpawnBox(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: Enderman) {
		/* do nothing */
	}
}
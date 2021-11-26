package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnUtil
import org.bukkit.Material.NETHER_WART_BLOCK
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.HOGLIN
import org.bukkit.util.Vector

class SpawnHoglin : SpawnInfo<Hoglin>(Hoglin::class.java, Vector(1.0, 0.0, 1.0), false) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.spawnFloor(block.getRelative(DOWN)) &&
		SpawnUtil.offsetTallSpawnBox(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: Hoglin) {
		entity.setAdult()
	}
}

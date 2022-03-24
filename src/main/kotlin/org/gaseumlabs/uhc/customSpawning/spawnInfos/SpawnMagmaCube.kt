package org.gaseumlabs.uhc.customSpawning.spawnInfos

import org.gaseumlabs.uhc.customSpawning.SpawnInfo
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.*
import org.bukkit.util.Vector

class SpawnMagmaCube : SpawnInfo<MagmaCube>(MagmaCube::class.java, Vector(1.0, 0.0, 1.0), false) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.wideSpawnFloor(block.getRelative(DOWN)) &&
		SpawnUtil.wideTallSpawnBox(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: MagmaCube) {
		/* 1 or 2 */
		entity.size = (count % 2) + 1
	}
}

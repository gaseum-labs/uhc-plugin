package org.gaseumlabs.uhc.customSpawning.spawnInfos

import org.gaseumlabs.uhc.customSpawning.SpawnInfo
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.Piglin
import org.bukkit.entity.Player
import org.bukkit.util.Vector

class SpawnPiglin : SpawnInfo<Piglin>(Piglin::class.java, Vector(0.5, 0.0, 0.5), false) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.lightFilter(block, SpawnUtil.NETHER_LIGHT_LEVEL) &&
		SpawnUtil.spawnFloor(block.getRelative(DOWN)) &&
		SpawnUtil.spawnBox(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: Piglin) {
		entity.setAdult()
		entity.canPickupItems = true
	}
}
package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnUtil
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.util.Vector

class SpawnGhast : SpawnInfo<Ghast>(Ghast::class.java, Vector(1.0, 0.0, 1.0), false) {
	/* can spawn in the air */
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		for (x in -1..2) {
			for (y in 0..3) {
				for (z in -1..2) {
					if (!SpawnUtil.spawnIn(block.getRelative(x, y, z))) return false
				}
			}
		}

		return true
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: Ghast) {

	}
}

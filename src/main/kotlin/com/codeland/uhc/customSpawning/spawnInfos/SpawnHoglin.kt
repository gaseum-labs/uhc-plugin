package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType

class SpawnHoglin : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (spawnSpace(block, 3, 2, 3) && block.getRelative(BlockFace.DOWN).type != Material.NETHER_WART_BLOCK) reg(EntityType.HOGLIN) else null
	}
}

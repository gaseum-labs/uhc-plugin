package com.codeland.uhc.customSpawning.spawnInfos.hostile

import com.codeland.uhc.customSpawning.SpawnInfo
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*

class SpawnHoglin : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		return if (spawnSpace(block,
				3,
				2,
				3) && block.getRelative(BlockFace.DOWN).type != Material.NETHER_WART_BLOCK
		) reg(EntityType.HOGLIN) else null
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: LivingEntity) {
		(entity as Hoglin).setAdult()
	}
}

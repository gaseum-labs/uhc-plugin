package org.gaseumlabs.uhc.customSpawning.spawnInfos

import org.gaseumlabs.uhc.customSpawning.SpawnInfo
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.*
import org.bukkit.util.Vector

open class SpawnRegularMonster<E : LivingEntity>(type: Class<E>) : SpawnInfo<E>(type, Vector(0.5, 0.0, 0.5), false) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.lightFilter(block, SpawnUtil.MONSTER_LIGHT_LEVEL) &&
		SpawnUtil.spawnFloor(block.getRelative(DOWN)) &&
		SpawnUtil.spawnBox(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: E) {
		/* do nothing */
	}
}

class SpawnCreeper : SpawnRegularMonster<Creeper>(Creeper::class.java)

class SpawnWitch : SpawnRegularMonster<Witch>(Witch::class.java)

class SpawnSkeleton : SpawnRegularMonster<Skeleton>(Skeleton::class.java)

class SpawnStray : SpawnRegularMonster<Stray>(Stray::class.java)

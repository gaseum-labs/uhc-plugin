package org.gaseumlabs.uhc.customSpawning.regeneration

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.customSpawning.*
import org.gaseumlabs.uhc.customSpawning.spawnInfos.*
import org.gaseumlabs.uhc.util.Util.takeFrom
import org.gaseumlabs.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.*
import org.bukkit.block.*
import kotlin.random.Random

class LeatherRegen(game: Game) : Regen(game, 5, 600) {
	private fun spawnInfo(block: Block): SpawnInfo<*> {
		return when {
			SpawnUtil.mountains(block.biome) -> SpawnLlama()
			SpawnUtil.plains(block.biome) -> if (Random.nextBoolean()) SpawnHorse() else SpawnDonkey()
			else -> SpawnCow()
		}
	}

	override fun place(chunk: Chunk): Boolean {
		val (block, spawnInfo) = ChunkPlacer.randomPosition(chunk, 59, 80) { block ->
			val spawnInfo = spawnInfo(block)
			Pair(block, spawnInfo).takeFrom(spawnInfo.allowSpawn(block, 0))
		} ?: return false

		spawnInfo.spawn(block).removeWhenFarAway = false

		return true
	}
}

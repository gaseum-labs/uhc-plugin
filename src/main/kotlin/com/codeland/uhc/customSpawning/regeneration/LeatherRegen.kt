package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.customSpawning.*
import com.codeland.uhc.customSpawning.spawnInfos.*
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import org.bukkit.*
import org.bukkit.block.*
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.*
import kotlin.random.Random

class LeatherRegen(game: Game) : Regen(game, 5, 600) {
	private fun spawnInfo(block: Block): SpawnInfo<*> {
		return when (block.biome) {
			Biome.MOUNTAINS,
			Biome.WOODED_MOUNTAINS,
			Biome.GRAVELLY_MOUNTAINS,
			Biome.MODIFIED_GRAVELLY_MOUNTAINS,
			-> SpawnLlama()
			Biome.PLAINS,
			Biome.SUNFLOWER_PLAINS,
			-> if (Random(block.blockKey).nextBoolean()) SpawnHorse() else SpawnDonkey()
			else -> SpawnCow()
		}
	}

	override fun place(chunk: Chunk): Boolean {
		val (block, spawnInfo) = AbstractChunkPlacer.randomPosition(chunk, 59, 80) { block ->
			val spawnInfo = spawnInfo(block)

			if (spawnInfo.allowSpawn(block, 0)) {
				Pair(block, spawnInfo)
			} else {
				null
			}
		} ?: return false

		spawnInfo.spawn(block)

		return true
	}
}

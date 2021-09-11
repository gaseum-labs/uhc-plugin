package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.core.phase.phases.Shrink
import com.codeland.uhc.customSpawning.SpawnInfo.Companion.isWater
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import kotlin.math.ceil
import kotlin.random.Random

class LeatherRegen(game: Game): Regen(game, 510) {
	fun spawnOn(block: Block): Boolean {
		return block.type === Material.GRASS_BLOCK ||
			block.type === Material.DIRT ||
			block.type === Material.COARSE_DIRT ||
			block.type === Material.PODZOL
	}

	fun spawnIn(block: Block): Boolean {
		return block.type.isAir ||
		block.type === Material.GRASS ||
		block.type === Material.TALL_GRASS ||
		block.type === Material.FERN ||
		block.type === Material.LARGE_FERN
	}

	override fun place(chunk: Chunk): Boolean {
		val block = AbstractChunkPlacer.randomPosition(chunk, 58, 70) { block, _, _, _ ->
			spawnIn(block) && spawnIn(block.getRelative(BlockFace.UP)) && spawnOn(block.getRelative(BlockFace.DOWN))
		}

		if (block != null) {
			val entity = chunk.world.spawnEntity(block.location.add(0.5, 0.0, 0.5),
				when (block.biome) {
					Biome.MOUNTAINS,
					Biome.WOODED_MOUNTAINS,
					Biome.GRAVELLY_MOUNTAINS,
					Biome.MODIFIED_GRAVELLY_MOUNTAINS -> EntityType.LLAMA
					Biome.PLAINS,
					Biome.SUNFLOWER_PLAINS -> if (Random.nextBoolean()) EntityType.HORSE else EntityType.DONKEY
					else -> EntityType.COW
				}
			)

			(entity as Ageable).setAdult()
		}

		return block != null
	}
}
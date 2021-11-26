package com.codeland.uhc.customSpawning.regeneration

import com.codeland.uhc.core.Game
import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.*
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import kotlin.random.Random

class LeatherRegen(game: Game) : Regen(game, 5, 570) {
	private fun spawnOn(block: Block): Boolean {
		return block.type === Material.GRASS_BLOCK ||
		block.type === Material.DIRT ||
		block.type === Material.COARSE_DIRT ||
		block.type === Material.PODZOL ||
		block.type === Material.GRAVEL
	}

	private fun entityType(block: Block): EntityType {
		return when (block.biome) {
			Biome.MOUNTAINS,
			Biome.WOODED_MOUNTAINS,
			Biome.GRAVELLY_MOUNTAINS,
			Biome.MODIFIED_GRAVELLY_MOUNTAINS,
			-> EntityType.LLAMA
			Biome.PLAINS,
			Biome.SUNFLOWER_PLAINS,
			-> if (Random(block.blockKey).nextBoolean()) EntityType.HORSE else EntityType.DONKEY
			else -> EntityType.COW
		}
	}

	private fun spawnBox(block: Block, type: EntityType): Boolean {
		return when (type) {
			EntityType.HORSE,
			EntityType.DONKEY,
			-> SpawnInfo.spawnBox(block, 3, 2, 3)
			else -> SpawnInfo.spawnBox(block, 1, 2, 1)
		}
	}

	override fun place(chunk: Chunk): Boolean {
		val floor = AbstractChunkPlacer.randomPosition(chunk, 58, 70) { floor, _, _, _ ->
			spawnOn(floor) && spawnBox(floor.getRelative(BlockFace.UP), entityType(floor))
		}

		if (floor != null) {
			(floor.world.spawnEntity(floor.location.add(0.5, 1.0, 0.5), entityType(floor)) as Ageable).setAdult()
		}

		return floor != null
	}
}

package com.codeland.uhc.customSpawning

import org.bukkit.Material
import org.bukkit.block.*
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.data.Waterlogged

object SpawnUtil {
	const val MONSTER_LIGHT_LEVEL = 7
	const val NETHER_LIGHT_LEVEL = 11
	const val NETHER_CAVE_Y = 31
	const val SURFACE_Y = 58

	fun lightFilter(block: Block, level: Int): Boolean {
		return block.lightLevel <= level
	}

	fun onCycle(spawnCycle: Int, n: Int, offset: Int = 0): Boolean {
		return spawnCycle >= n && spawnCycle % n == offset
	}

	/* block spawning attributes */

	fun isWater(block: Block): Boolean {
		return block.type === Material.WATER ||
		block.type === Material.KELP ||
		block.type === Material.SEAGRASS ||
		block.type === Material.TALL_SEAGRASS ||
		((block.blockData as? Waterlogged)?.isWaterlogged == true)
	}

	fun spawnObstacle(block: Block): Boolean {
		return !block.isPassable || block.type == Material.LAVA
	}

	fun spawnIn(block: Block): Boolean {
		return !spawnObstacle(block) && !isWater(block)
	}

	fun spawnFloor(block: Block): Boolean {
		return !block.isPassable &&
		block.type !== Material.LILY_PAD &&
		block.type !== Material.NETHER_WART_BLOCK &&
		block.type !== Material.WARPED_WART_BLOCK &&
		block.type !== Material.OAK_LEAVES &&
		block.type !== Material.SPRUCE_LEAVES &&
		block.type !== Material.BIRCH_LEAVES &&
		block.type !== Material.JUNGLE_LEAVES &&
		block.type !== Material.ACACIA_LEAVES &&
		block.type !== Material.DARK_OAK_LEAVES &&
		block.type !== Material.BAMBOO
	}

	fun animalSpawnFloor(block: Block): Boolean {
		return block.type === Material.GRASS_BLOCK ||
		block.type === Material.DIRT ||
		block.type === Material.COARSE_DIRT ||
		block.type === Material.PODZOL ||
		block.type === Material.SAND
	}

	fun wideSpawnFloor(block: Block): Boolean {
		return spawnFloor(block) ||
		spawnFloor(block.getRelative(1, 0, 0)) ||
		spawnFloor(block.getRelative(0, 0, 1)) ||
		spawnFloor(block.getRelative(1, 0, 1))
	}

	fun wideAnimalSpawnFloor(block: Block): Boolean {
		return animalSpawnFloor(block) ||
		animalSpawnFloor(block.getRelative(1, 0, 0)) ||
		animalSpawnFloor(block.getRelative(0, 0, 1)) ||
		animalSpawnFloor(block.getRelative(1, 0, 1))
	}

	/* spawn boxes */

	fun spawnBox(block: Block): Boolean {
		return spawnIn(block) && spawnIn(block.getRelative(UP))
	}

	fun tallSpawnBox(block: Block): Boolean {
		return spawnIn(block) && spawnIn(block.getRelative(0, 1, 0)) && spawnIn(block.getRelative(0, 2, 0))
	}

	fun wideSpawnBox(block: Block): Boolean {
		return spawnIn(block.getRelative(0, 0, 0)) &&
		spawnIn(block.getRelative(1, 0, 0)) &&
		spawnIn(block.getRelative(0, 0, 1)) &&
		spawnIn(block.getRelative(1, 0, 1))
	}

	fun wideTallSpawnBox(block: Block): Boolean {
		return spawnIn(block.getRelative(0, 0, 0)) &&
		spawnIn(block.getRelative(1, 0, 0)) &&
		spawnIn(block.getRelative(0, 0, 1)) &&
		spawnIn(block.getRelative(1, 0, 1)) &&
		spawnIn(block.getRelative(0, 1, 0)) &&
		spawnIn(block.getRelative(1, 1, 0)) &&
		spawnIn(block.getRelative(0, 1, 1)) &&
		spawnIn(block.getRelative(1, 1, 1))
	}

	/* animal spawning biome categories */

	fun desert(biome: Biome) = biome === Biome.DESERT ||
	biome === Biome.BADLANDS ||
	biome === Biome.ERODED_BADLANDS

	fun mountains(biome: Biome) = biome === Biome.WINDSWEPT_HILLS ||
	biome === Biome.WINDSWEPT_GRAVELLY_HILLS ||
	biome === Biome.WINDSWEPT_SAVANNA

	fun snowy(biome: Biome) = biome === Biome.SNOWY_BEACH ||
	biome === Biome.SNOWY_PLAINS ||
	biome === Biome.SNOWY_SLOPES

	fun plains(biome: Biome) = biome === Biome.PLAINS ||
	biome === Biome.SUNFLOWER_PLAINS ||
	biome === Biome.MEADOW

	fun taiga(biome: Biome) = biome === Biome.TAIGA ||
	biome === Biome.OLD_GROWTH_PINE_TAIGA ||
	biome === Biome.OLD_GROWTH_SPRUCE_TAIGA

	fun jungle(biome: Biome) = biome === Biome.JUNGLE ||
	biome === Biome.BAMBOO_JUNGLE ||
	biome === Biome.SPARSE_JUNGLE
}

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
		block.type !== Material.DARK_OAK_LEAVES
	}

	fun animalSpawnFloor(block: Block): Boolean {
		return block.type === Material.GRASS_BLOCK ||
		block.type === Material.DIRT ||
		block.type === Material.COARSE_DIRT ||
		block.type === Material.PODZOL ||
		block.type === Material.SAND
	}

	/* spawn boxes */

	fun spawnBox(block: Block): Boolean {
		return spawnIn(block) && spawnIn(block.getRelative(UP))
	}

	fun tallSpawnBox(block: Block): Boolean {
		return spawnIn(block) && spawnIn(block.getRelative(0, 1, 0)) && spawnIn(block.getRelative(0, 2, 0))
	}

	//fun largeSpawnBox(block: Block, height: Int): Boolean {
	//	return (0 until height).all { y ->
	//		spawnIn(block.getRelative(-1, y, -1)) &&
	//		spawnIn(block.getRelative(0, y, -1)) &&
	//		spawnIn(block.getRelative(1, y, -1)) &&
	//		spawnIn(block.getRelative(-1, y, 0)) &&
	//		spawnIn(block.getRelative(0, y, 0)) &&
	//		spawnIn(block.getRelative(1, y, 0)) &&
	//		spawnIn(block.getRelative(-1, y, 1)) &&
	//		spawnIn(block.getRelative(0, y, 1)) &&
	//		spawnIn(block.getRelative(1, y, 1))
	//	}
	//}

	fun offsetShortSpawnBox(block: Block): Boolean {
		return spawnIn(block.getRelative(0, 0, 0)) &&
		spawnIn(block.getRelative(1, 0, 0)) &&
		spawnIn(block.getRelative(0, 0, 1)) &&
		spawnIn(block.getRelative(1, 0, 1))
	}

	fun offsetTallSpawnBox(block: Block): Boolean {
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
	biome === Biome.DESERT_HILLS ||
	biome === Biome.DESERT_LAKES

	fun mountains(biome: Biome) = biome === Biome.MOUNTAINS ||
	biome === Biome.GRAVELLY_MOUNTAINS ||
	biome === Biome.WOODED_MOUNTAINS ||
	biome === Biome.MODIFIED_GRAVELLY_MOUNTAINS

	fun snowy(biome: Biome) = biome === Biome.SNOWY_TUNDRA ||
	biome === Biome.SNOWY_MOUNTAINS ||
	biome === Biome.ICE_SPIKES

	fun plains(biome: Biome) = biome === Biome.PLAINS ||
	biome === Biome.SUNFLOWER_PLAINS

	fun taiga(biome: Biome) = biome === Biome.GIANT_SPRUCE_TAIGA ||
	biome === Biome.GIANT_TREE_TAIGA ||
	biome === Biome.SNOWY_TAIGA ||
	biome === Biome.SNOWY_TAIGA_MOUNTAINS ||
	biome === Biome.TAIGA ||
	biome === Biome.TAIGA_HILLS ||
	biome === Biome.TAIGA_MOUNTAINS

	fun jungle(biome: Biome) = biome === Biome.JUNGLE ||
	biome === Biome.JUNGLE_EDGE ||
	biome === Biome.JUNGLE_HILLS ||
	biome === Biome.MODIFIED_JUNGLE_EDGE ||
	biome === Biome.MODIFIED_JUNGLE ||
	biome === Biome.BAMBOO_JUNGLE ||
	biome === Biome.BAMBOO_JUNGLE_HILLS
}
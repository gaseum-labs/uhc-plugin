package com.codeland.uhc.quirk

import org.bukkit.Material

object Creative {
	val blocks = arrayOf<Material>(
		Material.COBBLESTONE,
		Material.COBBLESTONE_SLAB,
		Material.COBBLESTONE_STAIRS,
		Material.COBBLESTONE_WALL,

		Material.STONE,
		Material.STONE_SLAB,
		Material.STONE_STAIRS,

		Material.STONE_BRICKS,
		Material.STONE_BRICK_SLAB,
		Material.STONE_BRICK_STAIRS,
		Material.STONE_BRICK_WALL,

		Material.DIRT,
		Material.GRASS_BLOCK,
		Material.STONE_SLAB,

		Material.NETHERRACK,
		Material.MAGMA_BLOCK,
		Material.NETHER_BRICKS,
		Material.NETHER_BRICK_FENCE,
		Material.NETHER_BRICK_STAIRS,
		Material.NETHER_BRICK_SLAB,
		Material.NETHER_BRICK_WALL,

		Material.TORCH,
		Material.CAMPFIRE,
		Material.SOUL_TORCH,
		Material.SOUL_CAMPFIRE,

		Material.GRANITE,
		Material.GRANITE_SLAB,
		Material.GRANITE_STAIRS,
		Material.GRANITE_WALL,
		Material.POLISHED_GRANITE,
		Material.POLISHED_GRANITE_SLAB,
		Material.POLISHED_GRANITE_STAIRS,

		Material.ANDESITE,
		Material.ANDESITE_SLAB,
		Material.ANDESITE_STAIRS,
		Material.ANDESITE_WALL,
		Material.POLISHED_ANDESITE,
		Material.POLISHED_ANDESITE_SLAB,
		Material.POLISHED_ANDESITE_STAIRS,

		Material.DIORITE,
		Material.DIORITE_SLAB,
		Material.DIORITE_STAIRS,
		Material.DIORITE_WALL,
		Material.POLISHED_DIORITE,
		Material.POLISHED_DIORITE_SLAB,
		Material.POLISHED_DIORITE_STAIRS,

		Material.OAK_FENCE,
		Material.SPRUCE_FENCE,
		Material.DARK_OAK_FENCE,
		Material.BIRCH_FENCE,
		Material.ACACIA_FENCE,
		Material.JUNGLE_FENCE,
		Material.WARPED_FENCE,
		Material.CRIMSON_FENCE,
		Material.OAK_SLAB,
		Material.SPRUCE_SLAB,
		Material.DARK_OAK_SLAB,
		Material.BIRCH_SLAB,
		Material.ACACIA_SLAB,
		Material.JUNGLE_SLAB,
		Material.WARPED_SLAB,
		Material.CRIMSON_SLAB,
		Material.OAK_STAIRS,
		Material.SPRUCE_STAIRS,
		Material.DARK_OAK_STAIRS,
		Material.BIRCH_STAIRS,
		Material.ACACIA_STAIRS,
		Material.JUNGLE_STAIRS,
		Material.WARPED_STAIRS,
		Material.CRIMSON_STAIRS,
		Material.OAK_PRESSURE_PLATE,
		Material.SPRUCE_PRESSURE_PLATE,
		Material.DARK_OAK_PRESSURE_PLATE,
		Material.BIRCH_PRESSURE_PLATE,
		Material.ACACIA_PRESSURE_PLATE,
		Material.JUNGLE_PRESSURE_PLATE,
		Material.WARPED_PRESSURE_PLATE,
		Material.CRIMSON_PRESSURE_PLATE,

		Material.REDSTONE,
		Material.PISTON,
		Material.STICKY_PISTON,
		Material.REPEATER,
		Material.COMPARATOR,
		Material.REDSTONE_BLOCK,
		Material.REDSTONE_TORCH,
		Material.DISPENSER,
		Material.DROPPER,
		Material.TARGET,
		Material.STONE_PRESSURE_PLATE,
		Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
		Material.LIGHT_WEIGHTED_PRESSURE_PLATE
	)

	init {
		blocks.sort()
	}
}
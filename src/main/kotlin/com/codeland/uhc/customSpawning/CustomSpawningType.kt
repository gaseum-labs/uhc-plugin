package com.codeland.uhc.customSpawning

import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Player

enum class CustomSpawningType(
	val minRadius: Int,
	val maxRadius: Int,
	val verticalRadius: Int,
	val tryTime: Int,
	val overworldEntries: Array<SpawnEntry>,
	val netherEntries: Array<SpawnEntry>,
	val getCap: (Player) -> Double,
) {
	HOSTILE(32, 86, 15, 20, arrayOf(
		SpawnEntry.ZOMBIE,
		SpawnEntry.SKELETON,
		SpawnEntry.CREEPER,
		SpawnEntry.SPIDER
	), arrayOf(
		SpawnEntry.NETHER_DEFAULT,
		SpawnEntry.NETHER_DEFAULT,
		SpawnEntry.NETHER_DEFAULT,
		SpawnEntry.NETHER_DEFAULT,
		SpawnEntry.NETHER_DEFAULT,
		SpawnEntry.NETHER_DEFAULT,
		SpawnEntry.NETHER_SPECIAL
	), { player ->
		when (player.world.environment) {
			World.Environment.NORMAL -> {
				if (player.location.world.isDayTime && player.location.y >= 58) 10.0 else 30.0
			}
			World.Environment.NETHER -> {
				30.0 * (if (player.location.block.y <= SpawnUtil.NETHER_CAVE_Y) 1.5 else 1.0) /
				(if (player.location.block.biome === Biome.SOUL_SAND_VALLEY) 2.0 else 1.0)
			}
			else -> 30.0
		}
	}),

	PASSIVE(
		32, 86, 48, 20,
		arrayOf(
			SpawnEntry.SHEEP,
			SpawnEntry.PIG,
			SpawnEntry.CHICKEN
		),
		emptyArray(),
		{ 20.0 },
	),

	BLAZE(32, 64, 10, 3600, emptyArray(), arrayOf(
		SpawnEntry.BLAZE
	), { player ->
		if (player.location.block.y <= SpawnUtil.NETHER_CAVE_Y) 0.0 else 1.0
	});

	val spawnTag = "_UCS_${this.name}"
}

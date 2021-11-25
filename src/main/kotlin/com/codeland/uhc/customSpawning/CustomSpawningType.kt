package com.codeland.uhc.customSpawning

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

enum class CustomSpawningType(
	val minRadius: Int,
	val maxRadius: Int,
	val verticalRadius: Int,
	val tryTime: Int,
	val gameSpawnInfoList: Array<SpawnInfo>,
	val netherSpawnInfoList: Array<SpawnInfo>,
	val getCap: (Player) -> Double,
	val onSpawn: (Player, Entity) -> Unit,
) {
	HOSTILE(32, 86, 10, 20, arrayOf(
		SpawnInfoType.ZOMBIE.spawnInfo,
		SpawnInfoType.SKELETON.spawnInfo,
		SpawnInfoType.CREEPER.spawnInfo,
		SpawnInfoType.SPIDER.spawnInfo
	), arrayOf(
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_SPECIAL.spawnInfo,
	), { player ->
		when (player.world.environment) {
			World.Environment.NORMAL -> {
				if (player.location.world.isDayTime && player.location.y >= 58) 10.0 else 30.0
			}
			World.Environment.NETHER -> {
				20.0 * (if (player.location.block.y <= SpawnInfo.NETHER_CAVE_Y) 2.0 else 1.0) /
					(if (player.location.block.biome === Biome.SOUL_SAND_VALLEY) 2.0 else 1.0)
			}
			else -> 30.0
		}
	}, { _, _ -> }),

	PASSIVE(32, 86, 48, 20, arrayOf(
		SpawnInfoType.SHEEP.spawnInfo,
		SpawnInfoType.PIG.spawnInfo,
		SpawnInfoType.CHICKEN.spawnInfo
	),
		emptyArray(),
		{ 20.0 },
		{ _, _ -> }
	),

	BLAZE(32, 64, 10, 3600, emptyArray(), arrayOf(
		SpawnInfoType.BLAZE.spawnInfo
	), { player ->
		if (player.location.block.y <= SpawnInfo.NETHER_CAVE_Y) 0.0 else 1.0
	}, { player, entity ->
		val component = Component.text("Blaze Spawned!", TextColor.color(0xff6417))
		player.sendActionBar(component)
		player.sendMessage(component)

		player.playSound(player.location.add(
			entity.location
				.subtract(player.location)
				.toVector()
				.normalize()
				.multiply(3)
		), org.bukkit.Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 1.0f)
	});

	val spawnTag = "_UCS_${this.name}"
}

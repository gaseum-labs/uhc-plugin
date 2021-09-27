package com.codeland.uhc.customSpawning

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
	HOSTILE(32, 86, 10, 30, arrayOf(
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
				if (player.location.world.isDayTime && player.location.y >= 58) 10.0 else 20.0
			}
			World.Environment.NETHER -> {
				20.0 * (if (player.location.block.y <= SpawnInfo.NETHER_CAVE_Y) 2.0 else 1.0) /
					(if (player.location.block.biome === Biome.SOUL_SAND_VALLEY) 2.0 else 1.0)
			}
			else -> 20.0
		}
	}, { _, _ -> }),

	PASSIVE(32, 86, 48, 20, arrayOf(
		SpawnInfoType.SHEEP.spawnInfo,
		SpawnInfoType.PIG.spawnInfo,
		SpawnInfoType.CHICKEN.spawnInfo
	),
		emptyArray(),
		{ 40.0 },
		{ _, _ -> }
	),

	BLAZE(32, 64, 9, 3600, emptyArray(), arrayOf(
		SpawnInfoType.BLAZE.spawnInfo
	), { player ->
		if (player.location.block.y <= SpawnInfo.NETHER_CAVE_Y) 0.0 else 1.0
	}, { player, entity ->
		val soundLocation = player.location.add(
			entity.location
				.subtract(player.location)
				.toVector()
				.normalize()
				.multiply(3)
		)

		val component = net.kyori.adventure.text.Component.text("Blaze Spawned!", net.kyori.adventure.text.format.TextColor.color(0xff6417))
		player.sendActionBar(component)
		player.sendMessage(component)

		player.playSound(
			net.kyori.adventure.sound.Sound.sound(net.kyori.adventure.sound.Sound.Type { net.kyori.adventure.key.Key.key("entity.blaze.ambient") }, net.kyori.adventure.sound.Sound.Source.MASTER, 1.0f, 1.0f),
			soundLocation.x, soundLocation.y, soundLocation.z
		)
	});

	val spawnTag = "_UCS_${this.name}"
}

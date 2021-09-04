package com.codeland.uhc.customSpawning

import kotlin.math.roundToInt

enum class CustomSpawningType(
	val minRadius: Int,
	val maxRadius: Int,
	val verticalRadius: Int,
	val mobcap: Int,
	val tryTime: Int,
	val gameSpawnInfoList: Array<SpawnInfo>,
	val netherSpawnInfoList: Array<SpawnInfo>,
) {
	HOSTILE(32, 86, 10, 20, 40, arrayOf(
		SpawnInfoType.ZOMBIE.spawnInfo,
		SpawnInfoType.SKELETON.spawnInfo,
		SpawnInfoType.CREEPER.spawnInfo,
		SpawnInfoType.SPIDER.spawnInfo
	), arrayOf(
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_SPECIAL.spawnInfo,
		SpawnInfoType.PIGLIN.spawnInfo
	)),

	PASSIVE(32, 86, 48, 25, 20, arrayOf(
		SpawnInfoType.SHEEP.spawnInfo,
		SpawnInfoType.PIG.spawnInfo,
		SpawnInfoType.COW.spawnInfo,
		SpawnInfoType.CHICKEN.spawnInfo
	), emptyArray());

	val spawnTag = "_UCS_${this.name}"

	data class SpawningPlayerData(
		var index: Int,
		var cycle: Int,
		var cap: Double
	) {
		constructor() : this(0, 0, 0.0)

		fun getMobCap(): Int {
			return cap.roundToInt().coerceAtLeast(1)
		}
	}
}

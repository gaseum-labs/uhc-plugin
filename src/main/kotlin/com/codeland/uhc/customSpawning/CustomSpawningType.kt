package com.codeland.uhc.customSpawning

enum class CustomSpawningType(
	val minRadius: Int,
	val maxRadius: Int,
	val verticalRadius: Int,
	val mobcap: Int,
	val tryTime: Int,
	val gameSpawnInfoList: Array<SpawnInfo>,
	val netherSpawnInfoList: Array<SpawnInfo>,
) {
	HOSTILE(32, 86, 10, 25, 20, arrayOf(
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

	PASSIVE(78, 120, 90, 15, 20, arrayOf(
		SpawnInfoType.USELESS_ANIMAL.spawnInfo,
		SpawnInfoType.COW.spawnInfo,
		SpawnInfoType.USELESS_ANIMAL.spawnInfo,
		SpawnInfoType.COW.spawnInfo,
		SpawnInfoType.USELESS_ANIMAL.spawnInfo,
		SpawnInfoType.CHICKEN.spawnInfo
	), emptyArray());

	val spawnTag = "_UCS_${this.name}"

	data class SpawningPlayerData(
		var index: Int,
		var cycle: Int,
		var mobcap: Double
	) {
		constructor() : this(0, 0, 0.0)
	}
}

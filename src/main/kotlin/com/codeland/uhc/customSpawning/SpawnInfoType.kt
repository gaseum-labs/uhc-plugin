package com.codeland.uhc.customSpawning

import com.codeland.uhc.customSpawning.spawnInfos.*

enum class SpawnInfoType(val spawnInfo: SpawnInfo) {
	ZOMBIE(SpawnZombie()),

	SKELETON(SpawnSkeleton()),

	CREEPER(SpawnCreeper()),

	SPIDER(SpawnSpider()),

	ENDERMAN(SpawnEnderman()),

	ZOMBIE_PIGLIN(SpawnZombiePiglin()),

	PIGLIN(SpawnPiglin()),

	BLAZE(SpawnBlaze()),

	MAGMA_CUBE(SpawnMagmaCube()),

	HOGLIN(SpawnHoglin()),

	GHAST(SpawnGhast()),

	NETHER_DEFAULT(NetherDefaultSpawn()),

	NETHER_SPECIAL(NetherSpecialSpawn());
}

package com.codeland.uhc.customSpawning

import com.codeland.uhc.customSpawning.spawnInfos.hostile.*
import com.codeland.uhc.customSpawning.spawnInfos.passive.*

enum class SpawnInfoType(val spawnInfo: SpawnInfo) {
	ZOMBIE(SpawnZombie()),
	SKELETON(SpawnSkeleton()),
	CREEPER(SpawnCreeper()),
	SPIDER(SpawnSpider()),
	ENDERMAN(SpawnEnderman()),

	ZOMBIE_PIGLIN(SpawnZombiePiglin()),
	PIGLIN(SpawnPiglin()),
	MAGMA_CUBE(SpawnMagmaCube()),
	HOGLIN(SpawnHoglin()),
	GHAST(SpawnGhast()),

	BLAZE(SpawnBlaze()),

	NETHER_DEFAULT(NetherDefaultSpawn()),
	NETHER_SPECIAL(NetherSpecialSpawn()),

	CHICKEN(SpawnChicken()),
	SHEEP(SpawnSheep()),
	PIG(SpawnPig());
}

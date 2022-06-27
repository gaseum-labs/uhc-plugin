package org.gaseumlabs.uhc.customSpawning

import org.gaseumlabs.uhc.customSpawning.spawnInfos.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Biome
import org.gaseumlabs.uhc.world.gen.BiomeNo
import net.minecraft.world.level.biome.Biomes

enum class SpawnEntry(val getSpawnInfo: (block: Block, spawnCycle: Int) -> SpawnInfo<*>) {
	ZOMBIE({ block, spawnCycle ->
		if (SpawnUtil.isWater(block) && SpawnUtil.isWater(block.getRelative(BlockFace.UP))) {
			SpawnDrowned()
		} else if (SpawnUtil.onCycle(spawnCycle, 20)) {
			SpawnZombieVillager()
		} else if (BiomeNo.isDesertBiome(BiomeNo.biomeAt(block)) && block.y >= SpawnUtil.SURFACE_Y) {
			SpawnHusk()
		} else {
			SpawnZombie()
		}
	}),
	SKELETON({ block, spawnCycle ->
		if (SpawnUtil.onCycle(spawnCycle, 16) && block.y < 0) {
			SpawnSlime()
		} else if (BiomeNo.isSnowyBiome(BiomeNo.biomeAt(block)) && block.y >= SpawnUtil.SURFACE_Y) {
			SpawnStray()
		} else {
			SpawnSkeleton()
		}
	}),
	CREEPER({ _, spawnCycle ->
		if (SpawnUtil.onCycle(spawnCycle, 32)) {
			SpawnWitch()
		} else {
			SpawnCreeper()
		}
	}),
	SPIDER({ _, spawnCycle ->
		if (SpawnUtil.onCycle(spawnCycle, 10)) {
			SpawnEnderman()
		} else {
			SpawnSpider()
		}
	}),

	NETHER_DEFAULT({ block, spawnCycle ->
		when (block.biome) {
			Biome.CRIMSON_FOREST -> when (spawnCycle % 6) {
				0 -> SpawnZombiePiglin()
				1 -> SpawnHoglin()
				2 -> SpawnPiglin()
				3 -> SpawnHoglin()
				4 -> SpawnPiglin()
				else -> SpawnHoglin()
			}
			Biome.BASALT_DELTAS -> SpawnMagmaCube()
			Biome.SOUL_SAND_VALLEY -> SpawnSkeleton()
			Biome.WARPED_FOREST -> SpawnEnderman()
			else -> when (spawnCycle % 5) {
				0 -> SpawnZombiePiglin()
				1 -> SpawnZombiePiglin()
				2 -> SpawnZombiePiglin()
				3 -> SpawnZombiePiglin()
				else -> SpawnPiglin()
			}
		}
	}),
	NETHER_SPECIAL({ _, spawnCycle ->
		when (spawnCycle % 5) {
			0 -> SpawnGhast()
			1 -> SpawnMagmaCube()
			2 -> SpawnEnderman()
			3 -> SpawnMagmaCube()
			else -> SpawnEnderman()
		}
	}),

	CHICKEN({ block, spawnCycle ->
		val biome = BiomeNo.biomeAt(block)
		when {
			BiomeNo.isJungleBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnChicken()
				1 -> SpawnChicken()
				else -> SpawnParrot()
			}
			BiomeNo.isTaigaBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnChicken()
				1 -> SpawnChicken()
				else -> SpawnRabbit()
			}
			BiomeNo.isSnowyBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnRabbit()
				1 -> SpawnRabbit()
				else -> SpawnPolarBear()
			}
			BiomeNo.isDesertBiome(biome) -> SpawnRabbit()
			else -> SpawnChicken()
		}
	}),
	PIG({ block, spawnCycle ->
		val biome = BiomeNo.biomeAt(block)
		when {
			BiomeNo.isJungleBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnPig()
				1 -> SpawnPig()
				else -> SpawnPanda()
			}
			BiomeNo.isTaigaBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnRabbit()
				1 -> SpawnRabbit()
				else -> SpawnPig()
			}
			BiomeNo.isSnowyBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnRabbit()
				1 -> SpawnRabbit()
				else -> SpawnPolarBear()
			}
			BiomeNo.isDesertBiome(biome) -> SpawnRabbit()
			biome === Biomes.UHC_BEACH -> SpawnTurtle()
			else -> SpawnPig()
		}
	}),
	SHEEP({ block, spawnCycle ->
		val biome = BiomeNo.biomeAt(block)
		when {
			BiomeNo.isJungleBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnSheep()
				1 -> SpawnSheep()
				else -> SpawnOcelot()
			}
			BiomeNo.isTaigaBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnFox()
				1 -> SpawnWolf()
				else -> SpawnSheep()
			}
			BiomeNo.isSnowyBiome(biome) -> when (spawnCycle % 3) {
				0 -> SpawnRabbit()
				1 -> SpawnRabbit()
				else -> SpawnPolarBear()
			}
			BiomeNo.isMountainsBiome(biome) -> SpawnGoat()
			BiomeNo.isDesertBiome(biome) -> SpawnRabbit()
			biome === Biomes.UHC_BEACH -> SpawnTurtle()
			else -> SpawnSheep()
		}
	});
}

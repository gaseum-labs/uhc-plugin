package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType.COW
import org.bukkit.entity.EntityType.HORSE
import org.bukkit.entity.EntityType.LLAMA
import org.bukkit.entity.EntityType.DONKEY
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnHorse
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.locateAround
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreader
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionEntity
import kotlin.random.Random

class ResourceLeather : ResourceDescriptionEntity(
	5,
	45,
	5,
	10 * 20,
) {
	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (centerY < SpawnUtil.SURFACE_Y) return null
		if (world !== WorldManager.gameWorld) return null

		val potentialSpots = locateAround(world, centerX, centerZ, 11, 32.0, 80.0, 8) { x, z -> x to z }

		for ((x, z) in potentialSpots) {
			val surface = surfaceSpreader(world, x, z, 5, ::cowHorseGood)
			if (surface != null) {
				return listOf(surface.getRelative(UP))
			}
		}

		return null
	}

	override fun setEntity(block: Block): Entity {
		val entityType = when {
			SpawnUtil.mountains(block.biome) && Random.nextBoolean() -> LLAMA
			SpawnUtil.plains(block.biome) && Random.nextBoolean() -> if (Random.nextInt(3) == 0) DONKEY else HORSE
			else -> COW
		}

		val animal = block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), entityType) as Animals
		animal.setAdult()
		return animal
	}

	override fun isEntity(entity: Entity): Boolean {
		return when (entity.type) {
			COW,
			HORSE,
			LLAMA,
			-> true
			else -> false
		}
	}

	private val spawnHorse = SpawnHorse()

	/* placement */
	private fun cowHorseGood(surfaceBlock: Block): Boolean {
		return spawnHorse.allowSpawn(surfaceBlock.getRelative(UP), 0)
	}
}

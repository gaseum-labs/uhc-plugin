package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.EntityType.COW
import org.bukkit.entity.EntityType.HORSE
import org.bukkit.entity.EntityType.LLAMA
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnHorse
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
		val potentialSpots = locateAround(world, centerX, centerZ, 11, 32.0, 80.0, 8) { x, z -> x to z }

		for ((x, z) in potentialSpots) {
			val surface = surfaceSpreader(world, x, z, 5, ::cowHorseGood)
			if (surface != null) {
				return listOf(surface.getRelative(UP))
			}
		}

		return null
	}

	override fun setBlock(block: Block) {
		val entityType = when {
			SpawnUtil.plains(block.biome) && Random.nextBoolean() -> LLAMA
			SpawnUtil.mountains(block.biome) && Random.nextBoolean() -> HORSE
			else -> COW
		}

		block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), entityType)
	}

	private val spawnHorse = SpawnHorse()

	/* placement */
	private fun cowHorseGood(surfaceBlock: Block): Boolean {
		return spawnHorse.allowSpawn(surfaceBlock.getRelative(UP), 0)
	}
}

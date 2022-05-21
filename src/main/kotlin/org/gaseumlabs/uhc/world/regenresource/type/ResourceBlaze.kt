package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.BLAZE
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnBlaze
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderNether
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionEntity

class ResourceBlaze(
	released: HashMap<PhaseType, Int>,
	chunkRadius: Int, //4?
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : ResourceDescriptionEntity(
	released,
	chunkRadius,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	override fun eligable(player: Player): Boolean {
		return true
	}

	override fun generateInChunk(chunk: Chunk): List<Block>? {
		val potentialSpots = RegenUtil.aroundInChunk(
			chunk,
			{ y -> RegenUtil.yRangeCenterBias(y, 0.0f, 1.0f, 32, 110) },
			32
		) { block ->
			if (block.isPassable) block else null
		}

		for (block in potentialSpots) {
			val surface = surfaceSpreaderNether(chunk.world, block.x, block.y, block.z, 4, ::blazeGood)
			if (surface != null) {
				return listOf(surface.getRelative(UP))
			}
		}

		return null
	}

	override fun setEntity(block: Block): Entity {
		val blaze = block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), BLAZE) as Blaze
		blaze.removeWhenFarAway = false
		return blaze
	}

	override fun isEntity(entity: Entity): Boolean {
		return entity.type === BLAZE
	}

	private val spawnBlaze = SpawnBlaze()

	/* placement */
	private fun blazeGood(surfaceBlock: Block): Boolean {
		return spawnBlaze.allowSpawn(surfaceBlock.getRelative(UP), 0)
	}
}

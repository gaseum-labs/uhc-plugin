package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.BLAZE
import org.bukkit.entity.EntityType.WITHER_SKELETON
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnBlaze
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderNether
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionEntity

class ResourceBlaze(
	released: HashMap<PhaseType, Int>,
	chunkRadius: Int,
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

	override fun generate(bounds: RegenUtil.GenBounds, fullVein: Boolean): List<Block>? {
		val potentialSpots = RegenUtil.volume(
			bounds,
			32..110,
			32
		) { block ->
			if (block.isPassable) block else null
		}

		for (block in potentialSpots) {
			val surface = surfaceSpreaderNether(block.world, block.x, block.y, block.z, 4, ::blazeGood)
			if (surface != null) {
				return listOf(surface.getRelative(UP))
			}
		}

		return null
	}

	override fun setEntity(block: Block, fullVein: Boolean): Entity {
		val entity = block.world.spawnEntity(
			block.location.add(0.5, 0.0, 0.5),
			if (fullVein) BLAZE else WITHER_SKELETON
		) as Monster
		entity.removeWhenFarAway = false
		return entity
	}
	
	override fun isEntity(entity: Entity): Boolean {
		return entity.type === BLAZE || entity.type === WITHER_SKELETON
	}

	private val spawnBlaze = SpawnBlaze()

	/* placement */
	private fun blazeGood(surfaceBlock: Block): Boolean {
		return spawnBlaze.allowSpawn(surfaceBlock.getRelative(UP), 0)
	}
}

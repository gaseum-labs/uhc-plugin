package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.Blaze
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType.BLAZE
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnBlaze
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderNether
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionEntity

class ResourceBlaze : ResourceDescriptionEntity(
	2,
	7,
	3,
	30 * 20,
	"Blaze"
) {
	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (world !== WorldManager.netherWorld) return null

		val potentialSpots = RegenUtil.locateAround(world, centerX, centerZ, 11, 48.0, 80.0, 8) { x, z -> x to z }

		for ((x, z) in potentialSpots) {
			val surface = surfaceSpreaderNether(world, x, centerY, z, 5, ::blazeGood)
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

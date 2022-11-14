package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.BLAZE
import org.bukkit.entity.EntityType.WITHER_SKELETON
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnBlaze
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderNether

class RegenResourceBlaze(
	released: HashMap<PhaseType, Release>,
	worldName: String,
	prettyName: String,
) : RegenResourceEntity(
	released,
	worldName,
	prettyName,
) {
	override fun eligible(player: Player) = true
	override fun onUpdate(vein: VeinEntity) {}

	override fun generateEntity(genBounds: RegenUtil.GenBounds, tier: Int): EntityGenResult? {
		val potentialSpots = RegenUtil.volume(
			genBounds,
			32..110,
			32
		) { block ->
			if (block.isPassable) block else null
		}

		for (block in potentialSpots) {
			surfaceSpreaderNether(block.world, block.x, block.y, block.z, 4) {
				spawnBlaze.allowSpawn(it.getRelative(UP), 0)
			}?.let {
				return EntityGenResult(it.getRelative(UP), 1)
			}
		}

		return null
	}

	override fun initializeEntity(block: Block, tier: Int): Entity {
		val entity = block.world.spawnEntity(
			block.location.add(0.5, 0.0, 0.5),
			if (tier == 0) BLAZE else WITHER_SKELETON
		) as Monster
		entity.removeWhenFarAway = false
		return entity
	}

	override fun isModifiedEntity(entity: Entity) = false

	private val spawnBlaze = SpawnBlaze()
}

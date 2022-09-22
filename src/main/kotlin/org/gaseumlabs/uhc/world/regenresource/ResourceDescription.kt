package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType

abstract class ResourceDescription(
	val released: HashMap<PhaseType, Int>,
	val chunkRadius: Int,
	val worldName: String,
	val chunkSpawnChance: Float,
	val prettyName: String,
) {
	lateinit var regenResource: RegenResource

	abstract fun eligable(player: Player): Boolean

	abstract fun generate(genBounds: RegenUtil.GenBounds, fullVein: Boolean): List<Block>?

	override fun toString(): String {
		return prettyName
	}
}

abstract class ResourceDescriptionBlock(
	released: HashMap<PhaseType, Int>,
	chunkRadius: Int,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : ResourceDescription(
	released,
	chunkRadius,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	abstract fun setBlock(block: Block, index: Int, fullVein: Boolean)

	/**
	 * IMPORTANT! THESE SHOULD NEVER OVERLAP BETWEEN ANY OTHER BLOCK RESOURCE DESCRIPTIONS
	 */
	abstract fun isBlock(block: Block): Boolean
}

abstract class ResourceDescriptionEntity(
	released: HashMap<PhaseType, Int>,
	chunkRadius: Int,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : ResourceDescription(
	released,
	chunkRadius,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	abstract fun setEntity(block: Block, fullVein: Boolean): Entity

	/**
	 * IMPORTANT! THESE SHOULD NEVER OVERLAP BETWEEN ANY OTHER BLOCK RESOURCE DESCRIPTIONS
	 */
	abstract fun isEntity(entity: Entity): Boolean
}


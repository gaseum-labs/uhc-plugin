package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.util.KeyGen

data class GenResult(val blocks: List<Block>, val value: Int)

abstract class RegenResource<V : Vein>(
	val released: HashMap<PhaseType, Int>,
	val worldName: String,
	val chunkSpawnChance: Float,
	val prettyName: String,
) {
	val id = ResourceId.register(this)
	val idName = prettyName.lowercase().replace(' ', '_')
	val chunkKey = KeyGen.genKey("resource_${idName}")

	abstract fun eligible(player: Player): Boolean

	abstract fun generate(genBounds: RegenUtil.GenBounds, fullVein: Boolean): GenResult?

	abstract fun isModified(vein: V): Boolean

	abstract fun onUpdate(vein: V)

	abstract fun createVein(
		x: Int,
		z: Int,
		partition: Int,
		timestamp: Int,
		value: Int,
		blocks: List<Block>,
		full: Boolean
	): V

	final override fun toString(): String {
		return prettyName
	}
}

abstract class RegenResourceBlock(
	released: HashMap<PhaseType, Int>,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : RegenResource<VeinBlock>(released, worldName, chunkSpawnChance, prettyName) {
	abstract fun initializeBlock(blocks: List<Block>, fullVein: Boolean)

	final override fun isModified(vein: VeinBlock) = isModifiedBlock(vein.blocks)
	abstract fun isModifiedBlock(blocks: List<Block>): Boolean

	override fun createVein(
		x: Int,
		z: Int,
		partition: Int,
		timestamp: Int,
		value: Int,
		blocks: List<Block>,
		full: Boolean
	): VeinBlock {
		val originalData = blocks.map { it.blockData }
		initializeBlock(blocks, full)
		blocks.forEach {
			it.setMetadata(GlobalResources.RESOURCE_KEY, FixedMetadataValue(UHCPlugin.plugin, id))
		}
		return VeinBlock(originalData, blocks, x, z, partition, timestamp, value)
	}
}

data class EntityGenResult(val block: Block, val value: Int)

abstract class RegenResourceEntity(
	released: HashMap<PhaseType, Int>,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : RegenResource<VeinEntity>(released, worldName, chunkSpawnChance, prettyName) {
 	final override fun generate(genBounds: RegenUtil.GenBounds, fullVein: Boolean) =
		 generateEntity(genBounds, fullVein)?.let { (block, value) -> GenResult(listOf(block), value) }
	abstract fun generateEntity(genBounds: RegenUtil.GenBounds, fullVein: Boolean): EntityGenResult?

	abstract fun initializeEntity(block: Block, fullVein: Boolean): Entity

	final override fun isModified(vein: VeinEntity) = !vein.isLoaded() || isModifiedEntity(vein.entity)
	abstract fun isModifiedEntity(entity: Entity): Boolean

	override fun createVein(
		x: Int,
		z: Int,
		partition: Int,
		timestamp: Int,
		value: Int,
		blocks: List<Block>,
		full: Boolean
	): VeinEntity {
		val entity = initializeEntity(blocks[0], full)
		entity.setMetadata(GlobalResources.RESOURCE_KEY, FixedMetadataValue(UHCPlugin.plugin, id))
		return VeinEntity(entity, x, z, partition, timestamp, value)
	}
}

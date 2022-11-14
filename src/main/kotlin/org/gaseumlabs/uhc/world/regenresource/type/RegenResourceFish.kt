package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.regenresource.GenResult
import org.gaseumlabs.uhc.world.regenresource.RegenResource
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.Vein

class VeinFish(
	val center: Block,
	val surface: Boolean,
	x: Int,
	z: Int,
	partition: Int,
	timestamp: Int,
	value: Int,
) : Vein(x, z, partition, timestamp, value) {
	override fun centerLocation() = center.location.toCenterLocation()

	override fun erase() {}
}

class RegenResourceFish(
	val yRange: IntRange,
	val eligibleRange: IntRange,
	val surface: Boolean,
	released: HashMap<PhaseType, Int>,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : RegenResource<VeinFish>(
	released,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	override fun eligible(player: Player) = eligibleRange.contains(player.location.y.toInt())

	override fun generate(genBounds: RegenUtil.GenBounds, fullVein: Boolean): GenResult? {
		val source = RegenUtil.perfectGen(
			4,
			genBounds,
			yRange,
			faces,
			{ when {
				it.type === Material.WATER -> 1
				it.isPassable || isIgnore(it) -> 0
				else -> 2
			} },
			{ when {
				it.type === Material.WATER -> true
				it.isPassable || isIgnore(it) -> false
				else -> null
			} },
		) ?: return null

		return GenResult(listOf(source), 1)
	}

	override fun isModified(vein: VeinFish) = vein.center.type !== Material.WATER

	override fun createVein(
		x: Int,
		z: Int,
		partition: Int,
		timestamp: Int,
		value: Int,
		blocks: List<Block>,
		full: Boolean
	): VeinFish {
		return VeinFish(blocks[0], surface, x, z, partition, timestamp, value)
	}

	override fun onUpdate(vein: VeinFish) {
		val world = vein.center.world

		world.spawnParticle(
			Particle.END_ROD,
			vein.centerLocation(),
			20,
			1.5,
			0.5,
			1.5,
			0.0
		)
	}

	companion object {
		val faces = arrayOf(BlockFace.DOWN)

		val ignore = Util.sortedArrayOf(
			Material.POINTED_DRIPSTONE,
			Material.BIG_DRIPLEAF,
			Material.LILY_PAD
		)

		fun isIgnore(block: Block) = Util.binarySearch(block.type, ignore)
	}
}

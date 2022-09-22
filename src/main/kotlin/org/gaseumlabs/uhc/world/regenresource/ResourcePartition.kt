package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.World

abstract class ResourcePartition(val size: Int = 0) {
	abstract fun selectFor(
		world: World,
		partition: Int,
		radius: Int,
	): RegenUtil.GenBounds

	companion object {
		fun partitionOfSize(n: Int): ResourcePartition? = when (n) {
			0 -> null
			9 -> ResourcePartitionGrid(3, 3)
			12 -> ResourcePartitionGrid(3, 4)
			16 -> ResourcePartitionGrid(4, 4)
			20 -> ResourcePartitionGrid(4, 5)
			25 -> ResourcePartitionGrid(5, 5)
			else -> throw Error("Invalid partition size of $n")
		}
	}
}

class ResourcePartitionGrid(val wide: Int, val tall: Int) : ResourcePartition(wide * tall) {
	override fun selectFor(world: World, partition: Int, radius: Int): RegenUtil.GenBounds {
		val pWidth = (radius * 2 + 1) / wide
		val pHeight = (radius * 2 + 1) / tall
		return RegenUtil.GenBounds(
			world,
			-radius + (partition / tall) * pWidth,
			-radius + (partition % tall) * pHeight,
			pWidth,
			pHeight
		)
	}
}
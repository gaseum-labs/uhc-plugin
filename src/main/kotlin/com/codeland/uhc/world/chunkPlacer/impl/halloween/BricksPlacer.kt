package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import org.bukkit.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class BricksPlacer(size: Int) : ChunkPlacer(size) {
	private val replaceable = arrayOf(
		Material.STONE,
		Material.DIRT,
		Material.ANDESITE,
		Material.GRANITE,
		Material.DIORITE,
		Material.GRASS_BLOCK,
		Material.NETHERRACK,
		Material.WARPED_NYLIUM,
		Material.CRIMSON_NYLIUM
	)

	init {
		replaceable.sort()
	}

	private val maxDistance = sqrt(5.0f.pow(2.0f) * 3)

	override fun place(chunk: Chunk) {
		val center = randomPositionBool(chunk, 8, 99) { block ->
			Util.binarySearch(block.type, replaceable)
		}

		if (center != null) for (i in -5..5) for (j in -5..5) for (k in -5..5) {
			val distance = sqrt((i * i) + (j * j) + (k * k.toFloat()))
			val chance = (maxDistance - distance) / maxDistance

			if (Random.nextFloat() < chance) {
				val placeBlock = center.getRelative(i, j, k)

				if (Util.binarySearch(placeBlock.type, replaceable)) {
					when (Random.nextInt(3)) {
						0 -> Material.STONE_BRICKS
						1 -> Material.MOSSY_STONE_BRICKS
						else -> Material.CRACKED_STONE_BRICKS
					}
				}
			}
		}
	}
}

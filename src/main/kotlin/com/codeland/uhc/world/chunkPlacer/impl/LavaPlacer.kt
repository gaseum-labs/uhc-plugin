package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World

class LavaPlacer : DelayedChunkPlacer(1) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		for (x in -1..1) for (z in -1..1)
			if (!world.isChunkGenerated(chunkX + x, chunkZ + z)) return false

		return true
	}

	fun layerHasEmpty(chunk: Chunk, y: Int): Boolean {
		for (x in 0..15) for (z in 0..15) {
			if (chunk.getBlock(x, y, z).isPassable) return true
		}

		return false
	}

	fun chunkLavaLevel(chunk: Chunk): Int {
		var lowestY = 10
		var foundEmpty = false

		for (y in 9 downTo 6) {
			if (layerHasEmpty(chunk, y)) {
				lowestY = y
				foundEmpty = true
			} else {
				if (foundEmpty) break
			}
		}

		return lowestY
	}

	fun edgeGuard(chunk: Chunk, y: Int, xRange: IntRange, zRange: IntRange) {
		for (x in xRange) for (z in zRange) {
			val block = chunk.getBlock(x, y, z)
			if (block.type === Material.LAVA) block.setType(Material.STONE, false)
		}
	}

	override fun place(chunk: Chunk, chunkIndex: Int) {
		val level = chunkLavaLevel(chunk)

		val leftLevel = chunkLavaLevel(chunk.world.getChunkAt(chunk.x - 1, chunk.z))
		val rightLevel = chunkLavaLevel(chunk.world.getChunkAt(chunk.x + 1, chunk.z))
		val upLevel = chunkLavaLevel(chunk.world.getChunkAt(chunk.x, chunk.z - 1))
		val downLevel = chunkLavaLevel(chunk.world.getChunkAt(chunk.x, chunk.z + 1))

		/* remove lava */
		for (y in 10 downTo level + 1) {
			for (x in 0..15) for (z in 0..15) {
				val block = chunk.getBlock(x, y, z)
				if (block.type === Material.LAVA) block.setType(Material.CAVE_AIR, false)
			}
		}

		/* add edges */
		for (y in level downTo 1) {
			if (leftLevel != level) edgeGuard(chunk, y, 0..0, 0..15)
			if (rightLevel != level) edgeGuard(chunk, y, 15..15, 0..15)
			if (upLevel != level) edgeGuard(chunk, y, 0..15, 0..0)
			if (downLevel != level) edgeGuard(chunk, y, 0..15, 15..15)
		}
	}
}

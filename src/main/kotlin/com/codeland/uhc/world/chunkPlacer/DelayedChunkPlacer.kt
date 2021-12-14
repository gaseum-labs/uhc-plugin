package com.codeland.uhc.world.chunkPlacer

import org.bukkit.Chunk
import org.bukkit.World
import java.util.concurrent.locks.*

abstract class DelayedChunkPlacer(size: Int) : AbstractChunkPlacer(size) {
	private var chunkList = ArrayList<Chunk>()

	abstract fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean

	private val lock = ReentrantLock()

	override fun onGenerate(chunk: Chunk, uniqueSeed: Long, worldSeed: Long) {
		try {
			lock.lock()

			if (shouldGenerate(chunk.x, chunk.z, uniqueSeed, worldSeed, size)) {
				chunkList.add(chunk)
			}

			chunkList.removeIf {
				if (chunkReady(it.world, it.x, it.z)) {
					place(it)
					true
				} else {
					false
				}
			}

		} finally {
			lock.unlock()
		}
	}

	companion object {
		fun chunkReadyPlus(world: World, chunkX: Int, chunkZ: Int): Boolean {
			return world.isChunkGenerated(chunkX + 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX + 1, chunkZ - 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ - 1)
		}

		fun chunkReadyAround(world: World, chunkX: Int, chunkZ: Int): Boolean {
			return world.isChunkGenerated(chunkX + 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX + 1, chunkZ - 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ - 1) &&

			world.isChunkGenerated(chunkX + 1, chunkZ) &&
			world.isChunkGenerated(chunkX - 1, chunkZ) &&
			world.isChunkGenerated(chunkX, chunkZ + 1) &&
			world.isChunkGenerated(chunkX, chunkZ - 1)
		}
	}
}

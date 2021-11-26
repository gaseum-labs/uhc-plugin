package com.codeland.uhc.world.chunkPlacer

import com.codeland.uhc.UHCPlugin
import org.bukkit.*

abstract class DelayedChunkPlacer(size: Int) : AbstractChunkPlacer(size) {
	private var chunkList = ArrayList<Pair<Chunk, Int>>()

	abstract fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean

	override fun onGenerate(chunk: Chunk, seed: Int) {
		val chunkIndex = shouldGenerate(chunk.x, chunk.z, seed, uniqueSeed, size)
		if (chunkIndex != -1) chunkList.add(Pair(chunk, chunkIndex))

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
			var safeChunks = 0

			val removeChunks = Array(chunkList.size) { i ->
				val checkChunk = chunkList[i].first

				if (chunkReady(checkChunk.world, checkChunk.x, checkChunk.z)) {
					place(checkChunk, chunkList[i].second)
					true
				} else {
					++safeChunks
					false
				}
			}

			val newChunkList = ArrayList<Pair<Chunk, Int>>(safeChunks)

			removeChunks.forEachIndexed { i, removed ->
				if (!removed) newChunkList.add(chunkList[i])
			}

			chunkList = newChunkList
		}
	}

	override fun reset(uniqueSeed: Int) {
		super.reset(uniqueSeed)

		chunkList.clear()
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

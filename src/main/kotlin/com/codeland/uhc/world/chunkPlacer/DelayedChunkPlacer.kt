package com.codeland.uhc.world.chunkPlacer

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Chunk
import org.bukkit.World
import kotlin.system.exitProcess

abstract class DelayedChunkPlacer(size: Int, uniqueSeed: Int) : AbstractChunkPlacer(size, uniqueSeed) {
	private var chunkList = ArrayList<Chunk>()

	abstract fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean

	override fun onGenerate(chunk: Chunk, seed: Int) {
		if (
			shouldGenerate(chunk.x, chunk.z, seed, uniqueSeed, size)
		) {
			chunkList.add(chunk)
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
			var safeChunks = 0

			val removeChunks = Array(chunkList.size) { i ->
				val checkChunk = chunkList[i]

				if (chunkReady(checkChunk.world, checkChunk.x, checkChunk.z)) {
					place(checkChunk)
					true
				} else {
					++safeChunks
					false
				}
			}

			val newChunkList = ArrayList<Chunk>(safeChunks)

			removeChunks.forEachIndexed { i, removed ->
				if (!removed) newChunkList.add(chunkList[i])
			}

			chunkList = newChunkList
		}
	}
}

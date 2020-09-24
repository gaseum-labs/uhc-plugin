package com.codeland.uhc.world

import com.codeland.uhc.util.Util
import org.apache.commons.collections4.QueueUtils
import org.bukkit.Chunk
import java.util.Queue
import kotlin.math.ceil
import kotlin.math.floor

object CaveLocatorQueue {
	val chunkList = ArrayList<Chunk>()

	fun validChunk(chunk: Chunk): Boolean {
		for (x in -3..3)
			for (z in -3..3)
				if (!chunk.world.isChunkGenerated(chunk.x + x, chunk.z + z)) return false

		return true
	}

	fun onGenerate(chunk: Chunk, radius: Int) {
		val left = floor(-radius / 16.0).toInt()
		val right = ceil(radius / 16.0).toInt()

		/* this chunk should have minerals placed if within radius */
		if (chunk.x >= left && chunk.z >= left && chunk.x <= right && chunk.z <= right) {
			chunkList.add(chunk)
		}

		try {
			chunkList.removeIf { checkChunk ->
				if (validChunk(checkChunk)) {
					CaveLocator.locateChunkCaves(checkChunk, radius)
					true
				} else {
					false
				}
			}
		} catch (ex: ConcurrentModificationException) {}
	}
}

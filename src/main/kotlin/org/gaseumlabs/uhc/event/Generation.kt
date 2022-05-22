package org.gaseumlabs.uhc.event

import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.world.WorldGenOption.*
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.chunkPlacer.ChunkPlacer
import org.gaseumlabs.uhc.world.chunkPlacer.ChunkPlacerHolder.*
import org.bukkit.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkPopulateEvent

class Generation : Listener {
	data class SuspendedChunk(val key: Long, val placerList: ArrayList<ChunkPlacer>)

	companion object {
		private var suspendedChunks = HashMap<World, ArrayList<SuspendedChunk>>()

		fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
			return world.isChunkGenerated(chunkX + 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ + 1) &&
			world.isChunkGenerated(chunkX + 1, chunkZ - 1) &&
			world.isChunkGenerated(chunkX - 1, chunkZ - 1) &&

			world.isChunkGenerated(chunkX + 1, chunkZ) &&
			world.isChunkGenerated(chunkX - 1, chunkZ) &&
			world.isChunkGenerated(chunkX, chunkZ + 1) &&
			world.isChunkGenerated(chunkX, chunkZ - 1)
		}

		fun cleanSuspended(world: World) {
			suspendedChunks.remove(world)
		}
	}

	@EventHandler
	fun onChunkLoad(event: ChunkPopulateEvent) {
		val world = event.world
		val chunk = event.chunk
		val config = UHC.getConfig()

		/** synchronize and delay the chunk placers */

		SchedulerUtil.later(suspendedChunks[world]?.size?.toLong() ?: 0L) {
			val placerList = ArrayList<ChunkPlacer>()

			if (world.name == WorldManager.GAME_WORLD_NAME) {
				if (config.worldGenEnabled(HALLOWEEN)) {
					PUMPKIN.addToList(chunk, placerList)
					DEAD_BUSH.addToList(chunk, placerList)
					LANTERN.addToList(chunk, placerList)
					COBWEB.addToList(chunk, placerList)
					BANNER.addToList(chunk, placerList)
					BRICKS.addToList(chunk, placerList)
				}

				if (config.worldGenEnabled(CHRISTMAS)) {
					SNOW.addToList(chunk, placerList)
				}

				if (config.worldGenEnabled(TOWERS)) {
					TOWER.addToList(chunk, placerList)
				}

			} else if (world.name == WorldManager.NETHER_WORLD_NAME) {
				
			}

			suspendedChunks[world]?.removeIf { (key, currentList) ->
				val currentChunk = world.getChunkAt(key)

				if (chunkReady(currentChunk.world, currentChunk.x, currentChunk.z)) {
					currentList.forEach { placer -> placer.place(currentChunk) }
					true
				} else {
					false
				}
			}

			suspendedChunks.getOrPut(world) { ArrayList() }
				.add(SuspendedChunk(chunk.chunkKey, placerList))
		}
	}
}
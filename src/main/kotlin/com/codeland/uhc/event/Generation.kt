package com.codeland.uhc.event

import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.WorldGenOption.*
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.chunkPlacer.ChunkPlacer
import com.codeland.uhc.world.chunkPlacer.ChunkPlacerHolder.*
import com.codeland.uhc.world.chunkPlacer.impl.*
import org.bukkit.*
import org.bukkit.Material.AIR
import org.bukkit.Material.MELON
import org.bukkit.entity.Animals
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
		/** santizie chunk */

		val world = event.world
		val chunk = event.chunk
		val config = UHC.getConfig()

		if (world.name == WorldManager.GAME_WORLD_NAME) {
			/* no chunk animals */
			chunk.entities.forEach { entity ->
				if (entity is Animals) entity.remove()
			}

			/* remove chunk melons */
			for (y in 63..120) for (x in 0..15) for (z in 0..15) {
				val block = chunk.getBlock(x, y, z)
				if (block.type === MELON) block.setType(AIR, false)
			}

			/* remove chunk ores */
			if (config.worldGenEnabled(ORE_FIX) || config.worldGenEnabled(REVERSE_ORE_FIX)) {
				OrePlacer.removeOres(chunk)
			}

			/* remove chunk oxeyes */
			if (config.worldGenEnabled(MUSHROOM_FIX)) {
				OxeyePlacer.removeOxeye(chunk)
			}

			/* remove chunk sugar cane */
			if (config.worldGenEnabled(SUGAR_CANE_REGEN)) {
				for (x in 0..15) {
					for (z in 0..15) {
						for (y in 60..86) {
							val block = chunk.getBlock(x, y, z)
							if (block.type === Material.SUGAR_CANE) block.setType(AIR, false)
						}
					}
				}
			}
		}

		/** synchronize and delay the chunk placers */

		SchedulerUtil.later(suspendedChunks[world]?.size?.toLong() ?: 0L) {
			val placerList = ArrayList<ChunkPlacer>()

			if (world.name == WorldManager.GAME_WORLD_NAME) {
				AMETHYST.addToList(chunk, placerList)

				if (config.worldGenEnabled(REVERSE_ORE_FIX)) {
					REVERSE_DIAMOND.addToList(chunk, placerList)
					REVERSE_GOLD.addToList(chunk, placerList)
					REVERSE_LAPIS.addToList(chunk, placerList)
					REVERSE_COPPER.addToList(chunk, placerList)
					REVERSE_REDSTONE.addToList(chunk, placerList)
					REVERSE_IRON.addToList(chunk, placerList)
					REVERSE_COAL.addToList(chunk, placerList)

				} else if (config.worldGenEnabled(ORE_FIX)) {
					DIAMOND.addToList(chunk, placerList)
					GOLD.addToList(chunk, placerList)
					LAPIS.addToList(chunk, placerList)
					EMERALD.addToList(chunk, placerList)
				}

				if (config.worldGenEnabled(MUSHROOM_FIX)) {
					OXEYE.addToList(chunk, placerList)
					RED_MUSHROOM.addToList(chunk, placerList)
					BROWN_MUSHROOM.addToList(chunk, placerList)
				}

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
				if (config.worldGenEnabled(NETHER_FIX)) {
					BLACKSTONE.addToList(chunk, placerList)
					DEBRIS.addToList(chunk, placerList)
					MAGMA.addToList(chunk, placerList)
					LAVA_STREAM.addToList(chunk, placerList)
					BASALT.addToList(chunk, placerList)
					WART.addToList(chunk, placerList)
				}
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

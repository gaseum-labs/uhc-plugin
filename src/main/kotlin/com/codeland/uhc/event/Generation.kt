package com.codeland.uhc.event

import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.WorldGenOption.*
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.chunkPlacer.ChunkPlacerHolder
import com.codeland.uhc.world.chunkPlacer.ChunkPlacerHolder.*
import com.codeland.uhc.world.chunkPlacer.impl.*
import org.bukkit.Material.AIR
import org.bukkit.Material.MELON
import org.bukkit.entity.Animals
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkPopulateEvent

class Generation : Listener {
	@EventHandler
	fun onChunkLoad(event: ChunkPopulateEvent) {
		SchedulerUtil.nextTick {
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

				AMETHYST.onGenerate(chunk, world.seed)

				if (config.worldGenEnabled(ORE_FIX) || config.worldGenEnabled(REVERSE_ORE_FIX)) {
					OrePlacer.removeOres(chunk)
				}

				if (config.worldGenEnabled(REVERSE_ORE_FIX)) {
					REVERSE_DIAMOND.onGenerate(chunk, world.seed)
					REVERSE_LAPIS.onGenerate(chunk, world.seed)
					REVERSE_GOLD.onGenerate(chunk, world.seed)
					REVERSE_COPPER.onGenerate(chunk, world.seed)
					REVERSE_REDSTONE.onGenerate(chunk, world.seed)
					REVERSE_IRON.onGenerate(chunk, world.seed)
					REVERSE_COAL.onGenerate(chunk, world.seed)

				} else if (config.worldGenEnabled(ORE_FIX)) {
					DIAMOND.onGenerate(chunk, world.seed)
					GOLD.onGenerate(chunk, world.seed)
					LAPIS.onGenerate(chunk, world.seed)
					EMERALD.onGenerate(chunk, world.seed)
				}

				if (config.worldGenEnabled(MUSHROOM_FIX)) {
					OxeyePlacer.removeOxeye(chunk)
					OXEYE.onGenerate(chunk, world.seed)
					RED_MUSHROOM.onGenerate(chunk, world.seed)
					BROWN_MUSHROOM.onGenerate(chunk, world.seed)
				}

				if (config.worldGenEnabled(SUGAR_CANE_FIX) || config.worldGenEnabled(SUGAR_CANE_REGEN)) {
					SugarCanePlacer.removeSugarCane(chunk)
				}

				if (config.worldGenEnabled(SUGAR_CANE_FIX)) {
					DEEP_SUGAR_CANE.onGenerate(chunk, world.seed)
					LOW_SUGAR_CANE.onGenerate(chunk, world.seed)
					HIGH_SUGAR_CANE.onGenerate(chunk, world.seed)
				}

				if (config.worldGenEnabled(HALLOWEEN)) {
					PUMPKIN.onGenerate(chunk, world.seed)
					DEAD_BUSH.onGenerate(chunk, world.seed)
					LANTERN.onGenerate(chunk, world.seed)
					COBWEB.onGenerate(chunk, world.seed)
					BANNER.onGenerate(chunk, world.seed)
					BRICKS.onGenerate(chunk, world.seed)
				}

				if (config.worldGenEnabled(CHRISTMAS)) {
					SNOW.onGenerate(chunk, world.seed)
				}

				if (config.worldGenEnabled(TOWERS)) {
					TOWER.onGenerate(chunk, world.seed)
				}

			} else if (world.name == WorldManager.NETHER_WORLD_NAME) {
				if (config.worldGenEnabled(NETHER_FIX)) {
					BLACKSTONE.onGenerate(chunk, world.seed)
					DEBRIS.onGenerate(chunk, world.seed)
					MAGMA.onGenerate(chunk, world.seed)
					LAVA_STREAM.onGenerate(chunk, world.seed)
					BASALT.onGenerate(chunk, world.seed)
					WART.onGenerate(chunk, world.seed)
				}
			}
		}
	}
}

package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.*
import com.codeland.uhc.world.WorldGenOption.*
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.chunkPlacer.impl.OxeyePlacer
import com.codeland.uhc.world.chunkPlacer.impl.SugarCanePlacer
import com.codeland.uhc.world.chunkPlacerHolder.*
import com.codeland.uhc.world.chunkPlacerHolder.type.*
import org.bukkit.Bukkit
import org.bukkit.entity.Animals
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkPopulateEvent

class Generation : Listener {
	@EventHandler
	fun onChunkLoad(event: ChunkPopulateEvent) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
			val world = event.world
			val chunk = event.chunk

			val config = UHC.getConfig()

			if (world.name == WorldManager.GAME_WORLD_NAME) {
				/* no chunk animals */
				chunk.entities.forEach { entity ->
					if (entity is Animals) entity.remove()
				}

				OreFix.amethystPlacer.onGenerate(chunk, world.seed.toInt())

				if (config.worldGenEnabled(CAVE_INDICATORS)) {
					OreFix.removeMinerals(chunk)
					OreFix.mineralPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (config.worldGenEnabled(ORE_FIX) || config.worldGenEnabled(REVERSE_ORE_FIX)) {
					OreFix.removeOres(chunk)
				}

				if (config.worldGenEnabled(REVERSE_ORE_FIX)) {
					OreFix.reverseCoalPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseIronPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseRedstonePlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseCopperPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseGoldPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseLapisPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseDiamondPlacer.onGenerate(chunk, world.seed.toInt())

				} else if (config.worldGenEnabled(ORE_FIX)) {
					OreFix.diamondPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.goldPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.lapisPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.emeraldPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (config.worldGenEnabled(NETHER_INDICATORS)) {
					NetherIndicators.netherIndicatorPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (config.worldGenEnabled(MUSHROOM_FIX)) {
					OxeyePlacer.removeOxeye(chunk)
					MushroomOxeyeFix.oxeyePlacer.onGenerate(chunk, world.seed.toInt())
					MushroomOxeyeFix.redMushroomPlacer.onGenerate(chunk, world.seed.toInt())
					MushroomOxeyeFix.brownMushroomPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (config.worldGenEnabled(MELON_FIX)) {
					MelonFix.removeMelons(chunk)
					MelonFix.melonPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (config.worldGenEnabled(SUGAR_CANE_FIX) || config.worldGenEnabled(SUGAR_CANE_REGEN)) {
					SugarCanePlacer.removeSugarCane(chunk)
				}

				if (config.worldGenEnabled(SUGAR_CANE_FIX)) {
					SugarCaneFix.deepSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
					SugarCaneFix.lowSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
					SugarCaneFix.highSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (config.worldGenEnabled(HALLOWEEN)) {
					HalloweenWorld.pumpkinPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.deadBushPlacer.onGenerate(chunk, world.seed.toInt())

					HalloweenWorld.lanternPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.cobwebPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.bannerPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.bricksPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (config.worldGenEnabled(CHRISTMAS)) {
					ChristmasWorld.snowPlacer.onGenerate(chunk, world.seed.toInt())
				}

			} else if (world.name == WorldManager.NETHER_WORLD_NAME) {
				if (config.worldGenEnabled(NETHER_FIX)) {
					NetherFix.blackstonePlacer.onGenerate(chunk, world.seed.toInt())
					NetherFix.magmaPlacer.onGenerate(chunk, world.seed.toInt())
					NetherFix.lavaStreamPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.debrisPlacer.onGenerate(chunk, world.seed.toInt())
					NetherFix.basaltPlacer.onGenerate(chunk, world.seed.toInt())

					NetherFix.wartPlacer.onGenerate(chunk, world.seed.toInt())
				}
			}
		}
	}
}

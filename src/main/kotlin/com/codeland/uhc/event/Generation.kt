package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.*
import com.codeland.uhc.world.WorldGenOption.*
import com.codeland.uhc.world.WorldGenOption.Companion.getEnabled
import com.codeland.uhc.world.WorldManager
import com.codeland.uhc.world.chunkPlacer.impl.OxeyePlacer
import com.codeland.uhc.world.chunkPlacer.impl.SugarCanePlacer
import com.codeland.uhc.world.chunkPlacerHolder.*
import com.codeland.uhc.world.chunkPlacerHolder.type.*
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.entity.Ageable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkPopulateEvent

class Generation : Listener {
	@EventHandler
	fun onChunkLoad(event: ChunkPopulateEvent) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
			val world = event.world
			val chunk = event.chunk

			/* only change world generation of game worlds */
			if (WorldManager.isNonGameWorld(world)) return@scheduleSyncDelayedTask

			if (world.environment === World.Environment.NORMAL) {
				/* no baby animals */
				chunk.entities.forEach { entity ->
					if (entity is Ageable) entity.setAdult()
				}

				if (getEnabled(NETHER_INDICATORS)) {
					NetherIndicators.netherIndicatorPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (getEnabled(ORE_FIX) || getEnabled(REVERSE_ORE_FIX)) {
					OreFix.removeOres(chunk)
				}

				if (getEnabled(REVERSE_ORE_FIX)) {
					OreFix.reverseCoalPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseIronPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseRedstonePlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseCopperPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseGoldPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseLapisPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.reverseDiamondPlacer.onGenerate(chunk, world.seed.toInt())

				} else if (getEnabled(ORE_FIX)) {
					OreFix.removeMinerals(chunk)
					OreFix.reduceLava(chunk)

					OreFix.diamondPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.goldPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.lapisPlacer.onGenerate(chunk, world.seed.toInt())
					OreFix.mineralPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (getEnabled(MUSHROOM_FIX)) {
					OxeyePlacer.removeOxeye(chunk)
					MushroomOxeyeFix.oxeyePlacer.onGenerate(chunk, world.seed.toInt())

					MushroomOxeyeFix.redMushroomPlacer.onGenerate(chunk, world.seed.toInt())
					MushroomOxeyeFix.brownMushroomPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (getEnabled(MELON_FIX)) {
					MelonFix.removeMelons(chunk)
					MelonFix.melonPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (getEnabled(SUGAR_CANE_FIX)) {
					SugarCanePlacer.removeSugarCane(chunk)
					SugarCaneFix.deepSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
					SugarCaneFix.lowSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
					SugarCaneFix.highSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (getEnabled(DUNGEON_FIX)) {
					DungeonFix.dungeonChestReplacer.onGenerate(chunk, world.seed.toInt())
				}

				if (getEnabled(HALLOWEEN)) {
					HalloweenWorld.pumpkinPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.deadBushPlacer.onGenerate(chunk, world.seed.toInt())

					HalloweenWorld.lanternPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.cobwebPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.bannerPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.bricksPlacer.onGenerate(chunk, world.seed.toInt())
				}

				if (getEnabled(CHRISTMAS)) {
					ChristmasWorld.snowPlacer.onGenerate(chunk, world.seed.toInt())
				}

			} else if (world.environment === World.Environment.NETHER) {
				if (getEnabled(NETHER_FIX)) {
					NetherFix.wartPlacer.onGenerate(chunk, world.seed.toInt())
				}
			}
		}
	}
}

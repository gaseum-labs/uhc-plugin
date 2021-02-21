package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.*
import com.codeland.uhc.world.type.MushroomOxeyeFix
import com.codeland.uhc.world.chunkPlacer.impl.OxeyePlacer
import com.codeland.uhc.world.chunkPlacer.impl.SugarCanePlacer
import com.codeland.uhc.world.type.*
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkPopulateEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.ceil

class Generation : Listener {
	@EventHandler
	fun onChunkLoad(event: ChunkPopulateEvent) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
			val world = event.world
			val chunk = event.chunk

			/* do not attempt to change world generation in the lobby worlds */
			if (WorldManager.isNonGameWorld(world)) return@scheduleSyncDelayedTask

			if (GameRunner.netherWorldFix && world.environment == World.Environment.NETHER) {
				NetherFix.wartPlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.netherIndicators && world.environment == World.Environment.NORMAL) {
				NetherIndicators.netherIndicatorPlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.oreWorldFix && world.environment == World.Environment.NORMAL) {
				OreFix.removeMinerals(chunk)
				OreFix.removeOres(chunk)
				OreFix.reduceLava(chunk)

				OreFix.diamondPlacer.onGenerate(chunk, world.seed.toInt())
				OreFix.goldPlacer.onGenerate(chunk, world.seed.toInt())
				OreFix.lapisPlacer.onGenerate(chunk, world.seed.toInt())
				OreFix.mineralPlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.mushroomWorldFix && world.environment == World.Environment.NORMAL) {
				OxeyePlacer.removeOxeye(chunk)
				MushroomOxeyeFix.oxeyePlacer.onGenerate(chunk, world.seed.toInt())

				MushroomOxeyeFix.redMushroomPlacer.onGenerate(chunk, world.seed.toInt())
				MushroomOxeyeFix.brownMushroomPlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.melonWorldFix && world.environment == World.Environment.NORMAL) {
				MelonFix.melonPlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.sugarCaneWorldFix && world.environment == World.Environment.NORMAL) {
				SugarCanePlacer.removeSugarCane(chunk)
				SugarCaneFix.deepSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
				SugarCaneFix.lowSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
				SugarCaneFix.highSugarCanePlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.dungeonWorldFix && world.environment == World.Environment.NORMAL) {
				DungeonFix.dungeonChestReplacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.waterWorld && world.environment == World.Environment.NORMAL) {
				WaterWorld.waterPlacer.onGenerate(chunk, world.seed.toInt())
				WaterWorld.removeEntities(chunk)
			}

			if (GameRunner.halloweenGeneration) {
				if (world.environment == World.Environment.NORMAL) {
					HalloweenWorld.pumpkinPlacer.onGenerate(chunk, world.seed.toInt())
					HalloweenWorld.deadBushPlacer.onGenerate(chunk, world.seed.toInt())
				}

				HalloweenWorld.lanternPlacer.onGenerate(chunk, world.seed.toInt())
				HalloweenWorld.cobwebPlacer.onGenerate(chunk, world.seed.toInt())
				HalloweenWorld.bannerPlacer.onGenerate(chunk, world.seed.toInt())
				HalloweenWorld.bricksPlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.christmasGeneration && world.environment == World.Environment.NORMAL) {
				ChristmasWorld.snowPlacer.onGenerate(chunk, world.seed.toInt())
			}

			if (GameRunner.chunkSwapping) {
				ChunkSwap.chunkSwapper.onGenerate(chunk, world.seed.toInt())
			}
		}
	}
}

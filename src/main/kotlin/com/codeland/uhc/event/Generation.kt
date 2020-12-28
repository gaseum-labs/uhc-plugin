package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.*
import com.codeland.uhc.world.*
import com.codeland.uhc.world.MushroomOxeyeFix
import com.codeland.uhc.world.chunkPlacer.impl.OxeyePlacer
import com.codeland.uhc.world.chunkPlacer.impl.SugarCanePlacer
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

			if (GameRunner.netherWorldFix && world.environment == World.Environment.NETHER) {
				NetherFix.wartPlacer.onGenerate(chunk, world.seed.toInt())
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

			//diamondPictureChunk(chunk)
		}
	}

	val numChunks = ceil(1001 / 16.0).toInt()
	val offset = numChunks / 2
	val img = BufferedImage(numChunks, numChunks, BufferedImage.TYPE_INT_ARGB)

	fun diamondPictureChunk(chunk: Chunk) {
		val x = chunk.x + offset
		val z = chunk.z + offset

		if (x >= 0 && z >= 0 && x < numChunks && z < numChunks) {
			var caveCount = 0

			for (x in 0..15) for (z in 0..15) for (y in 11..15)
				if (chunk.getBlock(x, y, x).isPassable) ++caveCount


			var caveValue = caveCount / (16 * 16 * 2f)
			if (caveValue > 1f) caveValue = 1f

			img.setRGB(x, z, (caveValue * 0xff).toInt().shl(16).or(0xff000000.toInt()))
			ImageIO.write(img, "png", File("diamondCaves.png"))
		}
	}
}

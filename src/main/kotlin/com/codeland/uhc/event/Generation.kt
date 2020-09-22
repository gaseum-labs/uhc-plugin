package com.codeland.uhc.event

import com.codeland.uhc.core.*
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkPopulateEvent
import kotlin.math.abs
import kotlin.math.log

class Generation : Listener {
	@EventHandler
	fun onChunkLoad(event: ChunkPopulateEvent) {
		val world = event.world
		val chunk = event.chunk

		/* prevent animal spawns in the waiting area */
		if (GameRunner.uhc.isPhase(PhaseType.WAITING) && world.environment == World.Environment.NORMAL && (abs(chunk.x) > 10 || abs(chunk.z) > 10)) {
			chunk.entities.forEach { entity ->
				entity.remove()
			}
		}

		/* mushroom fix */
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 63..121) {
					val block = chunk.getBlock(x, y, z)
					if (block.type == Material.BROWN_MUSHROOM || block.type == Material.RED_MUSHROOM) {
						if (block.lightLevel > 12) {
							block.setType(Material.AIR, false)
						}
					}
				}
			}
		}

		if (GameRunner.netherWorldFix && world.environment == World.Environment.NETHER) {
			NetherFix.wartPlacer.place(chunk, world.seed.toInt())
		}

		if (GameRunner.mushroomWorldFix && world.environment == World.Environment.NORMAL) {
			StewFix.removeOxeye(chunk)
			StewFix.addCaveMushrooms(chunk, world.seed.toInt())
		}

		if (GameRunner.oreWorldFix && world.environment == World.Environment.NORMAL) {
			OreFix.removeOres(chunk)
			OreFix.addOres(chunk, world.seed.toInt())
		}

		if (GameRunner.melonWorldFix && world.environment == World.Environment.NORMAL) {
			MelonFix.melonPlacer.place(chunk, world.seed.toInt())
		}
	}
}

package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.phases.endgame.AbstractEndgame
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Chest

object PvpArena {
	/**
	 * if the prepareArena process has already been done to this world
	 *
	 * it leaves a barrier as an indicator
	 */
	private fun readArenaMarker(world: World, radius: Int): Pair<Int, Int>? {
		val chest = world.getBlockAt(-radius, 1, -radius).getState(false) as? Chest ?: return null

		val strParts = chest.customName?.split('|') ?: return null

		if (strParts.size != 2) return null

		val min = strParts[0].toIntOrNull() ?: return null
		val max = strParts[1].toIntOrNull() ?: return null

		return Pair(min, max)
	}

	private fun createArenaMarker(world: World, radius: Int, uhc: UHC) {
		val block = world.getBlockAt(-radius, 1, -radius)

		block.setType(Material.CHEST, false)
		val chest = block.getState(false) as Chest

		chest.customName = "${uhc.lobbyPVPMin}|${uhc.lobbyPVPMax}"
	}

	/**
	 * applies an endgame like effect to the pvp arena to limit players skybasing and hiding underground
	 */
	fun prepareArena(world: World, radius: Int, uhc: UHC) {
		val arenaMarker = readArenaMarker(world, radius)

		/* arena has not yet been created */
		if (arenaMarker == null) {
			val (min, max) = AbstractEndgame.determineMinMax(world, radius, 100)

			uhc.lobbyPVPMin = min
			uhc.lobbyPVPMax = max + 3

			for (x in -radius..radius) for (z in -radius..radius) {
				for (y in max + 1..255)
					world.getBlockAt(x, y, z).setType(Material.AIR, false)

				for (y in 0..min - 1)
					world.getBlockAt(x, y, z).setType(Material.BEDROCK, false)
			}

			/* mark prepared */
			createArenaMarker(world, radius, uhc)

			/* arena has been created */
		} else {
			uhc.lobbyPVPMin = arenaMarker.first
			uhc.lobbyPVPMax = arenaMarker.second
		}
	}
}

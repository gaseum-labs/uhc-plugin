package com.codeland.uhc.phases.final

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

class FinalDefault : Phase() {

	override fun customStart() {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendGameMessage(player, "The border has stopped shrinking")
		}

		if (uhc.netherToZero) {
			for (world in Bukkit.getServer().worlds) {
				if (world.environment == World.Environment.NETHER) {
					world.worldBorder.center = Location(world, 10000.0, 0.0, 10000.0)

					break
				}
			}
		}
	}

	override fun perSecond(remainingSeconds: Int) {

	}

	override fun getCountdownString(): String {
		return "Glowing starts in"
	}

	override fun endPhrase(): String {
		return "GLOWING WILL BE APPLIED"
	}
}

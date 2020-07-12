package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World

class EndgameDeathmatch : Phase() {

	override fun customStart() {
		for (w in Bukkit.getServer().worlds) {
			if (w.environment == World.Environment.NORMAL) {
				var border = w.worldBorder.size.toInt() / 2 + 1

				for (x in -border..border) {
					for (z in -border..border) {
						w.getBlockAt(x, 255, z).setType(Material.BARRIER, false)
					}
				}

				val minSpreadDist = (uhc.preset.startRadius * 3.1415926 / GameRunner.quickRemainingTeams().toDouble())
				Bukkit.getServer().dispatchCommand(uhc.gameMaster!!, "spreadplayers 0 0 $minSpreadDist ${uhc.preset.endRadius} true @a")
			}
		}
	}

	override fun perSecond(remainingSeconds: Long) {

	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun endPhrase(): String {
		return ""
	}
}

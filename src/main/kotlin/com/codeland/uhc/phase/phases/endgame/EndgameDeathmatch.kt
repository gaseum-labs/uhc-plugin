package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.boss.BossBar

class EndgameDeathmatch : Phase() {

	override fun customStart() {
		EndgameNone.closeNether()

		for (w in Bukkit.getServer().worlds) {
			if (w.environment == World.Environment.NORMAL) {
				var border = w.worldBorder.size.toInt() / 2 + 1

				for (x in -border..border) {
					for (z in -border..border) {
						w.getBlockAt(x, 255, z).setType(Material.BARRIER, false)
					}
				}

				val minSpreadDist = uhc.startRadius
				Bukkit.getServer().dispatchCommand(uhc.gameMaster!!, "spreadplayers 0 0 $minSpreadDist ${uhc.endRadius} true @a")
			}
		}
	}

	override fun customEnd() {}
	override fun onTick(currentTick: Int) {}
	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		barStatic(bossBar)
	}

	override fun endPhrase(): String {
		return ""
	}
}

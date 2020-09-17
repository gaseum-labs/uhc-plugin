package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.phase.Phase
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.boss.BossBar

class EndgamePoison : Phase() {
	override fun customStart() {
		EndgameNone.closeNether()
	}

	override fun customEnd() {}

	override fun onTick(currentTick: Int) {
		for (player in Bukkit.getServer().onlinePlayers) {
			player.health -= 0.05
		}
	}

	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		barStatic(bossBar)
	}

	override fun endPhrase(): String {
		return ""
	}
}

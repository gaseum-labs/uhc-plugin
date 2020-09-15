package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.phase.Phase
import org.bukkit.World
import org.bukkit.boss.BossBar

class EndgameNone : Phase() {
	override fun customStart() {}
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

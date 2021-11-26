package com.codeland.uhc.core.phase.phases

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import org.bukkit.World

class Postgame(game: Game) : Phase(PhaseType.POSTGAME, 0, game) {
	override fun endPhrase(): String {
		return ""
	}

	override fun updateBarLength(remainingTicks: Int): Float {
		return 1.0f
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		UHC.containSpecs()
	}
}

package com.codeland.uhc.core.phase.phases

import com.codeland.uhc.component.UHCComponent
import com.codeland.uhc.component.UHCStyle.BOLD
import com.codeland.uhc.core.Game
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.util.Util
import org.bukkit.World

class Grace(game: Game, time: Int) : Phase(PhaseType.GRACE, time, game) {
	override fun updateBarLength(remainingTicks: Int): Float {
		return barLengthRemaining(remainingTicks)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent {
		return UHCComponent.text("Grade Period Ends in", phaseType.color)
			.and(Util.timeString(remainingSeconds), phaseType.color, BOLD)
	}

	override fun endPhrase(): String {
		return "Grace Period Ending"
	}
}

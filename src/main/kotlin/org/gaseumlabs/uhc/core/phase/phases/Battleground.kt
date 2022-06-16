package org.gaseumlabs.uhc.core.phase.phases

import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.component.UHCStyle.BOLD
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.util.Util
import org.bukkit.World
import org.gaseumlabs.uhc.component.UHCColor

class Battleground(game: Game, time: Int) : Phase(PhaseType.BATTLEGROUND, time, game) {
	init {
		game.world.worldBorder.size = (game.config.battlegroundRadius.get() * 2 + 1).toDouble()
	}

	override fun updateBarLength(remainingTicks: Int): Float {
		return barLengthRemaining(remainingTicks)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent {
		return UHCComponent.text("Battleground", phaseType.color)
			.and(" Ends in ", UHCColor.U_WHITE)
			.and(Util.timeString(remainingSeconds), phaseType.color, BOLD)
	}

	override fun endPhrase(): String {
		return "Endgame Starting"
	}
}

package org.gaseumlabs.uhc.core.phase.phases

import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.bukkit.World

class Postgame(game: Game) : Phase(PhaseType.POSTGAME, 0, game) {
	override fun endPhrase(): String {
		return ""
	}

	override fun updateBarLength(remainingTicks: Int): Float {
		return 1.0f
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent {
		return UHCComponent.text(phaseType.prettyName, phaseType.color)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		UHC.containSpecs()
	}
}

package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.phases.Phase

class EndgameNone : Phase() {

	override fun start(uhc: UHC, length: Long) {}

	override fun getCountdownString(): String {
		return ""
	}

	override fun getPhaseType(): UHCPhase {
		return UHCPhase.ENDGAME
	}

	override fun endPhrase(): String {
		return ""
	}
}
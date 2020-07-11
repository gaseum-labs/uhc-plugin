package com.codeland.uhc.phases.endgame

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase

class EndgameNone : Phase() {

	override fun customStart() {}
	override fun perSecond(remainingSeconds: Long) {
		TODO("Not yet implemented")
	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun endPhrase(): String {
		return ""
	}
}

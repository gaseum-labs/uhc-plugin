package com.codeland.uhc.phases.endgame

import com.codeland.uhc.phases.Phase

class EndgameNone : Phase() {

	override fun customStart() {}
	override fun perSecond(remainingSeconds: Int) {

	}

	override fun getCountdownString(): String {
		return ""
	}

	override fun endPhrase(): String {
		return ""
	}
}

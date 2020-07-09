package com.codeland.uhc.phases.final

import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase

class FinalDefault : Phase() {
	override fun getCountdownString(): String {
		return "Glowing starts in "
	}

	override fun endPhrase(): String {
		return "GLOWING WILL BE APPLIED"
	}

}
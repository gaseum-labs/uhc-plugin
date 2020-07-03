package com.codeland.uhc.phases.final

import com.codeland.uhc.phaseType.UHCPhase
import com.codeland.uhc.phases.Phase

class FinalDefault : Phase() {
	override fun getCountdownString(): String {
		return "Glowing starts in "
	}

	override fun getPhaseType(): UHCPhase {
		return UHCPhase.FINAL
	}

	override fun endPhrase(): String {
		return "GLOWING WILL BE APPLIED"
	}

}
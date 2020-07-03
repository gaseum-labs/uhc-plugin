package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.endgame.EndgameNone

enum class EndgameType {
	NONE;

	fun startPhase(uhc : UHC) : Phase {
		val ret = EndgameNone()
		ret.start(uhc, 0)
		return ret
	}
}
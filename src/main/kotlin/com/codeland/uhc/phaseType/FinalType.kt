package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.final.FinalDefault

enum class FinalType {
	DEFAULT;

	fun startPhase(uhc: UHC, length : Long) : Phase {
		val ret = FinalDefault()
		ret.start(uhc, length)
		return ret
	}
}
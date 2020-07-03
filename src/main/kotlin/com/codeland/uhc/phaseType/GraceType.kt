package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.grace.GraceDefault

enum class GraceType {
	DEFAULT;

	fun startPhase(uhc : UHC, length : Long) : Phase {
		val ret = GraceDefault()
		ret.start(uhc, length)
		return ret
	}
}
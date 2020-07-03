package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.shrink.ShrinkDefault

enum class ShrinkType {
	DEFAULT;

	fun startPhase(uhc: UHC, length : Long) : Phase {
		val ret = ShrinkDefault()
		ret.start(uhc, length)
		return ret
	}
}
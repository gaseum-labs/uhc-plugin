package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.glowing.GlowingDefault
import com.codeland.uhc.phases.glowing.GlowingTopTwo

enum class GlowType {
	DEFAULT,
	TOPTWO;

	fun startPhase(uhc : UHC, length : Long) : Phase {
		val ret : Phase =
		if (this == TOPTWO) {
			GlowingTopTwo()
		} else {
			GlowingDefault()
		}
		ret.start(uhc, length)
		return ret
	}
}
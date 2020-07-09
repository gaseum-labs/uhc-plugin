package com.codeland.uhc.phaseType

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.grace.GraceDefault
import com.codeland.uhc.event.Zatoichi
import com.destroystokyo.paper.utils.PaperPluginLogger
import java.util.logging.Level

enum class GraceType {
	DEFAULT;

	fun startPhase(uhc : UHC, length : Long) : Phase {
		PaperPluginLogger.getGlobal().log(Level.INFO, "starting grace phase: $this")

		val ret = GraceDefault()

		ret.start(uhc, length)
		return ret
	}
}
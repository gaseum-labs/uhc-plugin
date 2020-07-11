package com.codeland.uhc.phases.waiting

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase

class WaitingDefault : Phase() {

    override fun customStart() {
        TODO("Not yet implemented")
    }

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

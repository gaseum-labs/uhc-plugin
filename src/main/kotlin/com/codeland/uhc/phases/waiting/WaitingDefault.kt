package com.codeland.uhc.phases.waiting

import com.codeland.uhc.phases.Phase

class WaitingDefault : Phase() {

    override fun customStart() {

    }

    override fun perSecond(remainingSeconds: Long) {

    }

    override fun getCountdownString(): String {
        return ""
    }

    override fun endPhrase(): String {
        return ""
    }
}

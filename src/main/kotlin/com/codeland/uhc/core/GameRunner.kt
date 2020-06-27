package com.codeland.uhc.core

class GameRunner {
    private var uhc: UHC? = null

    var phase = UHCPhase.WAITING

    fun setUhc(uhc: UHC?) {
        this.uhc = uhc
    }
}
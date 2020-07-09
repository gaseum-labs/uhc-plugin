package com.codeland.uhc.phaseType

object Factories {
    var list = Array<ArrayList<PhaseFactory>>(PhaseType.values().size) {
        ArrayList()
    }
}
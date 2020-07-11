package com.codeland.uhc.phaseType

object Factories {
    var list = Array<ArrayList<PhaseVariant>>(PhaseType.values().size) {
        ArrayList()
    }
}
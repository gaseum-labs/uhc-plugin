package com.codeland.uhc.phaseType

object VariantList {
    var list = Array<ArrayList<PhaseVariant>>(PhaseType.values().size) {
        ArrayList()
    }
}
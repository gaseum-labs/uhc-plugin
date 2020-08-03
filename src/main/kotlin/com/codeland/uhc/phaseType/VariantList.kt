package com.codeland.uhc.phaseType

import com.codeland.uhc.core.Util

object VariantList {
    var list = emptyArray<ArrayList<PhaseVariant>>()

    fun create() {
        list = Array<ArrayList<PhaseVariant>>(PhaseType.values().size) {
            ArrayList()
        }

        PhaseVariant.values().forEach { variant ->
            list[variant.type.ordinal].add(variant)
        }
    }
}
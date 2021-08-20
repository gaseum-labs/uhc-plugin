package com.codeland.uhc.extensions

import kotlin.random.Random

object MiscExtensions {
    fun Random.Default.nextFloat(from: Float, until: Float): Float {
        return from + this.nextFloat() * (until - from)
    }

    fun Random.Default.nextFloat(until: Float): Float {
        return this.nextFloat(0f, until)
    }

    fun Random.Default.chance(chance: Double): Boolean {
        return this.nextFloat() < chance
    }
}

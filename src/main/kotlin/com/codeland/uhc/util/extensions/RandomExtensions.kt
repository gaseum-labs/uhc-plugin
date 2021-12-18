package com.codeland.uhc.util.extensions

import kotlin.random.Random

object RandomExtensions {
    fun Random.chance(probability: Double): Boolean {
        return nextDouble() < probability
    }

    fun Random.nextFloat(max: Float): Float {
        return nextFloat() * max
    }

    fun Random.nextFloat(min: Float, max: Float): Float {
        return min + nextFloat() * (max - min)
    }
}


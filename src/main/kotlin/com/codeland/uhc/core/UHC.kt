package com.codeland.uhc.core

class UHC(startRadius: Double, endRadius: Double, graceTime: Double, shrinkTime: Double) {

    private var startRadius = 0.0
    private var endRadius = 0.0
    private var graceTime = 0.0
    private var shrinkTime = 0.0

    fun setRadius(startRadius: Double, endRadius: Double) {
        this.startRadius = startRadius
        this.endRadius = endRadius
    }

    fun setGraceTime(graceTime: Double) {
        this.graceTime = graceTime
    }

    fun setShrinkTime(shrinkTime: Double) {
        this.shrinkTime = shrinkTime
    }

    init {
        setRadius(startRadius, endRadius)
        setGraceTime(graceTime)
        setShrinkTime(shrinkTime)
    }
}
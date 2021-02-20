package com.codeland.uhc.phase.phases.postgame

import com.codeland.uhc.phase.Phase
import org.bukkit.World

class PostgameDefault : Phase() {
    override fun endPhrase(): String {
        return ""
    }

    override fun customStart() {}

    override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
        return 1.0
    }

    override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
        return barStatic()
    }

    override fun perTick(currentTick: Int) {}

    override fun perSecond(remainingSeconds: Int) {
        uhc.containSpecs()
    }
}

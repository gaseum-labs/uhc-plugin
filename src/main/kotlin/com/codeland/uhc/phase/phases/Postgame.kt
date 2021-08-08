package com.codeland.uhc.phase.phases

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.phase.PhaseType
import org.bukkit.World

class Postgame(game: Game) : Phase(PhaseType.POSTGAME, 0, game) {
    override fun endPhrase(): String {
        return ""
    }

    override fun customStart() {}

    override fun updateBarLength(remainingTicks: Int): Float {
        return 1.0f
    }

    override fun updateBarTitle(world: World, remainingSeconds: Int): String {
        return barStatic()
    }

    override fun perTick(currentTick: Int) {}

    override fun perSecond(remainingSeconds: Int) {
        UHC.containSpecs()
    }
}

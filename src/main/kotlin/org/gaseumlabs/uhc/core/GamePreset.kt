package org.gaseumlabs.uhc.core

import org.bukkit.World

class GamePreset(
    val killReward: KillReward,
    val defaultWorldEnvironment: World.Environment,
    val scale: Float,
    val battlegroundRadius: Int,
    val graceTime: Int,
    val shrinkTime: Int,
    val battlegroundTime: Int,
    val collapseTime: Int,
) {
    companion object {
        fun defaultGamePreset() = GamePreset(
            KillReward.NONE,
            World.Environment.NORMAL,
            1.0f,
            72,
            1200,
            1200,
            1200,
            300,
        )
    }
}

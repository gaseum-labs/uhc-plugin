package com.codeland.uhc.phase.phases.postgame

import com.codeland.uhc.core.CustomSpawning
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.team.TeamData
import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.*
import kotlin.collections.ArrayList

class PostgameDefault : Phase() {
    override fun endPhrase(): String {
        return ""
    }

    override fun customStart() {}

    override fun customEnd() {}

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

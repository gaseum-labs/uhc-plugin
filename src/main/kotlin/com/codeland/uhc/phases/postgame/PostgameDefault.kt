package com.codeland.uhc.phases.postgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.GameRunner.discordBot
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseFactory
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phases.Phase
import com.destroystokyo.paper.Title
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Team

class PostgameDefault : Phase() {
    override fun getCountdownString(): String {
        TODO("Not yet implemented")
    }

    override fun endPhrase(): String {
        TODO("Not yet implemented")
    }

    var winningTeam = null as Team?

    override fun start(uhc: UHC, length: Long) {
        var team: Team = winningTeam ?: return

        Bukkit.getServer().onlinePlayers.forEach { player ->
            val winningTeamComp = TextComponent(team.displayName)
            winningTeamComp.isBold = true
            winningTeamComp.color = team.color.asBungee()
            val congratsComp = TextComponent("HAS WON!")

            player.sendTitle(Title(winningTeamComp, congratsComp, 0, 200, 40))
        }

        discordBot?.clearTeamVCs()
    }
}
package com.codeland.uhc.phases.postgame

import com.codeland.uhc.core.GameRunner
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

    override fun customStart() {
        var team: Team = winningTeam ?: return

        Bukkit.getServer().onlinePlayers.forEach { player ->
            val topMessage = TextComponent("${team.displayName} Has Won!")
            topMessage.isBold = true
            topMessage.color = team.color.asBungee()

            var playerString = ""
            team.entries.forEach { playerName ->
                playerString += "$playerName "
            }

            val bottomMessage = TextComponent(playerString.removeSuffix(" "))
            bottomMessage.color = team.color.asBungee()

            player.sendTitle(Title(topMessage, bottomMessage, 0, 200, 40))
        }

        GameRunner.discordBot?.clearTeamVCs()
    }

    override fun perSecond(remainingSeconds: Long) {
        TODO("Not yet implemented")
    }
}
package com.codeland.uhc.phases.postgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phases.Phase
import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.scoreboard.Team

class PostgameDefault : Phase() {

    override fun getCountdownString(): String {
        return ""
    }

    override fun endPhrase(): String {
        return ""
    }

    var winningTeam = null as Team?

    override fun customStart() {
        var team = winningTeam

        if (team != null) {
            val topMessage = TextComponent("${team.displayName} Has Won!")
            topMessage.isBold = true
            topMessage.color = team.color.asBungee()

            var playerString = ""
            team.entries.forEach { playerName ->
                val player = Bukkit.getPlayer(playerName)

                if (player != null && player.gameMode == GameMode.SURVIVAL) {
                    playerString += "$playerName "
                    uhc.ledger.addEntry(playerName, GameRunner.uhc.elapsedTime, null)
                }
            }

            uhc.ledger.createTextFile()

            val bottomMessage = TextComponent(playerString.removeSuffix(" "))
            bottomMessage.color = team.color.asBungee()

            val title = Title(topMessage, bottomMessage, 0, 200, 40)

            Bukkit.getServer().onlinePlayers.forEach { player ->
                player.sendTitle(title)
            }

        } else {
            val topMessage = TextComponent("No one wins?")
            topMessage.isBold = true
            topMessage.color = ChatColor.GOLD

            val title = Title(topMessage, TextComponent(""), 0, 200, 40)

            Bukkit.getServer().onlinePlayers.forEach { player ->
                player.sendTitle(title)
            }
        }

        /* stop all world borders */
        Bukkit.getWorlds().forEach { world ->
            world.worldBorder.size = world.worldBorder.size
        }

        val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

        scoreboard.teams.forEach { team ->
           if (uhc.usingBot) GameRunner.bot?.destroyTeam(team) {}
            team.unregister()
        }

        uhc.carePackages.onEnd()
    }

    override fun customEnd() {}

    override fun onTick(currentTick: Int) {}

    override fun perSecond(remainingSeconds: Int) {

    }
}

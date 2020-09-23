package com.codeland.uhc.phase.phases.postgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.boss.BossBar

class PostgameDefault : Phase() {
    override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
        barStatic(bossBar)
    }

    override fun endPhrase(): String {
        return ""
    }

    var winningTeam = null as Team?

    override fun customStart() {
        var winningTeam = winningTeam

        if (winningTeam != null) {
            val topMessage = TextComponent(winningTeam.colorPair.colorString("${winningTeam.displayName} Has Won!"))

            var playerString = ""
            winningTeam.members.forEach { member ->
                val player = member.player

                if (player != null && GameRunner.playerIsAlive(player)) {
                    playerString += "${player.name} "
                    uhc.ledger.addEntry(player.name, GameRunner.uhc.elapsedTime, "winning", true)
                }
            }
            playerString = winningTeam.colorPair.colorString(playerString.dropLast(1))

            val bottomMessage = TextComponent(playerString.removeSuffix(" "))
            val title = Title(topMessage, bottomMessage, 0, 200, 40)

            Bukkit.getServer().onlinePlayers.forEach { player ->
                player.sendTitle(title)
            }

            uhc.ledger.createTextFile()

        } else {
            val title = Title(TextComponent("${ChatColor.GOLD}${ChatColor.BOLD}No one wins?"), TextComponent(""), 0, 200, 40)

            Bukkit.getServer().onlinePlayers.forEach { player ->
                player.sendTitle(title)
            }
        }

        /* stop all world borders */
        Bukkit.getWorlds().forEach { world ->
            world.worldBorder.size = world.worldBorder.size
        }

        TeamData.removeAllTeams()
    }

    override fun customEnd() {}

    override fun onTick(currentTick: Int) {}

    override fun perSecond(remainingSeconds: Int) {}
}

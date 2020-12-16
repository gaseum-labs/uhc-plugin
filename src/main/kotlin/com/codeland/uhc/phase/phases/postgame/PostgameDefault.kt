package com.codeland.uhc.phase.phases.postgame

import com.codeland.uhc.core.CustomSpawning
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.boss.BossBar
import java.util.*
import kotlin.collections.ArrayList

class PostgameDefault : Phase() {
    override fun endPhrase(): String {
        return ""
    }

    lateinit var winners: ArrayList<UUID>

    override fun customStart() {
        if (winners.isNotEmpty()) {
            val winningTeam = TeamData.playersTeam(winners[0])

            val topMessage: TextComponent
            val bottomMessage: TextComponent

            if (winningTeam == null) {
                val winningPlayer = Bukkit.getPlayer(winners[0])

                topMessage = TextComponent("${ChatColor.GOLD}${ChatColor.BOLD}${winningPlayer?.name} Has Won!")
                bottomMessage = TextComponent()

                uhc.ledger.addEntry(winningPlayer?.name ?: "NULL", GameRunner.uhc.elapsedTime, "winning", true)

            } else {
                topMessage = TextComponent(winningTeam.colorPair.colorString("${winningTeam.displayName} Has Won!"))

                var playerString = ""
                winners.forEach { winner ->
                    val player = Bukkit.getPlayer(winner)

                    playerString += "${player?.name} "
                    uhc.ledger.addEntry(player?.name ?: "NULL", GameRunner.uhc.elapsedTime, "winning", true)
                }
                bottomMessage = TextComponent(winningTeam.colorPair.colorString(playerString.dropLast(1)))
            }

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

        /*set all non participating */
        uhc.playerDataList.forEach { (uuid, playerData) ->
            playerData.participating = false
        }

        /* stop all world borders */
        Bukkit.getWorlds().forEach { world ->
            world.worldBorder.size = world.worldBorder.size
        }

        TeamData.removeAllTeams { player ->
            GameRunner.uhc.setParticipating(player, false)
        }

        if (uhc.customSpawning) CustomSpawning.endTask()
    }

    override fun customEnd() {}

    override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
        return 1.0
    }

    override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
        return barStatic()
    }

    override fun perTick(currentTick: Int) {}

    override fun perSecond(remainingSeconds: Int) {}
}

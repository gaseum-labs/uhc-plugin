package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
//import com.codeland.uhc.di
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import org.kodein.di.instance
import java.util.logging.Level

@CommandAlias("uhc")
class CommandSetup : BaseCommand() {

    //private val gameRunner: GameRunner by di.instance()
    private val teamColours = arrayOf(ChatColor.BLUE, ChatColor.RED, ChatColor.GREEN, ChatColor.GOLD, ChatColor.LIGHT_PURPLE, ChatColor.AQUA, ChatColor.DARK_RED, ChatColor.GRAY, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE)

    @CommandAlias("setup")
    @Description("Setup the UHC round")
    fun onSetup(sender: CommandSender, startRadius: Double, endRadius: Double, shrinkTime: Double, graceTime: Double): Boolean {
        val uhc = UHC(startRadius, endRadius, shrinkTime, graceTime)

        //gameRunner.setUhc(uhc)

        PaperPluginLogger.getGlobal().log(Level.INFO, "SETUP COMMAND WAS SENT")
        return true
    }

    @CommandAlias("UHCClearTeams")
    @Description("remove all current teams")
    fun clearTeams(sender : CommandSender) {
        val scoreboard = sender.server.scoreboardManager.mainScoreboard
        scoreboard.teams.forEach {
            it.unregister()
        }
    }

    @CommandAlias("UHCCreateTeam")
    @Description("create a new team")
    fun createTeam(sender : CommandSender, teamName : String) {
        var team = sender.server.scoreboardManager.mainScoreboard.registerNewTeam(teamName)
        team.color = teamColours[sender.server.scoreboardManager.mainScoreboard.teams.size]
    }

    @CommandAlias("UHCAddToTeam")
    @Description("add a player to a team")
    fun addPlayerToTeam(sender : CommandSender, teamName : String, player : OfflinePlayer) {
        sender.server.scoreboardManager.mainScoreboard.getEntryTeam(player.name!!)?.removeEntry(player.name!!)
        sender.server.scoreboardManager.mainScoreboard.getTeam(teamName)?.addEntry(player.name!!)
    }

    @CommandAlias("UHCRandomTeams")
    @Description("create random teams")
    fun randomTeams(sender : CommandSender, teamSize : Int) {
        val onlinePlayers = sender.server.onlinePlayers
        val scoreboard = sender.server.scoreboardManager.mainScoreboard
        var playerArray = ArrayList<String>()
        onlinePlayers.forEach {
            if (scoreboard.getEntryTeam(it.name) == null) {
                playerArray.add(it.name)
            }
        }
        var teams = TeamMaker().getTeamsRandom(playerArray, teamSize)
        var numPreMadeTeams = scoreboard.teams.size
        teams.forEachIndexed { index, players ->
            val teamName = teamColours[numPreMadeTeams + index].name + " Team"
            createTeam(sender, teamName)
            players.forEach {
                if (it != null) {
                    scoreboard.getTeam(teamName)!!.addEntry(it)
                }
            }
        }
    }
}
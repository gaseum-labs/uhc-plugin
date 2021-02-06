package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.team.TeamData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ShareCoordsCommand : BaseCommand() {
    @CommandAlias("sharecoords")
    @Description("shares your coordinates with your teammates")
    fun shareCoords(sender : CommandSender) {
        sender as Player

        /* sharecoords command can only be used during the game */
        if (!GameRunner.uhc.isGameGoing())
            return Commands.errorMessage(sender, "Game is not running")

        /* sharecoords command can only be used when alive and playing */
        if (!GameRunner.uhc.isCurrent(sender.uniqueId))
            return Commands.errorMessage(sender, "You are no longer in the game")

        val team = TeamData.playersTeam(sender.uniqueId)
        val location = sender.location

        /* different message based on teams or no teams */
        if (team == null) {
            val message = "${GOLD}You are located at ${location.blockX}, ${location.blockY}, ${location.blockZ}"

           sender.sendMessage(message)

        } else {
            val message = team.colorPair.colorString("${sender.name} is located at ${location.blockX}, ${location.blockY}, ${location.blockZ}")

            /* send to all team members (including self) */
            team.members.forEach { uuid ->
                val player = Bukkit.getPlayer(uuid)
                player?.sendMessage(message)
            }
        }
    }
}

package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.team.TeamData
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ShareCoordsCommand : BaseCommand() {

    @CommandAlias("sharecoords")
    @Description("shares your coordinates with your teammates")
    fun shareCoords(sender : CommandSender) {
        sender as Player

        val team = TeamData.playersTeam(sender)

        if (team == null) {
            GameRunner.sendGameMessage(sender, "You must be on a team to use this command")

        } else {
            val l = sender.location
            val message = team.colorPair.colorString(sender.name) + " is located at " +
                    "${l.blockX}, " +
                    "${l.blockY}, " +
                    "${l.blockZ}"

            team.members.forEach { member ->
                member.player?.sendMessage(message)
            }
        }
    }

}
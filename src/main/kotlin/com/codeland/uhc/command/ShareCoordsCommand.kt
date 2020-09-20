package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ShareCoordsCommand : BaseCommand() {

    @CommandAlias("sharecoords")
    @Description("shares your coordinates with your teammates")
    fun shareCoords(sender : CommandSender) {
        if (sender !is Player) return
        val team = GameRunner.playersTeam(sender.name)
        if (team == null) {
            sender.sendMessage(GOLD.toString() + "You must be on a team to use this command.")
        } else {
            val l = sender.location
            val message = GameRunner.prettyPlayerName(sender.name) + " is located at " +
                    "${l.blockX}, " +
                    "${l.blockY}, " +
                    "${l.blockZ}"
            team.entries.map { Bukkit.getPlayer(it) }.forEach {
                it?.sendMessage(message)
            }
        }
    }

}
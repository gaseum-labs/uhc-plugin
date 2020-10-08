package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.Chat
import com.codeland.uhc.event.Coloring
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("nickname")
class NicknameCommand : BaseCommand() {

    fun validNickname(nickname: String): Boolean {
        val allowedCharacters = ('a'..'z').toList() + ('0'..'9').toList() + '_' + ' '
        return nickname.toLowerCase().all {
            it in allowedCharacters
        }
    }

    fun nicknameTaken(nickname: String): Boolean {
        return Chat.nickMap.values.any { it.any { it.equals(nickname, ignoreCase = true) } }
    }

    @CommandAlias("add")
    @Description("add a nickname that can be used in mentions")
    fun add(sender: CommandSender, string: String) {
        val player = if (sender is Player) sender else return
        if (!validNickname(string)) {
            Commands.errorMessage(player, "Usernames must be alphanumeric (including underscores).")
        } else if (nicknameTaken(string)) {
            Commands.errorMessage(player, "That nickname is already taken.")
        } else {
            Chat.addNick(player, string)
            GameRunner.sendGameMessage(player, "You added $string as a nickname.")
        }
    }

    @CommandAlias("remove")
    @Description("remove a previously added nickname")
    fun remove(sender: CommandSender, string: String) {
        val player = if (sender is Player) sender else return
        if (Chat.getNicks(player).any { it.equals(string, ignoreCase = true) }) {
            Chat.removeNick(player, string)
            GameRunner.sendGameMessage(player, "You removed $string as a nickname.")
        } else {
            Commands.errorMessage(player, "$string is not one of your nicknames. Run ${ChatColor.UNDERLINE}/nickname list$RED${ChatColor.BOLD} to view them.")
        }
    }

    @CommandAlias("removeall")
    @Description("remove all of your nicknames")
    fun removeAll(sender: CommandSender) {
        val player = if (sender is Player) sender else return
        Chat.getNicks(player).clear()
        GameRunner.sendGameMessage(player, "Removed all nicknames.")
    }

    @CommandAlias("list")
    @Description("list your current nicknames")
    fun list(sender: CommandSender) {
        val player = if (sender is Player) sender else return
        val coloring: Coloring = { Chat.underline(TeamData.playersColor(player)(it)) }
        if (Chat.getNicks(player).size == 0) {
            GameRunner.sendGameMessage(player, "You have no nicknames. Run ${ChatColor.UNDERLINE}/nickname add${ChatColor.GOLD}${ChatColor.BOLD} to add one!")
        } else {
            GameRunner.sendGameMessage(player, "Your nicknames:")
            Chat.getNicks(sender).forEach { nickname ->
                player.sendMessage("$GOLD- " + coloring("@$nickname"))
            }
        }
    }
}
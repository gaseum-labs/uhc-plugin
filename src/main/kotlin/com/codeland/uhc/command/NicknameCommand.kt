package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.Chat
import com.codeland.uhc.event.Coloring
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("nick")
class NicknameCommand : BaseCommand() {

    private fun validNickname(nickname: String): Boolean {
        val allowedCharacters = ('a'..'z').toList() + ('0'..'9').toList() + '_'
        return nickname.toLowerCase().all {
            it in allowedCharacters
        }
    }

    private fun nicknameTaken(nickname: String): Boolean {
        return Chat.nickMap.values.any { it.any { it.equals(nickname, ignoreCase = true) } }
    }

    @CommandAlias("add")
    @Description("add a nickname that can be used in mentions")
    fun add(sender: CommandSender, target: String, nickname: String) {
        val player = if (sender is Player) sender else return
        if (!validNickname(nickname)) {
            Commands.errorMessage(player, "Usernames must be alphanumeric (including underscores).")
        } else if (nicknameTaken(nickname)) {
            Commands.errorMessage(player, "That nickname is already taken by another player.")
        } else if (Bukkit.getPlayer(target) == null) {
            Commands.errorMessage(player, "The player ${Commands.coloredInError(target, GRAY)} doesn't exist or isn't online.")//
        } else {
            Chat.addNick(Bukkit.getPlayer(target)!!, nickname)
            GameRunner.sendGameMessage(player, "${GameRunner.coloredInGameMessage(target, GRAY)} can now be called ${GameRunner.coloredInGameMessage(nickname, GRAY)}.")
        }
    }

    @CommandAlias("remove")
    @Description("remove a previously added nickname")
    fun remove(sender: CommandSender, target: String, nickname: String) {
        val player = if (sender is Player) sender else return
        if (Bukkit.getPlayer(target) == null) {
            Commands.errorMessage(player, "The player ${Commands.coloredInError(target, GRAY)} doesn't exist or isn't online.")
            return
        }
        val targetPlayer = Bukkit.getPlayer(target)!!
        if (Chat.getNicks(targetPlayer).any { it.equals(nickname, ignoreCase = true) }) {//
            Chat.removeNick(targetPlayer, nickname)
            GameRunner.sendGameMessage(player, "${GameRunner.coloredInGameMessage(target, GRAY)} can no longer be called ${GameRunner.coloredInGameMessage(nickname, GRAY)}.")
        } else {
            Commands.errorMessage(player, "${Commands.coloredInError(nickname, GRAY)} is not one of ${Commands.coloredInError(target, GRAY)}'s nicknames.")
        }
    }

    @CommandAlias("list")
    @Description("list the nicknames of a player")
    fun list(sender: CommandSender, target: String) {
        val player = if (sender is Player) sender else return
        if (Bukkit.getPlayer(target) == null) {
            Commands.errorMessage(player, "The player ${Commands.coloredInError(target, GRAY)} doesn't exist or isn't online.")
            return
        }
        val targetPlayer = Bukkit.getPlayer(target)!!
        val coloring: Coloring = { Chat.underline(TeamData.playersColor(targetPlayer)(it)) }
        if (Chat.getNicks(targetPlayer).size == 0) {
            GameRunner.sendGameMessage(player, "This player has no nicknames.")
        } else {
            GameRunner.sendGameMessage(player, "Showing ${GameRunner.coloredInGameMessage(target, GRAY)}'s nicknames...")
            Chat.getNicks(targetPlayer).forEach { nickname ->
                player.sendMessage("$GOLD- " + coloring("@$nickname"))
            }
        }
    }
}
package com.codeland.uhc.command

import com.codeland.uhc.core.GameRunner
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Commands {
    fun errorMessage(sender: CommandSender, text: String) {
        sender.sendMessage("${ChatColor.RED}${ChatColor.BOLD}$text")
    }

    fun errorMessage(player: Player, text: String) {
        player.sendMessage("${ChatColor.RED}${ChatColor.BOLD}$text")
    }

    fun coloredInError(string: String, color: ChatColor): String {
        return "$color${ChatColor.BOLD}$string${ChatColor.RED}${ChatColor.BOLD}"
    }

    /**
     * returns if the sender cannot use this command
     * you should return from original function if true
     */
    fun opGuard(sender: CommandSender): Boolean {
        if (!sender.isOp) {
            errorMessage(sender, "You must be a server operator to use this command!")

            return true
        }

        return false
    }

    fun notGoingGuard(sender: CommandSender): Boolean {
        if (GameRunner.uhc.isGameGoing()) {
            errorMessage(sender, "This command cannot be used while the game is running")

            return true
        }

        return false
    }
}
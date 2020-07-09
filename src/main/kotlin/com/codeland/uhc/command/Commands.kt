package com.codeland.uhc.command

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object Commands {
    fun errorMessage(sender: CommandSender, text: String) {
        val message = TextComponent(text)
        message.color = ChatColor.RED.asBungee()
        message.isBold = true

        sender.sendMessage(message)
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

    fun waitGuard(sender: CommandSender): Boolean {
        if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) {
            errorMessage(sender, "This command cannot be used while the game is running")

            return true
        }

        return false
    }
}
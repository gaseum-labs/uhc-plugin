package com.codeland.uhc.command

import co.aikar.commands.PaperCommandManager
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.*
import org.bukkit.Bukkit
import org.bukkit.block.Biome
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object Commands {
	fun errorMessage(sender: CommandSender, text: String) {
		sender.sendMessage(Component.text(text, RED, BOLD))
	}

	fun errorMessage(player: Player, text: String) {
		player.sendMessage(Component.text(text, RED, BOLD))
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
		if (UHC.game != null) {
			errorMessage(sender, "This command cannot be used while the game is running")

			return true
		}

		return false
	}

	fun registerCompletions(commandManager: PaperCommandManager) {
		commandManager.commandCompletions.registerCompletion("uhcplayer") {
			PlayerData.playerDataList.map { (uuid, _) -> Bukkit.getOfflinePlayer(uuid).name }
		}

		commandManager.commandCompletions.registerCompletion("quirkclass") {
			QuirkClass.values().mapIndexedNotNull { i, quirkClass -> if (i == 0) null else quirkClass.name.lowercase() }
		}

		commandManager.commandCompletions.registerCompletion("biome") {
			Biome.values().map { it.name.lowercase() }
		}
	}
}
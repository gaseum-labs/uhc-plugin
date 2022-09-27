package org.gaseumlabs.uhc.command

import co.aikar.commands.PaperCommandManager
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.chc.chcs.classes.QuirkClass
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.*
import org.bukkit.Bukkit
import org.bukkit.block.Biome
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.world.regenresource.ResourceId

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

	fun registerCompletions(commandManager: PaperCommandManager) {
		commandManager.commandCompletions.registerCompletion("uhcplayer") {
			PlayerData.playerDataList.map { (uuid, _) -> Bukkit.getOfflinePlayer(uuid).name }
		}

		commandManager.commandCompletions.registerCompletion("uhcteamplayer") { context ->
			val team = UHC.getTeams().playersTeam(context.player.uniqueId)
				?: return@registerCompletion listOf(context.player.name)
			team.members.map { Bukkit.getOfflinePlayer(it).name }
		}

		commandManager.commandCompletions.registerCompletion("quirkclass") {
			QuirkClass.values().mapIndexedNotNull { i, quirkClass -> if (i == 0) null else quirkClass.name.lowercase() }
		}

		commandManager.commandCompletions.registerCompletion("biome") {
			Biome.values().map { it.name.lowercase() }
		}

		commandManager.commandCompletions.registerCompletion("uhcblockx") { context ->
			val block = context.player.getTargetBlock(10)
			if (block == null) emptyList() else listOf(block.x.toString())
		}

		commandManager.commandCompletions.registerCompletion("uhcblocky") { context ->
			val block = context.player.getTargetBlock(10)
			if (block == null) emptyList() else listOf(block.y.toString())
		}

		commandManager.commandCompletions.registerCompletion("uhcblockz") { context ->
			val block = context.player.getTargetBlock(10)
			if (block == null) emptyList() else listOf(block.z.toString())
		}

		commandManager.commandCompletions.registerCompletion("uhcregenresource") {
			ResourceId.allKeys()
		}
	}
}
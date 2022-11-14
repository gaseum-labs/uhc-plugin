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

	private const val YOU_MUST_BE = "You must be a server operator to use this command!"
	private const val CANT_BE_USED = "Command can't be used from the console"

	/**
	 * gates only admins from using command
	 * @return true if should immediately exit
	 */
	fun opGuard(sender: CommandSender): Boolean {
		if (!sender.isOp) {
			errorMessage(sender, YOU_MUST_BE)
			return true
		}

		return false
	}

	/**
	 * gates only admins who are also players (not console) from using command
	 * @return null if should immediately exit, please use the return value as the player
	 */
	fun opGuardPlayer(sender: CommandSender): Player? {
		if (sender !is Player) {
			errorMessage(sender, CANT_BE_USED)
			return null
		}

		if (!sender.isOp) {
			errorMessage(sender, YOU_MUST_BE)
			return null
		}

		return sender
	}

	/**
	 * gates only players (not console) from using command
	 * @return null if should immediately exit, please use the return value as the player
	 */
	fun playerGuard(sender: CommandSender): Player? {
		if (sender !is Player) {
			errorMessage(sender, CANT_BE_USED)
			return null
		}

		return sender
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
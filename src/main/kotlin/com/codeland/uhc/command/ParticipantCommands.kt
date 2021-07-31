package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.core.Lobby
import com.codeland.uhc.core.UHC
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.team.*
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uhc")
class ParticipantCommands : BaseCommand() {
	@Subcommand("gui")
	@Description("get the current setup as the gui")
	fun getCurrentSetupGui(sender: CommandSender) {
		UHC.setupGui.open(sender as Player)
	}

	@Subcommand("optOut")
	@Description("opt out from participating")
	fun optOutCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (!UHC.isPhase(PhaseType.WAITING)) {
			Commands.errorMessage(sender, "The game has already started!")

		} else if (playerData.optingOut) {
			Commands.errorMessage(sender, "You have already opted out!")

		} else {
			playerData.optingOut = true
			playerData.staged = false

			TeamData.removeFromTeam(arrayListOf(sender.uniqueId), UHC.usingBot.get(), true, true)

			GameRunner.sendGameMessage(sender, "You have opted out of participating")
		}
	}

	@Subcommand("optIn")
	@Description("opt back into participating")
	fun optInCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (!UHC.isPhase(PhaseType.WAITING)) {
			Commands.errorMessage(sender, "The game has already started!")

		} else if (!playerData.optingOut) {
			Commands.errorMessage(sender, "You already aren't opting out!")

		} else {
			playerData.optingOut = false

			GameRunner.sendGameMessage(sender, "You have opted back into participating")
		}
	}

	@Subcommand("compass")
	@Description("tell which direction a cave will be in based on the cave indicator block")
	fun compassCommand(sender: CommandSender) {
		sender as Player

		val block = sender.getTargetBlock(5)

		if (block == null) {
			Commands.errorMessage(sender, "You are not looking at a block")
		} else {
			GameRunner.sendGameMessage(sender, when (block.type) {
				Material.GRANITE -> "Granite indicates a cave to the north"
				Material.DIORITE -> "Diorite indicates a cave to the east"
				Material.ANDESITE -> "Andesite indicates a cave to the south"
				Material.TUFF -> "Tuff indicates a cave to the west"
				else -> "${ChatColor.RED}${ChatColor.BOLD}This block is not a cave indicator"
			})
		}
	}

	@Subcommand("lobby")
	@Description("allows spectators to go back to lobby")
	fun lobbyCommand(sender: CommandSender) {
		sender as Player

		/* only non players can use this command */
		if (PlayerData.isParticipating(sender.uniqueId) || ArenaManager.playersArena(sender.uniqueId) != null)
			return Commands.errorMessage(sender, "You can't use this command right now")

		/* specs can go back to lobby */
		Lobby.onSpawnLobby(sender)
	}

	@CommandCompletion("@quirkclass")
	@Subcommand("class")
	@Description("set your class for classes quirk")
	fun classCommand(sender: CommandSender, quirkClass: QuirkClass) {
		sender as Player

		if (!UHC.isEnabled(QuirkType.CLASSES)) return Commands.errorMessage(sender, "Classes are not enabled")

		if (!(UHC.isPhase(PhaseType.GRACE) || UHC.isPhase(PhaseType.WAITING)))
			return Commands.errorMessage(sender, "You can't change your class right now")

		if (UHC.isPhase(PhaseType.GRACE) && Classes.getClass(sender.uniqueId) != QuirkClass.NO_CLASS) {
			return Commands.errorMessage(sender, "You've already chosen a class")
		}

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(sender, "You must pick a class")

		val playerData = PlayerData.getPlayerData(sender.uniqueId)
		val oldClass = Classes.getClass(playerData)

		/* always set their class, even during waiting */
		Classes.setClass(sender.uniqueId, quirkClass)

		/* only start them if the game has already started */
		if (UHC.isGameGoing()) Classes.startAsClass(sender, quirkClass, oldClass)

		GameRunner.sendGameMessage(sender, "Set your class to ${quirkClass.prettyName}")
	}

	@Subcommand("rename")
	@Description("rename a remote control in classes chc")
	fun renameCommand(sender: CommandSender, name: String) {
		sender as Player

		if (!UHC.isEnabled(QuirkType.CLASSES)) return Commands.errorMessage(sender, "Classes are not enabled.")

		if (Classes.getClass(sender.uniqueId) != QuirkClass.TRAPPER) return Commands.errorMessage(sender, "Your class can't use this command.")

		val control = Classes.remoteControls.find { (item, _, _) ->
			item == sender.inventory.itemInMainHand }
				?: return Commands.errorMessage(sender, "You're not holding a remote control.")

		control.displayName = name
		sender.inventory.setItemInMainHand(Classes.updateRemoteControl(control))
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tp")
	@Description("teleport to a player as a spectator")
	fun tpHereCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		sender as Player

		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (!UHC.isPhase(PhaseType.WAITING) && !playerData.participating && sender.gameMode == GameMode.SPECTATOR) {
			val location = GameRunner.getPlayerLocation(toPlayer.uniqueId)

			if (location != null) {
				sender.teleport(location)
				GameRunner.sendGameMessage(sender, "Teleported to ${toPlayer.name}")
			} else {
				Commands.errorMessage(sender, "Could not find player ${toPlayer.name}")
			}
		} else {
			Commands.errorMessage(sender, "You cannot teleport right now")
		}
	}
}

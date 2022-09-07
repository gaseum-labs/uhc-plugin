package org.gaseumlabs.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.chc.chcs.classes.Classes
import org.gaseumlabs.uhc.chc.chcs.classes.QuirkClass
import org.gaseumlabs.uhc.util.Action
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.gui.gui.CreateGameGui
import org.gaseumlabs.uhc.gui.gui.QueueGUI
import org.gaseumlabs.uhc.lobbyPvp.Platform
import org.gaseumlabs.uhc.lobbyPvp.PlatformStorage
import org.gaseumlabs.uhc.lobbyPvp.arena.*
import org.gaseumlabs.uhc.util.BlockPos
import org.gaseumlabs.uhc.world.WorldManager

@CommandAlias("uhc")
class ParticipantCommands : BaseCommand() {
	@Subcommand("gui")
	@Description("get the current setup as the gui")
	fun getCurrentSetupGui(sender: CommandSender) {
		GuiManager.openGui(sender as Player, CreateGameGui())
	}

	@Subcommand("pvp")
	fun openPvp(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.get(sender)

		if (
			playerData.participating ||
			ArenaManager.playersArena(sender.uniqueId) is PvpArena
		) return Commands.errorMessage(sender, "You can't use this menu right now")

		GuiManager.openGui(sender, QueueGUI(playerData))
	}

	@Subcommand("optOut")
	@Description("opt out from participating")
	fun optOutCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.get(sender.uniqueId)

		if (playerData.participating) {
			Commands.errorMessage(sender, "You are already in the game!")

		} else if (playerData.optingOut) {
			Commands.errorMessage(sender, "You have already opted out!")

		} else {
			playerData.optingOut = true

			UHC.preGameTeams.leaveTeam(sender.uniqueId)

			Action.sendGameMessage(sender, "You have opted out of participating")
		}
	}

	@Subcommand("optIn")
	@Description("opt back into participating")
	fun optInCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.get(sender.uniqueId)

		if (playerData.participating) {
			Commands.errorMessage(sender, "You are already in the game!")

		} else if (!playerData.optingOut) {
			Commands.errorMessage(sender, "You already aren't opting out!")

		} else {
			playerData.optingOut = false

			Action.sendGameMessage(sender, "You have opted back into participating")
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
			val goodMessage = when (block.type) {
				Material.GRANITE -> "Granite indicates a cave to the north"
				Material.DIORITE -> "Diorite indicates a cave to the east"
				Material.ANDESITE -> "Andesite indicates a cave to the south"
				Material.TUFF -> "Tuff indicates a cave to the west"
				else -> null
			}

			if (goodMessage == null) {
				Commands.errorMessage(sender, "This block is not a cave indicator")
			} else {
				Action.sendGameMessage(sender, goodMessage)
			}
		}
	}

	@Subcommand("lobby")
	@Description("return to the lobby")
	fun lobbyCommand(sender: CommandSender) {
		sender as Player

		/* only non players can use this command */
		if (PlayerData.get(sender).participating)
			return Commands.errorMessage(sender, "You can't use this command in game")

		/* forfeit */
		val arena = ArenaManager.playersArena(sender.uniqueId)
		if (arena is PvpArena && arena.playerIsAlive(sender))
			Action.sendGameMessage(sender, "You have forfeited")

		Lobby.onSpawnLobby(sender)
	}

	@Subcommand("spectate")
	fun spectate(sender: CommandSender) {
		sender as Player

		if (PlayerData.get(sender.uniqueId).participating)
			return Commands.errorMessage(sender, "You can't use this command in game")

		val game = UHC.game

		if (game != null) {
			sender.gameMode = GameMode.SPECTATOR
			sender.setItemOnCursor(null)
			sender.inventory.clear()
			sender.teleport(game.spectatorSpawnLocation())

		} else {
			Commands.errorMessage(sender, "Game has not started!")
		}
	}

	@CommandCompletion("@quirkclass")
	@Subcommand("class")
	@Description("set your class for classes quirk")
	fun classCommand(sender: CommandSender, quirkClass: QuirkClass) {
		sender as Player

		val game = UHC.game ?: return Commands.errorMessage(sender, "Game has not started")

		val classes = game.chc as? Classes ?: return Commands.errorMessage(sender, "Classes are not enabled")

		if (classes.getClass(sender.uniqueId) != QuirkClass.NO_CLASS) {
			return Commands.errorMessage(sender, "You've already chosen a class")
		}

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(sender, "You must pick a class")

		val playerData = PlayerData.get(sender.uniqueId)
		val oldClass = classes.getClass(playerData)

		/* always set their class, even during waiting */
		classes.setClass(sender.uniqueId, quirkClass)

		/* only start them if the game has already started */
		Classes.startAsClass(sender, quirkClass, oldClass)

		Action.sendGameMessage(sender, "Set your class to ${quirkClass.prettyName}")
	}

	@Subcommand("rename")
	@Description("rename a remote control in classes chc")
	fun renameCommand(sender: CommandSender, name: String) {
		sender as Player

		val game = UHC.game ?: return Commands.errorMessage(sender, "Game has not started")

		val classes = game.chc as? Classes ?: return Commands.errorMessage(sender, "Classes are not enabled")

		if (classes.getClass(sender.uniqueId) != QuirkClass.ENGINEER) return Commands.errorMessage(sender,
			"Your class can't use this command.")

		val control = Classes.remoteControls.find { (item, _, _) ->
			item == sender.inventory.itemInMainHand
		}
			?: return Commands.errorMessage(sender, "You're not holding a remote control.")

		control.displayName = name
		sender.inventory.setItemInMainHand(Classes.updateRemoteControl(control))
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tp")
	@Description("teleport to a player as a spectator")
	fun tpHereCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		sender as Player

		val playerData = PlayerData.get(sender.uniqueId)

		if (UHC.game != null && !playerData.participating && sender.gameMode == GameMode.SPECTATOR) {
			val location = Action.getPlayerLocation(toPlayer.uniqueId)

			if (location != null) {
				sender.teleport(location)
				Action.sendGameMessage(sender, "Teleported to ${toPlayer.name}")
			} else {
				Commands.errorMessage(sender, "Could not find player ${toPlayer.name}")
			}
		} else {
			Commands.errorMessage(sender, "You cannot teleport right now")
		}
	}
}

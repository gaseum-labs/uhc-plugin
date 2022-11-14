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
		val player = Commands.playerGuard(sender) ?: return
		GuiManager.openGui(player, CreateGameGui())
	}

	@Subcommand("pvp")
	fun openPvp(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return
		val playerData = PlayerData.get(player)

		if (
			playerData.participating ||
			ArenaManager.playersArena(player.uniqueId) is PvpArena
		) return Commands.errorMessage(player, "You can't use this menu right now")

		GuiManager.openGui(player, QueueGUI(playerData))
	}

	@Subcommand("optOut")
	@Description("opt out from participating")
	fun optOutCommand(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return
		val playerData = PlayerData.get(player.uniqueId)

		if (playerData.participating) {
			Commands.errorMessage(player, "You are already in the game!")

		} else if (playerData.optingOut) {
			Commands.errorMessage(player, "You have already opted out!")

		} else {
			playerData.optingOut = true

			UHC.preGameTeams.leaveTeam(player.uniqueId)

			Action.sendGameMessage(player, "You have opted out of participating")
		}
	}

	@Subcommand("optIn")
	@Description("opt back into participating")
	fun optInCommand(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return
		val playerData = PlayerData.get(player.uniqueId)

		if (playerData.participating) {
			Commands.errorMessage(player, "You are already in the game!")

		} else if (!playerData.optingOut) {
			Commands.errorMessage(player, "You already aren't opting out!")

		} else {
			playerData.optingOut = false

			Action.sendGameMessage(player, "You have opted back into participating")
		}
	}

	@Subcommand("compass")
	@Description("tell which direction a cave will be in based on the cave indicator block")
	fun compassCommand(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return

		val block = player.getTargetBlock(5)

		if (block == null) {
			Commands.errorMessage(player, "You are not looking at a block")
		} else {
			val goodMessage = when (block.type) {
				Material.GRANITE -> "Granite indicates a cave to the north"
				Material.DIORITE -> "Diorite indicates a cave to the east"
				Material.ANDESITE -> "Andesite indicates a cave to the south"
				Material.TUFF -> "Tuff indicates a cave to the west"
				else -> null
			}

			if (goodMessage == null) {
				Commands.errorMessage(player, "This block is not a cave indicator")
			} else {
				Action.sendGameMessage(player, goodMessage)
			}
		}
	}

	@Subcommand("lobby")
	@Description("return to the lobby")
	fun lobbyCommand(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return

		/* only non players can use this command */
		if (PlayerData.get(player).participating)
			return Commands.errorMessage(player, "You can't use this command in game")

		/* forfeit */
		val arena = ArenaManager.playersArena(player.uniqueId)
		if (arena is PvpArena && arena.playerIsAlive(player))
			Action.sendGameMessage(player, "You have forfeited")

		Lobby.onSpawnLobby(player)
	}

	@Subcommand("spectate")
	fun spectate(sender: CommandSender) {
		val player = Commands.playerGuard(sender) ?: return

		if (PlayerData.get(player.uniqueId).participating)
			return Commands.errorMessage(player, "You can't use this command in game")

		val game = UHC.game

		if (game != null) {
			player.gameMode = GameMode.SPECTATOR
			player.setItemOnCursor(null)
			player.inventory.clear()
			player.teleport(game.spectatorSpawnLocation())

		} else {
			Commands.errorMessage(player, "Game has not started!")
		}
	}

	@CommandCompletion("@quirkclass")
	@Subcommand("class")
	@Description("set your class for classes quirk")
	fun classCommand(sender: CommandSender, quirkClass: QuirkClass) {
		val player = Commands.playerGuard(sender) ?: return

		val game = UHC.game ?: return Commands.errorMessage(player, "Game has not started")

		val classes = game.chc as? Classes ?: return Commands.errorMessage(player, "Classes are not enabled")

		if (classes.getClass(player.uniqueId) != QuirkClass.NO_CLASS) {
			return Commands.errorMessage(player, "You've already chosen a class")
		}

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(player, "You must pick a class")

		val playerData = PlayerData.get(player.uniqueId)
		val oldClass = classes.getClass(playerData)

		/* always set their class, even during waiting */
		classes.setClass(player.uniqueId, quirkClass)

		/* only start them if the game has already started */
		Classes.startAsClass(player, quirkClass, oldClass)

		Action.sendGameMessage(player, "Set your class to ${quirkClass.prettyName}")
	}

	@Subcommand("rename")
	@Description("rename a remote control in classes chc")
	fun renameCommand(sender: CommandSender, name: String) {
		val player = Commands.playerGuard(sender) ?: return

		val game = UHC.game ?: return Commands.errorMessage(player, "Game has not started")

		val classes = game.chc as? Classes ?: return Commands.errorMessage(player, "Classes are not enabled")

		if (classes.getClass(player.uniqueId) != QuirkClass.ENGINEER) return Commands.errorMessage(player,
			"Your class can't use this command.")

		val control = Classes.remoteControls.find { (item, _, _) ->
			item == player.inventory.itemInMainHand
		}
			?: return Commands.errorMessage(player, "You're not holding a remote control.")

		control.displayName = name
		player.inventory.setItemInMainHand(Classes.updateRemoteControl(control))
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("tp")
	@Description("teleport to a player as a spectator")
	fun tpHereCommand(sender: CommandSender, toPlayer: OfflinePlayer) {
		val player = Commands.playerGuard(sender) ?: return

		val playerData = PlayerData.get(player.uniqueId)

		if (UHC.game != null && !playerData.participating && player.gameMode == GameMode.SPECTATOR) {
			val location = Action.getPlayerLocation(toPlayer.uniqueId)

			if (location != null) {
				player.teleport(location)
				Action.sendGameMessage(player, "Teleported to ${toPlayer.name}")
			} else {
				Commands.errorMessage(player, "Could not find player ${toPlayer.name}")
			}
		} else {
			Commands.errorMessage(player, "You cannot teleport right now")
		}
	}
}

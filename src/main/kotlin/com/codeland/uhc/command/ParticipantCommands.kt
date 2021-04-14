package com.codeland.uhc.command;

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.core.AbstractLobby
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.team.*
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("uhc")
class ParticipantCommands : BaseCommand() {
	@Subcommand("gui")
	@Description("get the current setup as the gui")
	fun getCurrentSetupGui(sender: CommandSender) {
		GameRunner.uhc.gui.inventory.open(sender as Player)
	}

	@Subcommand("optOut")
	@Description("opt out from participating")
	fun optOutCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			Commands.errorMessage(sender, "The game has already started!")

		} else if (playerData.optingOut) {
			Commands.errorMessage(sender, "You have already opted out!")

		} else {
			playerData.optingOut = true
			playerData.staged = false

			val team = TeamData.playersTeam(sender.uniqueId)
			if (team != null) TeamData.removeFromTeam(team, sender.uniqueId, true)

			GameRunner.sendGameMessage(sender, "You have opted out of participating")
		}
	}

	@Subcommand("optIn")
	@Description("opt back into participating")
	fun optInCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		if (!GameRunner.uhc.isPhase(PhaseType.WAITING)) {
			Commands.errorMessage(sender, "The game has already started!")

		} else if (!playerData.optingOut) {
			Commands.errorMessage(sender, "You already aren't opting out!")

		} else {
			playerData.optingOut = false

			GameRunner.sendGameMessage(sender, "You have opted back into participating")
		}
	}

	@Subcommand("name")
	@Description("change the name of your team")
	fun teamName(sender: CommandSender, newName: String) {
		val team = TeamData.playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		var realNewName = newName
		if (realNewName.startsWith('"') && realNewName.endsWith('"') && realNewName.length > 1) {
			realNewName = realNewName.substring(1, realNewName.length - 1)
		}

		team.displayName = realNewName

		/* broadcast change to all teammates */
		team.members.forEach { uuid ->
			val player = Bukkit.getPlayer(uuid)
			player?.sendMessage("Your team name has been changed to \"${team.colorPair.colorString(realNewName)}\"")
		}
	}

	@CommandCompletion("@teamcolor")
	@Subcommand("color")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color0: ChatColor) {
		changeTeamColor(sender, color0, null)
	}

	@CommandCompletion("@teamcolor @teamcolor")
	@Subcommand("color")
	@Description("change your team color")
	fun teamColor(sender: CommandSender, color0: ChatColor, color1: ChatColor) {
		changeTeamColor(sender, color0, color1)
	}

	@Subcommand("color random")
	@Description("change your team color")
	fun teamColor(sender: CommandSender) {
		val colors = TeamMaker.randomAvailable(1)

		if (colors == null)
			Commands.errorMessage(sender, "Could not make you a new random color")
		else
			changeTeamColor(sender, colors[0].color0, colors[0].color1)
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
				Material.DIRT -> "Dirt indicates a cave to the west"
				else -> "${ChatColor.RED}${ChatColor.BOLD}This block is not a cave indicator"
			})
		}
	}

	@Subcommand("lobby")
	@Description("allows spectators to go back to lobby")
	fun lobbyCommand(sender: CommandSender) {
		sender as Player
		val playerData = PlayerData.getPlayerData(sender.uniqueId)

		/* only non players can use this command */
		if (playerData.participating) return Commands.errorMessage(sender, "You're playing the game")

		/* lobby pvpers have to wait */
		if (playerData.lobbyPVP.inPvp) {
			playerData.lobbyPVP.exiting = true
			GameRunner.sendGameMessage(sender, "Stand still to return to lobby")

		/* specs immediately go to lobby */
		} else {
			AbstractLobby.onSpawnLobby(sender)
		}
	}

	private fun changeTeamColor(sender: CommandSender, color0: ChatColor, color1: ChatColor?) {
		val team = TeamData.playersTeam((sender as Player).uniqueId)
			?: return Commands.errorMessage(sender, "You are not on a team!")

		fun colorError(color: ChatColor) =
			Commands.errorMessage(sender, "${color}${Util.colorPrettyNames[color.ordinal]} ${ChatColor.RESET}is not a valid team color!")

		if (!Team.isValidColor(color0)) return colorError(color0)

		if (color1 != null && !Team.isValidColor(color1)) return colorError(color1)

		/* parse what the color the user wants to change to */
		val colorPair = ColorPair(color0, color1)

		if (!colorPair.valid()) return Commands.errorMessage(sender, "Invalid color combination!")

		if (colorPair.orderEquals(team.colorPair)) return Commands.errorMessage(sender, "This is already your color!")

		if (TeamData.teamExists(colorPair, team))
			return Commands.errorMessage(sender, "That color combination is already being used by another team!")

		/* change team name to be default name for new color if no custom name has been set */
		if (team.isDefaultName()) team.displayName = colorPair.getName()
		val oldColorPair = team.colorPair
		team.colorPair = colorPair

		/* update team channel name */
		if (GameRunner.uhc.usingBot) {
			GameRunner.bot?.updateTeamChannel(oldColorPair, colorPair)
		}

		val message = "Your team color has been changed to ${colorPair.colorString(colorPair.getName())}"

		/* broadcast change to all teammates */
		team.members.forEach { uuid ->
			val player = Bukkit.getPlayer(uuid)

			if (player != null) {
				GameRunner.sendGameMessage(player, message)
				NameManager.updateName(player)
			}
		}
	}

	@CommandCompletion("@quirkclass")
	@Subcommand("class")
	@Description("set your class for classes quirk")
	fun classCommand(sender: CommandSender, quirkClass: QuirkClass) {
		sender as Player

		if (!GameRunner.uhc.isEnabled(QuirkType.CLASSES)) return Commands.errorMessage(sender, "Classes are not enabled")

		if (!(GameRunner.uhc.isPhase(PhaseType.GRACE) || GameRunner.uhc.isPhase(PhaseType.WAITING)))
			return Commands.errorMessage(sender, "You can't change your class right now")

		if (GameRunner.uhc.isPhase(PhaseType.GRACE) && Classes.getClass(sender.uniqueId) != QuirkClass.NO_CLASS) {
			return Commands.errorMessage(sender, "You've already chosen a class")
		}

		if (quirkClass == QuirkClass.NO_CLASS) return Commands.errorMessage(sender, "You must pick a class")

		val playerData = PlayerData.getPlayerData(sender.uniqueId)
		val oldClass = Classes.getClass(playerData)

		/* always set their class, even during waiting */
		Classes.setClass(sender.uniqueId, quirkClass)

		/* only start them if the game has already started */
		if (GameRunner.uhc.isGameGoing()) Classes.startAsClass(sender, quirkClass, oldClass)

		GameRunner.sendGameMessage(sender, "Set your class to ${quirkClass.prettyName}")
	}

	@Subcommand("rename")
	@Description("rename a remote control in classes chc")
	fun renameCommand(sender: CommandSender, name: String) {
		sender as Player

		if (!GameRunner.uhc.isEnabled(QuirkType.CLASSES)) return Commands.errorMessage(sender, "Classes are not enabled.")

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

		if (!GameRunner.uhc.isPhase(PhaseType.WAITING) && !playerData.participating && sender.gameMode == GameMode.SPECTATOR) {
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

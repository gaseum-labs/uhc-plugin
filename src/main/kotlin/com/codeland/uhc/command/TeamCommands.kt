package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.team.ColorPair
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.team.TeamMaker
import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*
import kotlin.collections.ArrayList

@CommandAlias("uhca")
@Subcommand("team")
class TeamCommands : BaseCommand() {
	@Subcommand("clear")
	@Description("remove all current teams")
	fun clearTeams(sender : CommandSender) {
		if (Commands.opGuard(sender)) return
		if (Commands.notGoingGuard(sender)) return //TODO make team commands available to do during the game

		/* unstage everyone and remove teams */
		TeamData.removeAllTeams { uuid -> PlayerData.setStaged(uuid, false) }

		GameRunner.sendGameMessage(sender, "Cleared all teams")
	}

	@CommandCompletion("@teamcolor @uhcplayer")
	@Subcommand("add")
	@Description("add a player to a team")
	fun addPlayerToTeamCommand(sender: CommandSender, color: ChatColor, player: OfflinePlayer) {
		if (!Team.isValidColor(color)) return Commands.errorMessage(sender, "${Util.colorPrettyNames[color.ordinal]} is not a valid color")

		internalAddPlayerToTeam(sender, ColorPair(color), player)
	}

	@CommandCompletion("@teamcolor @teamcolor @uhcplayer")
	@Subcommand("add2")
	@Description("add a player to a team with two colors")
	fun addPlayerToTeamCommand(sender: CommandSender, color0: ChatColor, color1: ChatColor, player: OfflinePlayer) {
		if (!Team.isValidColor(color0)) return Commands.errorMessage(sender, "${Util.colorPrettyNames[color0.ordinal]} is not a valid color")
		if (!Team.isValidColor(color1)) return Commands.errorMessage(sender, "${Util.colorPrettyNames[color1.ordinal]} is not a valid color")

		internalAddPlayerToTeam(sender, ColorPair(color0, color1), player)
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("join")
	@Description("add a player to another player's team")
	fun addPlayerToTeamCommand(sender: CommandSender, teamPlayer: OfflinePlayer, player: OfflinePlayer) {
		val team = TeamData.playersTeam(teamPlayer.uniqueId)
			?: return Commands.errorMessage(sender, "${teamPlayer.name} is not on a team!")

		internalAddPlayerToTeam(sender, team.colorPair, player)
	}

	private fun internalAddPlayerToTeam(sender: CommandSender, colorPair: ColorPair, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		if (playerData.optingOut)
			return Commands.errorMessage(sender, "${player.name} is opting out of participating!")

		/* stage player and add them to team */
		val team = TeamData.addToTeam(colorPair, player.uniqueId, true)
		playerData.staged = true

		GameRunner.sendGameMessage(sender, "Added ${player.name} to team ${colorPair.colorString(team.displayName)}")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("remove")
	@Description("remove a player from a team")
	fun removePlayerFromTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val team = TeamData.playersTeam(player.uniqueId)
			?: return Commands.errorMessage(sender, "${player.name} is not on a team!")

		/* unstage and remove player from team */
		TeamData.removeFromTeam(team, player.uniqueId, true)
		PlayerData.setStaged(player.uniqueId, false)

		GameRunner.sendGameMessage(sender, "Removed ${player.name} from ${team.colorPair.colorString(team.displayName)}")
	}

	@Subcommand("random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		if (Commands.opGuard(sender)) return

		val onlinePlayers = sender.server.onlinePlayers
		val playerArray = ArrayList<UUID>(onlinePlayers.size)

		onlinePlayers.forEach { player ->
			if (TeamData.playersTeam(player.uniqueId) == null && !PlayerData.isOptingOut(player.uniqueId))
				playerArray.add(player.uniqueId)
		}

		val teams = TeamMaker.getTeamsRandom(playerArray, teamSize)
		val numPreMadeTeams = teams.size

		val teamColorPairs = TeamMaker.getColorList(numPreMadeTeams)
			?: return Commands.errorMessage(sender, "Team Maker could not make enough teams!")


		teams.forEachIndexed { index, uuids ->
			uuids.forEach { uuid ->
				if (uuid != null) {
					TeamData.addToTeam(teamColorPairs[index], uuid, true)
					PlayerData.setStaged(uuid, true)
				}
			}
		}

		GameRunner.sendGameMessage(sender, "Created ${teams.size} teams with a team size of ${teamSize}!")
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("swap")
	@Description("swap the teams of two players")
	fun swapTeams(sender: CommandSender, player1: OfflinePlayer, player2: OfflinePlayer) {
		val team1 = TeamData.playersTeam(player1.uniqueId) ?: return Commands.errorMessage(sender, "${player1.name} is not on a team!")
		val team2 = TeamData.playersTeam(player2.uniqueId) ?: return Commands.errorMessage(sender, "${player2.name} is not on a team!")

		TeamData.addToTeam(team2, player1.uniqueId, false)
		TeamData.addToTeam(team1, player2.uniqueId, false)

		GameRunner.sendGameMessage(sender, "${team2.colorPair.colorString(player1.name ?: "unknown")} ${ChatColor.GOLD}${ChatColor.BOLD}and ${team1.colorPair.colorString(player2.name ?: "unknown")} ${ChatColor.GOLD}${ChatColor.BOLD}sucessfully swapped teams!")
	}
}

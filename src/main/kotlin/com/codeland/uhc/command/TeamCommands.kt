package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.team.ColorPair
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.team.TeamMaker
import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList

@CommandAlias("uhca")
class TeamCommands : BaseCommand() {
	@CommandAlias("team clear")
	@Description("remove all current teams")
	fun clearTeams(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		TeamData.removeAllTeams( { player ->
			GameRunner.uhc.setParticipating(player, false)
		}) {
			GameRunner.sendGameMessage(sender, "Cleared all teams")
		}
	}

	@CommandAlias("team add")
	@Description("add a player to a team")
	fun addPlayerToTeamCommand(sender: CommandSender, color: ChatColor, player: OfflinePlayer) {
		if (!Team.isValidColor(color)) return Commands.errorMessage(sender, "${Util.colorPrettyNames[color.ordinal]} is not a valid color")

		internalAddPlayerToTeam(sender, ColorPair(color), player)
	}

	@CommandAlias("team add")
	@Description("add a player to a team")
	fun addPlayerToTeamCommand(sender: CommandSender, color0: ChatColor, color1: ChatColor, player: OfflinePlayer) {
		if (!Team.isValidColor(color0)) return Commands.errorMessage(sender, "${Util.colorPrettyNames[color0.ordinal]} is not a valid color")
		if (!Team.isValidColor(color1)) return Commands.errorMessage(sender, "${Util.colorPrettyNames[color1.ordinal]} is not a valid color")

		internalAddPlayerToTeam(sender, ColorPair(color0, color1), player)
	}

	@CommandAlias("team join")
	@Description("add a player to a team")
	fun addPlayerToTeamCommand(sender: CommandSender, teamPlayer: OfflinePlayer, player: OfflinePlayer) {
		val team = TeamData.playersTeam(teamPlayer.uniqueId)
			?: return Commands.errorMessage(sender, "${teamPlayer.name} is not on a team!")

		internalAddPlayerToTeam(sender, team.colorPair, player)
	}

	private fun internalAddPlayerToTeam(sender: CommandSender, colorPair: ColorPair, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		if (GameRunner.uhc.isOptingOut(player.uniqueId))
			return Commands.errorMessage(sender, "${player.name} is opting out of participating!")

		TeamData.addToTeam(colorPair, player.uniqueId, true) { team ->
			GameRunner.uhc.setParticipating(player.uniqueId, true)
			GameRunner.sendGameMessage(sender, "Added ${player.name} to team ${colorPair.colorString(team.displayName)}")
		}
	}

	@CommandAlias("team remove")
	@Description("remove a player from a team")
	fun removePlayerFromTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val team = TeamData.playersTeam(player.uniqueId)
			?: return Commands.errorMessage(sender, "${player.name} is not on a team!")

		TeamData.removeFromTeam(team, player.uniqueId, true) {
			GameRunner.uhc.setParticipating(player.uniqueId, false)
			GameRunner.sendGameMessage(sender, "Removed ${player.name} from ${team.colorPair.colorString(team.displayName)}")
		}
	}

	@CommandAlias("team random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		if (Commands.opGuard(sender)) return

		val onlinePlayers = sender.server.onlinePlayers
		val playerArray = ArrayList<UUID>(onlinePlayers.size)

		onlinePlayers.forEach { player ->
			if (TeamData.playersTeam(player.uniqueId) == null && !GameRunner.uhc.isOptingOut(player.uniqueId))
				playerArray.add(player.uniqueId)
		}

		val teams = TeamMaker.getTeamsRandom(playerArray, teamSize)
		val numPreMadeTeams = teams.size

		val teamColorPairs = TeamMaker.getColorList(numPreMadeTeams)
			?: return Commands.errorMessage(sender, "Team Maker could not make enough teams!")

		val lock = ReentrantLock()

		teams.forEachIndexed { index, uuids ->
			uuids.forEach { uuid ->
				if (uuid != null) {
					lock.lock()
					TeamData.addToTeam(teamColorPairs[index], uuid, true) {
						GameRunner.uhc.setParticipating(uuid, true)
						lock.unlock()
					}
				}
			}
		}

		GameRunner.sendGameMessage(sender, "Created ${teams.size} teams with a team size of ${teamSize}!")
	}

	@CommandAlias("team swap")
	@Description("swap the teams of two players")
	fun swapTeams(sender: CommandSender, player1: OfflinePlayer, player2: OfflinePlayer) {
		val team1 = TeamData.playersTeam(player1.uniqueId) ?: return Commands.errorMessage(sender, "${player1.name} is not on a team!")
		val team2 = TeamData.playersTeam(player2.uniqueId) ?: return Commands.errorMessage(sender, "${player2.name} is not on a team!")

		TeamData.addToTeam(team2, player1.uniqueId, false) {}
		TeamData.addToTeam(team1, player2.uniqueId, false) {}

		GameRunner.sendGameMessage(sender, "${team2.colorPair.colorString(player1.name ?: "unknown")} ${ChatColor.GOLD}${ChatColor.BOLD}and ${team1.colorPair.colorString(player2.name ?: "unknown")} ${ChatColor.GOLD}${ChatColor.BOLD}sucessfully swapped teams!")
	}
}

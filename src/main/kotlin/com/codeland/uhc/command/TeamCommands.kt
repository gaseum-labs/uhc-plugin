package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.util.Action
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.team.AbstractTeam
import com.codeland.uhc.team.PreTeam
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.Teams
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*
import kotlin.collections.ArrayList

@CommandAlias("uhca")
@Subcommand("team")
class TeamCommands : BaseCommand() {
	@Subcommand("clear")
	@Description("remove all current teams")
	fun clearTeamsCommand(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		UHC.getTeams().clearTeams()

		Action.sendGameMessage(sender, "Cleared all teams")
	}

	private fun teamPlayerList(sender: CommandSender, players: List<OfflinePlayer>): List<OfflinePlayer>? {
		return players.filter { player ->
			if (PlayerData.isOptingOut(player.uniqueId)) {
				Commands.errorMessage(sender, "${player.name} is opting out of participating")
				false
			} else {
				true
			}
		}.ifEmpty {
			Commands.errorMessage(sender, "No valid players selected")
			null
		}
	}

	private fun internalCreateTeam(sender: CommandSender, players: List<OfflinePlayer>) {
		if (Commands.opGuard(sender)) return

		val teamPlayerList = teamPlayerList(sender, players) ?: return
		val uuids = teamPlayerList.map { it.uniqueId } as ArrayList<UUID>

		val (color0, color1) = UHC.colorCube.pickTeam() ?:
		return Action.sendGameMessage(sender, "Not enough colors available to create a team")

		val game = UHC.game
		if (game == null) {
			UHC.preGameTeams.addTeam(PreTeam(color0, color1, uuids))

		} else {
			game.teams.addTeam(Team(PreTeam.randomName(), color0, color1, uuids))
		}

		Action.sendGameMessage(sender, "Created a team for ${teamPlayerList.joinToString(" and ") { it.name ?: "Unknown" }}")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("create")
	@Description("create a new team for a player")
	fun createTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		internalCreateTeam(sender, listOf(player))
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("create")
	@Description("create a new team comprised of 2 players")
	fun createTeamCommand(sender: CommandSender, player0: OfflinePlayer, player1: OfflinePlayer) {
		internalCreateTeam(sender, listOf(player0, player1))
	}

	@CommandCompletion("@uhcplayer @uhcplayer @uhcplayer")
	@Subcommand("create")
	@Description("create a new team comprised of 3 players")
	fun createTeamCommand(sender: CommandSender, player0: OfflinePlayer, player1: OfflinePlayer, player2: OfflinePlayer) {
		internalCreateTeam(sender, listOf(player0, player1, player2))
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("join")
	@Description("add the second player to the first player's team")
	fun addPlayerToTeamCommand(sender: CommandSender, teamPlayer: OfflinePlayer, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val teams = UHC.getTeams()
		val team = teams.playersTeam(teamPlayer.uniqueId)
			?: return Commands.errorMessage(sender, "${teamPlayer.name} is not on a team")

		val addedPlayer = (teamPlayerList(sender, listOf(player)) ?: return).first()

		teams.joinTeam(addedPlayer.uniqueId, team)

		Action.sendGameMessage(sender, "Added ${addedPlayer.name} to ${teamPlayer.name}'s team")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("remove")
	@Description("remove a player from a team")
	fun removePlayerFromTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		if (UHC.getTeams().leaveTeam(player.uniqueId)) {
			Action.sendGameMessage(sender, "Removed ${player.name} from their team")
		} else {
			Commands.errorMessage(sender, "${player.name} is not on a team")
		}
	}

	@Subcommand("random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		if (Commands.opGuard(sender)) return

		val teams = UHC.getTeams()

		val memberLists = Teams.randomMemberLists(sender.server.onlinePlayers.filter { player ->
			!teams.isOnTeam(player.uniqueId) && !PlayerData.isOptingOut(player.uniqueId)
		}, teamSize)

		memberLists.forEach { memberList ->
			internalCreateTeam(sender, memberList.filterNotNull())
		}

		Action.sendGameMessage(sender, "Created ${memberLists.size} teams of size $teamSize")
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("swap")
	@Description("swap the teams of two players")
	fun swapTeams(sender: CommandSender, player0: OfflinePlayer, player1: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val teams = UHC.getTeams()
		if (teams.swapTeams(player0.uniqueId, player1.uniqueId)) {
			Action.sendGameMessage(sender, "Swapped the teams of ${player0.name} and ${player1.name}")
		} else {
			Commands.errorMessage(sender, "Could not swap the teams of ${player0.name} and ${player1.name}")
		}
	}

	@Subcommand("list")
	@Description("lists out all teams and members")
	fun testTeams(sender: CommandSender) {
		fun <T: AbstractTeam> displayTeam(team: T) {
			sender.sendMessage(team.apply(team.grabName()).style(Style.style(TextDecoration.BOLD)))

			team.members.forEach { uuid ->
				sender.sendMessage(Component.text("- ").append(team.apply(Bukkit.getOfflinePlayer(uuid).name ?: "NULL")))
			}
		}

		val teams = UHC.getTeams().teams()

		if (teams.isNotEmpty()) {
			sender.sendMessage(Component.text("Teams:", NamedTextColor.GRAY, TextDecoration.BOLD))
			teams.forEach { team -> displayTeam(team) }
		} else {
			sender.sendMessage(Component.text("No teams", NamedTextColor.GRAY, TextDecoration.BOLD))
		}
	}
}

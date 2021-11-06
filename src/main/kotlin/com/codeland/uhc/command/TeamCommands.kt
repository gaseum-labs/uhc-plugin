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
import com.codeland.uhc.team.TeamData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.util.*

@CommandAlias("uhca")
@Subcommand("team")
class TeamCommands : BaseCommand() {
	/* should the team commands add people to their discord channels */
	private fun useDiscord() = UHC.game?.config?.usingBot?.get() == true

	@Subcommand("clear")
	@Description("remove all current teams")
	fun clearTeamsCommand(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		if (UHC.game == null) {
			TeamData.destroyAllTeams { }
			Action.sendGameMessage(sender, "Cleared all teams")
		}

		TeamData.destroyAllPreTeams {  }
		Action.sendGameMessage(sender, "Cleared all pre teams")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("create")
	@Description("create a new team for a player")
	fun createTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val (addedPlayers, createdTeam) = internalCreateTeam(sender, listOf(player))

		if (createdTeam == null) {
			Action.sendGameMessage(sender, "Created a team for ${addedPlayers.firstOrNull()?.name}")
		} else {
			Commands.errorMessage(sender, "Could not create a team")
		}
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("create")
	@Description("create a new team comprised of players")
	fun createTeamCommand(sender: CommandSender, player0: OfflinePlayer, player1: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val (addedPlayers, createdTeam) = internalCreateTeam(sender, listOf(player0, player1))

		if (createdTeam == null) {
			Action.sendGameMessage(sender, "Created a team for ${addedPlayers.joinToString(" and ") { it.name ?: "Unknown" }}")
		} else {
			Commands.errorMessage(sender, "Could not create a team")
		}
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("join")
	@Description("add the second player to the first player's team")
	fun addPlayerToTeamCommand(sender: CommandSender, teamPlayer: OfflinePlayer, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val (team, id) = TeamData.playersTeamId(teamPlayer.uniqueId)
			?: return Commands.errorMessage(sender, "${teamPlayer.name} is not on a team")

		val addedPlayers = internalAddPlayersToTeam(sender, team, id, listOf(player))

		if (addedPlayers.isNotEmpty()) {
			Action.sendGameMessage(sender, "Added ${addedPlayers.first()} to ${teamPlayer.name}'s team")
		} else {
			Commands.errorMessage(sender, "Could not add players to ${teamPlayer.name}'s team")
		}
	}

	private fun teamPlayerList(sender: CommandSender, players: List<OfflinePlayer>): List<OfflinePlayer> {
		return players.filter { player ->
			if (PlayerData.isOptingOut(player.uniqueId)) {
				Commands.errorMessage(sender, "${player.name} is opting out of participating")
				false
			} else {
				true
			}
		}
	}

	private fun internalCreateTeam(
		sender: CommandSender,
		players: List<OfflinePlayer>,
	): Pair<List<OfflinePlayer>, PreTeam?> {
		val addedPlayers = teamPlayerList(sender, players)

		val createdTeam = TeamData.createTeam(addedPlayers.map { it.uniqueId }, true)

		return Pair(addedPlayers, createdTeam)
	}

	private fun internalAddPlayersToTeam(
		sender: CommandSender,
		team: Team,
		id: Int,
		players: List<OfflinePlayer>
	) : List<OfflinePlayer> {
		val addedPlayers = teamPlayerList(sender, players)

		TeamData.addToTeam(team, if (useDiscord()) id else null, addedPlayers.map { it.uniqueId }, true)

		return addedPlayers
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("remove")
	@Description("remove a player from a team")
	fun removePlayerFromTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		TeamData.playersTeam(player.uniqueId)
			?: return Commands.errorMessage(sender, "${player.name} is not on a team")

		/* unstage and remove player from team */
		TeamData.removeFromTeam(arrayListOf(player.uniqueId),
			updateDiscord = useDiscord(),
			destroyTeam = true,
			updateNames = true
		)

		Action.sendGameMessage(sender, "Removed ${player.name} from their team")
	}

	@Subcommand("random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		if (Commands.opGuard(sender)) return

		val memberLists = TeamData.generateMemberLists(sender.server.onlinePlayers.filter { player ->
			!TeamData.isOnTeam(player.uniqueId) && !PlayerData.isOptingOut(player.uniqueId)
		}.map { it.uniqueId }, teamSize)

		memberLists.forEach { memberList ->
			TeamData.createTeam(memberList.filterNotNull(), true)
		}

		Action.sendGameMessage(sender, "Created ${memberLists.size} teams of size $teamSize")
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("swap")
	@Description("swap the teams of two players")
	fun swapTeams(sender: CommandSender, player0: OfflinePlayer, player1: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		if (player0.uniqueId == player1.uniqueId) return Commands.errorMessage(sender, "Both arguments are the same player")

		val (team0, id0) = TeamData.playersTeamId(player0.uniqueId) ?: return Commands.errorMessage(sender, "${player1.name} is not on a team")
		val (team1, id1) = TeamData.playersTeamId(player0.uniqueId) ?: return Commands.errorMessage(sender, "${player1.name} is not on a team")

		TeamData.addToTeam(team0, if (useDiscord()) id0 else null, listOf(player0.uniqueId), destroyTeam = false)
		TeamData.addToTeam(team1, if (useDiscord()) id1 else null, listOf(player1.uniqueId), destroyTeam = false)

		Action.sendGameMessage(sender, "Swapped teams of ${player0.name} and ${player1.name}")
	}

	@Subcommand("list")
	@Description("lists out all teams and members")
	fun testTeams(sender: CommandSender) {
		fun <T: AbstractTeam> displayTeam(team: T) {
			sender.sendMessage(team.apply(
				when (team) {
					is Team -> team.name
					is PreTeam -> team.name ?: "[Name not chosen]"
					else -> "[Unknown]"
				}
			).style(Style.style(TextDecoration.BOLD)))

			team.members.forEach { uuid ->
				sender.sendMessage(Component.text("- ").append(team.apply(Bukkit.getOfflinePlayer(uuid).name ?: "NULL")))
			}
		}

		val preTeams = TeamData.preTeams

		if (preTeams.isNotEmpty()) {
			sender.sendMessage(Component.text("Pre-teams:", NamedTextColor.WHITE, TextDecoration.BOLD))
			preTeams.forEach { team -> displayTeam(team) }
		} else {
			sender.sendMessage(Component.text("No pre-teams", NamedTextColor.WHITE, TextDecoration.BOLD))
		}

		val teams = TeamData.teams.map { it.first }

		if (teams.isNotEmpty()) {
			sender.sendMessage(Component.text("Teams:", NamedTextColor.GRAY, TextDecoration.BOLD))
			teams.forEach { team -> displayTeam(team) }
		} else {
			sender.sendMessage(Component.text("No teams", NamedTextColor.GRAY, TextDecoration.BOLD))
		}
	}
}

package com.codeland.uhc.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

@CommandAlias("uhca")
@Subcommand("team")
class TeamCommands : BaseCommand() {
	/* should the team commands add people to their discord channels */
	private fun useDiscord() = GameRunner.uhc.isGameGoing() && GameRunner.uhc.usingBot

	@Subcommand("clear")
	@Description("remove all current teams")
	fun clearTeamsCommand(sender : CommandSender) {
		if (Commands.opGuard(sender)) return

		/* unstage everyone and remove teams */
		TeamData.destroyTeam(null, useDiscord(), true) { PlayerData.setStaged(it, false) }

		GameRunner.sendGameMessage(sender, "Cleared all teams")
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("create")
	@Description("create a new team for a player")
	fun createTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		internalAddPlayersToTeam(sender, null, listOf(player), { list ->
			GameRunner.sendGameMessage(sender, "Created a team for ${list.firstOrNull()?.name}")
		}, {
			Commands.errorMessage(sender, "Could not create a team")
		})
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("create")
	@Description("create a new team comprised of players")
	fun createTeamCommand(sender: CommandSender, player1: OfflinePlayer, player2: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		internalAddPlayersToTeam(sender, null, listOf(player1, player2), { list ->
			GameRunner.sendGameMessage(sender, "Created a team for ${list.mapNotNull { it.name }.joinToString(" and ")}")
		}, {
			Commands.errorMessage(sender, "Could not create a team")
		})
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("join")
	@Description("add the second player to the first player's team")
	fun addPlayerToTeamCommand(sender: CommandSender, teamPlayer: OfflinePlayer, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val team = TeamData.playersTeam(teamPlayer.uniqueId)
			?: return Commands.errorMessage(sender, "${teamPlayer.name} is not on a team")

		internalAddPlayersToTeam(sender, team, listOf(player), { list ->
			GameRunner.sendGameMessage(sender, "Added ${list.mapNotNull { it.name }.joinToString(" and ")} to ${teamPlayer.name}'s team")
		}, {
			Commands.errorMessage(sender, "Could not add players to ${teamPlayer.name}'s team")
		})
	}

	private fun internalAddPlayersToTeam(
		sender: CommandSender,
		team: Team?,
		players: List<OfflinePlayer>,
		onSuccess: (List<OfflinePlayer>) -> Unit,
		onFail: () -> Unit
	) {
		val addedPlayers = players.filter { player ->
			if (PlayerData.isOptingOut(player.uniqueId)) {
				Commands.errorMessage(sender, "${player.name} is opting out of participating")
				false
			} else {
				true
			}
		}

		val team = TeamData.addToTeam(team, addedPlayers.map { it.uniqueId }, useDiscord(), true) { PlayerData.setStaged(it, true) }

		if (team == null) onFail() else onSuccess(addedPlayers)
	}

	@CommandCompletion("@uhcplayer")
	@Subcommand("remove")
	@Description("remove a player from a team")
	fun removePlayerFromTeamCommand(sender: CommandSender, player: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		TeamData.playersTeam(player.uniqueId)
			?: return Commands.errorMessage(sender, "${player.name} is not on a team")

		/* unstage and remove player from team */
		TeamData.removeFromTeam(arrayListOf(player.uniqueId), useDiscord(), true, true)
		PlayerData.setStaged(player.uniqueId, false)

		GameRunner.sendGameMessage(sender, "Removed ${player.name} from their team")
	}

	@Subcommand("random")
	@Description("create random teams")
	fun randomTeams(sender : CommandSender, teamSize : Int) {
		if (Commands.opGuard(sender)) return

		val memberLists = TeamData.generateMemberLists(sender.server.onlinePlayers.filter { player ->
			!TeamData.isOnTeam(player.uniqueId) && !PlayerData.isOptingOut(player.uniqueId)
		}.map { it.uniqueId }, teamSize)

		memberLists.forEach { memberList ->
			TeamData.addToTeam(null, memberList.filterNotNull(), useDiscord(), true) { PlayerData.setStaged(it, true) }
		}

		GameRunner.sendGameMessage(sender, "Created ${memberLists.size} teams of size $teamSize")
	}

	@CommandCompletion("@uhcplayer @uhcplayer")
	@Subcommand("swap")
	@Description("swap the teams of two players")
	fun swapTeams(sender: CommandSender, player1: OfflinePlayer, player2: OfflinePlayer) {
		if (Commands.opGuard(sender)) return

		val team1 = TeamData.playersTeam(player1.uniqueId) ?: return Commands.errorMessage(sender, "${player1.name} is not on a team")
		val team2 = TeamData.playersTeam(player2.uniqueId) ?: return Commands.errorMessage(sender, "${player2.name} is not on a team")

		TeamData.addToTeam(team2, listOf(player1.uniqueId), useDiscord(), false) {}
		TeamData.addToTeam(team1, listOf(player2.uniqueId), useDiscord(), false) {}

		GameRunner.sendGameMessage(sender, "Swapped teams of ${player1.name} and ${player2.name}")
	}

	@Subcommand("list")
	@Description("lists out all teams and members")
	fun testTeams(sender: CommandSender) {
		val teams = TeamData.teams

		if (teams.isNotEmpty()) {
			sender.sendMessage(Component.text("Teams:", NamedTextColor.GRAY, TextDecoration.BOLD))

			teams.forEach { team ->
				sender.sendMessage(team.apply("${team.name ?: "(Name not chosen)"}:").style(Style.style(TextDecoration.BOLD)))

				team.members.forEach { uuid ->
					sender.sendMessage(Component.text("- ").append(team.apply(Bukkit.getOfflinePlayer(uuid).name
						?: "NULL")))
				}
			}
		}

		val individualPlayers = PlayerData.playerDataList.filter { (uuid, playerData) -> (playerData.participating || playerData.staged) && !TeamData.isOnTeam(uuid) }

		if (individualPlayers.isNotEmpty()) {
			sender.sendMessage(Component.text("Individual players:", NamedTextColor.GRAY, TextDecoration.BOLD))

			individualPlayers.forEach { (uuid, playerData) ->
				sender.sendMessage(Component.text("- ").append(Component.text(Bukkit.getOfflinePlayer(uuid).name ?: "NULL")))
			}
		}

		if (teams.isEmpty() && individualPlayers.isEmpty())
			sender.sendMessage(Component.text("There are no teams", NamedTextColor.GRAY, TextDecoration.BOLD))
	}
}

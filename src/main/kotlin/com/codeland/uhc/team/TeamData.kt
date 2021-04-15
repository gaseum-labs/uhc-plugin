package com.codeland.uhc.team

import org.bukkit.Bukkit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

object TeamData {
	val teamColor = TeamColor(4)

	val teams = ArrayList<Team>()

	var nextTeamID = 0

	fun playersTeam(playerUuid: UUID): Team? {
		for (team in teams)
			for (teamUUID in team.members)
				if (playerUuid == teamUUID) return team

		return null
	}

	fun isOnTeam(playerUuid: UUID) = playersTeam(playerUuid) != null

	/**
	 * @param team the team to add players to. Null if a new team is to be created
	 * @param destroyTeam set to true if doing anything other than swapping
	 * @return null if the team could not be created
	 */
	fun addToTeam(team: Team?, players: List<UUID>, destroyTeam: Boolean, onAdd: (UUID) -> Unit): Team? {
		if (players.isEmpty()) return null

		if (team == null) {
			/* create the team with players added */
			val (color1, color2) = teamColor.pickTeam() ?: return null
			val newTeam = Team(nextTeamID, color1, color2, players as ArrayList<UUID>)
			++nextTeamID

			/* remove players from their old team */
			players.forEach { uuid -> removeFromTeam(uuid, destroyTeam, destroyTeam) }

			/* add new team internally */
			teams.add(newTeam)

			updateMemberNames(newTeam.members)
			players.forEach { onAdd(it) }

			return newTeam

		} else {
			val addedPlayers = players.mapNotNull { uuid ->
				/* remove player from old team if they are on one */
				val oldTeam = playersTeam(uuid)
				if (oldTeam == team) return@mapNotNull null

				if (oldTeam != null) removeFromTeam(oldTeam, uuid, destroyTeam, destroyTeam)

				/* add player to team */
				team.members.add(uuid)

				uuid
			}

			updateMemberNames(addedPlayers as ArrayList<UUID>)
			addedPlayers.forEach { onAdd(it) }

			return team
		}
	}

	fun removeFromTeam(player: UUID, destroyTeam: Boolean, updateNames: Boolean) {
		val oldTeam = playersTeam(player) ?: return
		removeFromTeam(oldTeam, player, destroyTeam, updateNames)
	}

	fun removeFromTeam(oldTeam: Team, uuid: UUID, destroyTeam: Boolean, updateNames: Boolean) {
		/* remove player from the team */
		oldTeam.members.removeIf { memberUuid -> memberUuid == uuid }

		if (oldTeam.members.isEmpty() && destroyTeam) destroyTeam(oldTeam, updateNames) {}
	}

	/**
	 * @param team the team to destroy. Null to destroy all teams
	 * @param onRemove called for each player that was removed from a team this way
	 */
	fun destroyTeam(team: Team?, updateNames: Boolean, onRemove: (UUID) -> Unit) {
		teams.removeIf { currentTeam ->
			if (currentTeam === team || team == null) {
				/* store a copy of letters to iterate over */
				val oldMemberList = ArrayList(currentTeam.members)

				/* remove the internal representation of players from the team */
				currentTeam.members.removeIf { onRemove(it); true }

				/* the names will be updated as if they were not on the team */
				if (updateNames) updateMemberNames(oldMemberList)
				teamColor.removeTeam(currentTeam.color1, currentTeam.color2)

				true
			} else {
				false
			}
		}
	}

	private fun updateMemberNames(memberList: ArrayList<UUID>) {
		memberList.mapNotNull { Bukkit.getPlayer(it) }.forEach { NameManager.updateName(it) }
	}

	fun generateMemberLists(players: List<UUID>, teamSize: Int): Array<List<UUID?>> {
		val used = Array(players.size) { false }

		val numTeams = ceil(players.size / teamSize.toDouble()).toInt()

		return Array(numTeams) {
			List(teamSize) {
				var index = (Math.random() * players.size).toInt()
				val startIndex = index

				while (used[index]) {
					index = (index + 1) % players.size
					if (index == startIndex) return@List null
				}
				used[index] = true

				players[index]
			}
		}
	}
}

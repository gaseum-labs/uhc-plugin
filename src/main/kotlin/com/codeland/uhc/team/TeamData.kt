package com.codeland.uhc.team

import com.codeland.uhc.core.GameRunner
import org.bukkit.Bukkit
import org.bukkit.GameMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
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

	/* team management interface */

	/**
	 * @param team the team to add players to. Null if a new team is to be created
	 * @param destroyTeam set to true if doing anything other than swapping
	 * @return null if the team could not be created
	 */
	fun addToTeam(team: Team?, players: List<UUID>, discord: Boolean, destroyTeam: Boolean, onAdd: (UUID) -> Unit): Team? {
		if (players.isEmpty()) return null

		if (team == null) {
			/* create the team with players added */
			val (color1, color2) = teamColor.pickTeam() ?: return null
			val newTeam = Team(nextTeamID, color1, color2, players as ArrayList<UUID>)
			++nextTeamID

			/* remove players from their old team */
			/* do not make them leave the discord or update their names, because they're about to re added */
            removeFromTeam(players, false, destroyTeam, false)

			/* add them to the team on discord */
			if (discord) GameRunner.bot?.addToTeamChannel(newTeam, players)

			/* add new team internally */
			teams.add(newTeam)

			updateMemberNames(newTeam.members)
			players.forEach { onAdd(it) }

			return newTeam

		} else {
			/* remove players from their old team */
			removeFromTeam(players, false, destroyTeam, false)

			/* add these players to the team */
			team.members.addAll(players)

			updateMemberNames(players as ArrayList<UUID>)
			players.forEach { onAdd(it) }

			return team
		}
	}

	fun removeFromTeam(players: List<UUID>, discord: Boolean, destroyTeam: Boolean, updateNames: Boolean) {
		/* associate all player's with their team */
		val teamMap = HashMap<Team, ArrayList<UUID>>()

		players.forEach { player ->
			val team = playersTeam(player)
			if (team != null) teamMap.getOrPut(team, { ArrayList() }).add(player)
		}

		teamMap.forEach { (team, teamPlayers) ->
			/* remove these players from the team channel */
			if (discord) GameRunner.bot?.removeFromTeamChannel(team, team.members.size, teamPlayers)

			/* remove these players from the team internally */
			team.members.removeAll(teamPlayers)

			/* destroy the team if everyone has been removed */
			if (destroyTeam && team.members.isEmpty()) destroyTeam(team, discord, updateNames) {}
		}
	}

	/**
	 * @param team the team to destroy. Null to destroy all teams
	 * @param onRemove called for each player that was removed from a team this way
	 */
	fun destroyTeam(team: Team?, discord: Boolean, updateNames: Boolean, onRemove: (UUID) -> Unit) {
		teams.removeIf { currentTeam ->
			if (currentTeam === team || team == null) {
				/* store a copy of letters to iterate over */
				val oldMemberList = ArrayList(currentTeam.members)

				/* destroy team channel on discord */
				if (discord) GameRunner.bot?.destroyTeamChannel(currentTeam)

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

	/* util */

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

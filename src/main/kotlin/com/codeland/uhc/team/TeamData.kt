package com.codeland.uhc.team

import com.codeland.uhc.core.UHC
import org.bukkit.Bukkit
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random.Default.nextInt

object TeamData {
	val teamColor = TeamColor(4)

	val preTeams = ArrayList<PreTeam>()
	val teams = ArrayList<Pair<Team, Int>>()

	fun playersTeam(playerUuid: UUID): Team? {
		return teams.find { (team, _) ->
			team.members.contains(playerUuid)
		}?.first
	}

	fun playersTeamId(playerUuid: UUID): Pair<Team, Int>? {
		return teams.find { (team, _) ->
			team.members.contains(playerUuid)
		}
	}

	fun isOnTeam(playerUuid: UUID) = playersTeam(playerUuid) != null

	private fun nextAvailableTeamId(): Int {
		for (i in teams.indices) {
			if (teams[i].second > i) {
				return i
			}
		}

		return teams.size
	}

	/* team management interface */

	fun createTeam(players: List<UUID>, destroyTeam: Boolean): PreTeam? {
		if (players.isEmpty()) return null

		/* create the team with players added */
		val (color1, color2) = teamColor.pickTeam() ?: return null
		val newTeam = PreTeam(color1, color2, ArrayList(players))

		/* remove players from their old team */
		/* do not make them leave the discord or update their names, because they're about to re added */
		removeFromTeam(players, false, destroyTeam, false)

		/* add new team internally */
		preTeams.add(newTeam)

		updateMemberNames(newTeam.members)

		return newTeam
	}

	/**
	 * @param team the team to add players to. Null if a new team is to be created
	 * @param destroyTeam set to true if doing anything other than swapping
	 * @return null if the team could not be created
	 */
	fun <T : AbstractTeam> addToTeam(team: T, discordId: Int?, players: List<UUID>, destroyTeam: Boolean) {
		/* remove players from their old team */
		removeFromTeam(players, false, destroyTeam, false)

		/* add these players to the team */
		team.members.addAll(players)

		if (discordId != null) UHC.bot?.addToTeamChannel(discordId, players)

		updateMemberNames(players)
	}

	fun removeFromTeam(players: List<UUID>,  updateDiscord: Boolean, destroyTeam: Boolean, updateNames: Boolean) {
		/* associate all player's with their team */
		val teamMap = HashMap<Pair<Team, Int>, ArrayList<UUID>>()

		players.forEach { player ->
			val teamId = playersTeamId(player)
			if (teamId != null) teamMap.getOrPut(teamId) { ArrayList() }.add(player)
		}

		teamMap.forEach { (teamId, teamPlayers) ->
			val (team, id) = teamId

			/* remove these players from the team channel */
			if (updateDiscord) UHC.bot?.removeFromTeamChannel(id, team.members.size, teamPlayers)

			/* remove these players from the team internally */
			team.members.removeAll(teamPlayers)

			/* destroy the team if everyone has been removed */
			if (destroyTeam && team.members.isEmpty()) destroyTeam(team, if (updateDiscord) id else null, updateNames) {}

			/* update names to no longer be on team */
			if (updateNames) updateMemberNames(teamPlayers)
		}
	}

	private fun <T : AbstractTeam> internalRemoveTeam(team: T, discordId: Int?, updateNames: Boolean, onRemove: (UUID) -> Unit) {
		/* store a copy of letters to iterate over */
		val oldMemberList = ArrayList(team.members)

		/* destroy team channel on discord */
		if (discordId != null) UHC.bot?.destroyTeamChannel(discordId)

		/* remove the internal representation of players from the team */
		team.members.forEach { onRemove(it) }
		team.members.clear()

		/* the names will be updated as if they were not on the team */
		if (updateNames) updateMemberNames(oldMemberList)
		teamColor.removeTeam(team.colors)
	}

	/**
	 * @param team the team to destroy
	 * @param onRemove called for each player that was removed from a team this way
	 */
	fun <T : AbstractTeam> destroyTeam(team: T, discordId: Int?, updateNames: Boolean, onRemove: (UUID) -> Unit) {
		internalRemoveTeam(team, discordId, updateNames, onRemove)

		when (team) {
			is PreTeam -> preTeams.remove(team)
			is Team -> teams.removeIf { it.first === team }
		}
	}

	fun destroyAllPreTeams(onRemove: (UUID) -> Unit) {
		preTeams.forEach { internalRemoveTeam(it, null, true, onRemove) }
		preTeams.clear()
	}

	fun destroyAllTeams(onRemove: (UUID) -> Unit) {
		teams.forEach { (team, id) -> internalRemoveTeam(team, id, true, onRemove) }
		teams.clear()
	}

	fun convertPreTeamsToTeams(moveToVcs: Boolean) {
		preTeams.forEach { preTeam ->
			teams.add(Pair(preTeam.toTeam(), nextAvailableTeamId()))
		}
		preTeams.clear()
	}

	fun convertPreTeamToTeam(preTeam: PreTeam) {
		teams.add(Pair(preTeam.toTeam(), nextAvailableTeamId()))
		preTeams.remove(preTeam)
	}

	/* util */

	private fun updateMemberNames(memberList: List<UUID>) {
		memberList.mapNotNull { Bukkit.getPlayer(it) }.forEach { NameManager.updateName(it) }
	}

	fun generateMemberLists(players: List<UUID>, teamSize: Int): Array<List<UUID?>> {
		val used = Array(players.size) { false }

		val numTeams = ceil(players.size / teamSize.toDouble()).toInt()

		return Array(numTeams) {
			List(teamSize) {
				var index = nextInt(players.size)
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

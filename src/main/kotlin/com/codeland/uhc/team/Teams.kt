package com.codeland.uhc.team

import com.codeland.uhc.util.extensions.ArrayListExtensions.removeFirst
import org.bukkit.Bukkit
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.random.Random

abstract class Teams <T: AbstractTeam> (val onPlayerMovement: (uuids: List<UUID>, teamId: Int?) -> Unit) {
	val teams = ArrayList<Pair<T, Int>>()

	fun playersTeam(uuid: UUID): T? {
		return teams.find { (team, _) ->
			team.members.contains(uuid)
		}?.first
	}

	fun playersTeamId(uuid: UUID): Pair<T, Int>? {
		return teams.find { (team, _) ->
			team.members.contains(uuid)
		}
	}

	fun teamsId(team: T): Int? {
		return teams.find { (t, _) ->
			t === team
		}?.second
	}

	/* major operations */

	fun addTeam(team: T): Int {
		/* remove players from their old team */
		team.members.forEach { uuid ->
			internalRemoveFromTeam(uuid)
		}

		val id = nextAvailableTeamId()

		teams.add(id, Pair(team, id))

		onPlayerMovement(team.members, id)

		return id
	}

	fun removeTeam(team: T)  {
		teams.removeFirst { it.first === team }

		onPlayerMovement(team.members, null)
	}

	fun clearTeams() {
		val allPlayers = teams.flatMap { it.first.members }

		teams.clear()

		onPlayerMovement(allPlayers, null)
	}

	fun joinTeam(uuid: UUID, team: T) {
		internalRemoveFromTeam(uuid)

		team.members.add(uuid)

		onPlayerMovement(listOf(uuid), teamsId(team) ?: return)
	}

	fun leaveTeam(uuid: UUID) {
		internalRemoveFromTeam(uuid)

		onPlayerMovement(listOf(uuid), null)
	}

	fun swapTeams(uuid0: UUID, uuid1: UUID): Boolean {
		val (team0, id0) = playersTeamId(uuid0) ?: Pair(null, null)
		val (team1, id1) = playersTeamId(uuid1) ?: Pair(null, null)

		if (team0 === team1) return false

		team0?.members?.remove(uuid0)
		team1?.members?.remove(uuid1)

		team0?.members?.add(uuid1)
		team1?.members?.add(uuid0)

		onPlayerMovement(listOf(uuid0), id1)
		onPlayerMovement(listOf(uuid1), id0)

		return true
	}

	/* internal */

	private fun nextAvailableTeamId(): Int {
		for (i in teams.indices) {
			if (teams[i].second > i) {
				return i
			}
		}

		return teams.size
	}

	private fun internalRemoveFromTeam(uuid: UUID) {
		val team = playersTeam(uuid) ?: return
		team.members.remove(uuid)

		if (team.members.isEmpty()) {
			teams.removeFirst { it.first === team }
		}
	}

	companion object {
		fun updateNames(uuids: List<UUID>) {
			uuids.mapNotNull { Bukkit.getPlayer(it) }.forEach { NameManager.updateName(it) }
		}

		fun generateMemberLists(players: List<UUID>, teamSize: Int): Array<List<UUID?>> {
			val used = Array(players.size) { false }

			val numTeams = ceil(players.size / teamSize.toDouble()).toInt()

			return Array(numTeams) {
				List(teamSize) {
					var index = Random.nextInt(players.size)
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
}

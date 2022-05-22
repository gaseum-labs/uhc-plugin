package org.gaseumlabs.uhc.team

import org.gaseumlabs.uhc.util.extensions.ArrayListExtensions.removeFirst
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.util.*
import kotlin.math.ceil
import kotlin.random.Random

class Teams<T : AbstractTeam>(
	val onPlayerMovement: (action: MoveAction) -> Unit,
	val teamCleanup: (team: T) -> Unit,
) {
	sealed class MoveAction(val uuids: List<UUID>, val team: AbstractTeam?)
	class ClearAction(uuids: List<UUID>) : MoveAction(uuids, null)
	class AddAction(val id: Int, uuids: List<UUID>, team: AbstractTeam?) : MoveAction(uuids, team)
	class RemoveAction(val size: Int, val id: Int, uuids: List<UUID>) : MoveAction(uuids, null)

	private val teams = ArrayList<Pair<T, Int>>()

	fun teams(): List<T> {
		return teams.map { it.first }
	}

	fun playersTeam(uuid: UUID): T? {
		return teams.find { (team) -> team.members.contains(uuid) }?.first
	}

	fun isOnTeam(uuid: UUID): Boolean {
		return playersTeam(uuid) != null
	}

	fun playersTeamId(uuid: UUID): Pair<T, Int>? {
		return teams.find { (team) -> team.members.contains(uuid) }
	}

	fun teamsId(team: T): Int? {
		return teams.find { (t) -> t === team }?.second
	}

	/* major operations */

	fun addTeam(team: T): Int {
		/* remove players from their old team */
		team.members.forEach { uuid ->
			internalRemoveFromTeam(uuid)
		}

		val id = nextAvailableTeamId()

		teams.add(id, Pair(team, id))

		onPlayerMovement(AddAction(id, team.members, team))

		return id
	}

	fun removeTeam(team: T) {
		val (removedTeam, removedId) = teams.removeFirst { it.first === team } ?: return

		teamCleanup(team)

		onPlayerMovement(RemoveAction(0, removedId, removedTeam.members))
	}

	fun clearTeams() {
		val allPlayers = teams.flatMap { it.first.members }

		teams.forEach { (team) -> teamCleanup(team) }
		teams.clear()

		onPlayerMovement(ClearAction(allPlayers))
	}

	fun joinTeam(uuid: UUID, team: T) {
		val newTeamId = teamsId(team) ?: return

		internalRemoveFromTeam(uuid)

		team.members.add(uuid)

		onPlayerMovement(AddAction(newTeamId, listOf(uuid), team))
	}

	fun leaveTeam(uuid: UUID): Boolean {
		val (team, id) = internalRemoveFromTeam(uuid) ?: return false

		onPlayerMovement(RemoveAction(id, team.members.size, listOf(uuid)))

		return true
	}

	fun swapTeams(uuid0: UUID, uuid1: UUID): Boolean {
		val (team0, id0) = playersTeamId(uuid0) ?: return false
		val (team1, id1) = playersTeamId(uuid1) ?: return false

		if (team0 === team1) return false

		team0.members.remove(uuid0)
		team1.members.remove(uuid1)

		team0.members.add(uuid1)
		team1.members.add(uuid0)

		onPlayerMovement(AddAction(id1, listOf(uuid0), team1))
		onPlayerMovement(AddAction(id0, listOf(uuid1), team0))

		return true
	}

	fun <R : AbstractTeam> transfer(otherTeams: Teams<R>, convert: (T) -> R) {
		teams.removeIf { (team, _) ->
			otherTeams.addTeam(convert(team))
			true
		}
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

	private fun internalRemoveFromTeam(uuid: UUID): Pair<T, Int>? {
		val (team, id) = playersTeamId(uuid) ?: return null
		team.members.remove(uuid)

		if (team.members.isEmpty()) {
			teams.removeFirst { it.first === team }
			teamCleanup(team)
		}

		return Pair(team, id)
	}

	companion object {
		fun updateNames(uuids: List<UUID>, team: AbstractTeam?) {
			uuids.mapNotNull { Bukkit.getPlayer(it) }.forEach { NameManager.updateNominalTeams(it, team, false) }
		}

		fun randomMemberLists(players: List<OfflinePlayer>, teamSize: Int): Array<List<OfflinePlayer?>> {
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

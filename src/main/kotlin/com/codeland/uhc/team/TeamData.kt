package com.codeland.uhc.team

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.Chat
import com.codeland.uhc.event.Coloring
import com.codeland.uhc.util.Util
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.StructureModifier
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.LockSupport
import javax.naming.Name
import kotlin.collections.ArrayList

object TeamData {
	val teamColors = arrayOf(
		BLUE,
		RED,
		GREEN,
		AQUA,
		LIGHT_PURPLE,
		YELLOW,
		DARK_RED,
		DARK_AQUA,
		DARK_PURPLE,
		GRAY,
		DARK_BLUE,
		DARK_GREEN,
		DARK_GRAY
	)

	val teamColorIndices = Array(ChatColor.values().size) { i ->
		teamColors.indexOf(ChatColor.values()[i])
	}

	val MAX_TEAMS = 91

	val teams = ArrayList<Team>()

	fun colorPairFromIndex(index: Int): ColorPair? {
		val pair = Util.getCombination(index, teamColors.size)

		if (pair.first == -1) return null
		if (pair.first == pair.second) return ColorPair(teamColors[pair.first])

		return ColorPair(teamColors[pair.first], teamColors[pair.second])
	}

	fun colorPairPermutation(n: Int): ColorPair? {
		if (n > teamColors.size * teamColors.size) return null
		val first: ChatColor = teamColors[n / teamColors.size]
		var second: ChatColor? = teamColors[n % teamColors.size]
		if (second == first) second = null
		return ColorPair(first, second)
	}

	fun teamExists(colorPair: ColorPair): Boolean {
		return teams.any { team -> team.colorPair == colorPair }
	}

	fun playersTeam(playerUuid: UUID): Team? {
		for (team in teams)
			for (teamUUID in team.members)
				if (playerUuid == teamUUID) return team

		return null
	}

	fun isOnTeam(playerUuid: UUID): Boolean {
		for (team in teams)
			for (teamUUID in team.members)
				if (playerUuid == teamUUID) return true

		return false
	}

	fun playersColor(uuid: UUID): Coloring {
		val team = playersTeam(uuid) ?: return Chat.solid(BLUE)
		return team.colorPair::colorString
	}

	fun addToTeam(colorPair: ColorPair, uuid: UUID, destroyTeam: Boolean, onComplete: (Team) -> Unit) {
		/* find if the new team exists */
		var newTeam = teams.find { team -> team.colorPair == colorPair }

		/* create the team if it doesn't exist */
		if (newTeam == null) {
			newTeam = Team(colorPair)
			teams.add(newTeam)
		}

		addToTeam(newTeam, uuid, destroyTeam, onComplete)
	}

	fun addToTeam(team: Team, uuid: UUID, destroyTeam: Boolean, onComplete: (Team) -> Unit) {
		/* final branch */
		fun internalAdd(o: Boolean = true) {
			team.members.add(uuid)

			val onlinePlayer = Bukkit.getPlayer(uuid)
			if (onlinePlayer != null) NameManager.updateName(onlinePlayer)

			onComplete(team)
		}

		/* second layer of checks */
		fun botAdd(o: Boolean = true) {
			if (GameRunner.uhc.usingBot)
				GameRunner.bot?.addPlayerToTeam(team, uuid, ::internalAdd) ?: internalAdd()
			else
				internalAdd()
		}

		/* remove player from old team if they are on one */
		val oldTeam = playersTeam(uuid)

		if (oldTeam != null)
			removeFromTeam(oldTeam, uuid, destroyTeam, ::botAdd)
		else
			botAdd()
	}

	fun removeFromTeam(player: UUID, destroyTeam: Boolean, onComplete: (Boolean) -> Unit) {
		removeFromTeam(playersTeam(player), player, destroyTeam, onComplete)
	}

	fun removeFromTeam(oldTeam: Team?, uuid: UUID, destroyTeam: Boolean, onComplete: (Boolean) -> Unit) {
		if (oldTeam == null) {
			onComplete(false)

		} else {
			oldTeam.members.removeIf { memberUuid -> memberUuid == uuid }

			val onlinePlayer = Bukkit.getPlayer(uuid)
			if (onlinePlayer != null) NameManager.updateName(onlinePlayer)

			/* remove the team if no one is left on it */
			if (destroyTeam && oldTeam.members.isEmpty()) {
				teams.removeIf { team -> team === oldTeam }

				if (GameRunner.uhc.usingBot)
					GameRunner.bot?.destroyTeam(oldTeam) { onComplete(true) }
				else
					onComplete(true)
			}
		}
	}

	fun removeAllTeams(onComplete: () -> Unit) {
		while (teams.isNotEmpty()) {
			removeFromTeam(teams[0], teams[0].members[0], true) {
				synchronized(this) {
					if (teams.isEmpty()) onComplete
				}
			}
		}
	}

	fun removeAllTeams(onRemove: (UUID) -> Unit, onComplete: (Boolean) -> Unit) {
		while (teams.isNotEmpty()) {
			onRemove(teams[0].members[0])

			removeFromTeam(teams[0], teams[0].members[0], true) {
				synchronized(this) {
					if (teams.isEmpty()) onComplete
				}
			}
		}
	}
}
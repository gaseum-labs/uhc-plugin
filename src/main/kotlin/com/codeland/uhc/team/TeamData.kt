package com.codeland.uhc.team

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.event.Chat
import com.codeland.uhc.event.Coloring
import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.DataWatcher
import net.minecraft.server.v1_16_R3.DataWatcherObject
import net.minecraft.server.v1_16_R3.DataWatcherRegistry
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*
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

	fun teamExists(colorPair: ColorPair, otherThan: Team): Boolean {
		return teams.any { team -> team !== otherThan && team.colorPair == colorPair }
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

	fun addToTeam(colorPair: ColorPair, uuid: UUID, destroyTeam: Boolean): Team {
		/* find if the new team exists */
		var newTeam = teams.find { team -> team.colorPair == colorPair }

		/* create the team if it doesn't exist */
		if (newTeam == null) {
			newTeam = Team(colorPair)
			teams.add(newTeam)
		}

		return addToTeam(newTeam, uuid, destroyTeam)
	}

	fun addToTeam(team: Team, uuid: UUID, destroyTeam: Boolean): Team {
		/* remove player from old team if they are on one */
		val oldTeam = playersTeam(uuid)
		if (oldTeam == team) return team
		if (oldTeam != null) removeFromTeam(oldTeam, uuid, destroyTeam)

		/* actually add to team internally */
		team.members.add(uuid)

		/* update player's display name */
		val onlinePlayer = Bukkit.getPlayer(uuid)
		if (onlinePlayer != null) NameManager.updateName(onlinePlayer)

		/* move player's vc */
		if (GameRunner.uhc.usingBot) GameRunner.bot?.addToTeamChannel(team, uuid)

		return team
	}

	fun removeFromTeam(player: UUID, destroyTeam: Boolean) {
		removeFromTeam(playersTeam(player), player, destroyTeam)
	}

	fun removeFromTeam(oldTeam: Team?, uuid: UUID, destroyTeam: Boolean): Boolean {
		if (oldTeam == null) return false

		oldTeam.members.removeIf { memberUuid -> memberUuid == uuid }

		val onlinePlayer = Bukkit.getPlayer(uuid)
		if (onlinePlayer != null) NameManager.updateName(onlinePlayer)

		/* remove the team if no one is left on it */
		if (destroyTeam && oldTeam.members.isEmpty()) {
			teams.removeIf { team -> team === oldTeam }

			if (GameRunner.uhc.usingBot) GameRunner.bot?.destroyTeamChannel(oldTeam)
		}

		return true
	}

	fun removeAllTeams(onRemove: (UUID) -> Unit) {
		while (teams.isNotEmpty()) {
			onRemove(teams[0].members[0])
			removeFromTeam(teams[0], teams[0].members[0], true)
		}
	}
}

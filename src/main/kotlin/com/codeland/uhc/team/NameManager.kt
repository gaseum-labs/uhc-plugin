package com.codeland.uhc.team

import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

object NameManager {
	fun updateName(player: Player) {
		TeamData.refreshPlayer(player)

		val team = TeamData.playersTeam(player)
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		val fakeTeam = scoreboard.getTeam(player.name)

		if (team == null) {
			player.setPlayerListName(null)

			if (fakeTeam == null)
				makeFakeTeam(player.name, ColorPair(ChatColor.WHITE))
			else
				updateTeam(fakeTeam, player.name, ColorPair(ChatColor.WHITE))

		} else {
			player.setPlayerListName(team.colorPair.colorString(player.name))

			if (fakeTeam == null)
				makeFakeTeam(player.name, team.colorPair)
			else
				updateTeam(fakeTeam, player.name, team.colorPair)
		}
	}

	fun makeFakeTeam(name: String, colorPair: ColorPair): Team {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		val team = scoreboard.registerNewTeam(name)
		team.addEntry(name)

		updateTeam(team, name, colorPair)

		return team
	}

	fun updateTeam(team: Team, name: String, colorPair: ColorPair) {
		team.color = colorPair.color0

		if (colorPair.color0 == ChatColor.WHITE) {
			team.prefix = ""
			team.suffix = ""
		} else {
			team.prefix = "${colorPair.color0}■ "
			team.suffix = " ${colorPair.color1 ?: colorPair.color0}■"
		}
	}
}

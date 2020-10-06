package com.codeland.uhc.team

import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

object NameManager {
	fun updateName(player: Player) {
		val team = TeamData.playersTeam(player)
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		val fakeTeam = scoreboard.getTeam(player.name)

		Util.log("player name: [${player.name}]")

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
		val fullName = colorPair.colorString(name)
		Util.log(fullName)
		val halfLength = fullName.length / 2

		team.prefix = fullName.substring(0, halfLength)
		team.suffix = fullName.substring(halfLength)
	}
}

package com.codeland.uhc.team

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.phases.waiting.LobbyPvp
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.scoreboard.Team

object NameManager {
	fun updateName(player: Player) {
		val (playerData, firstTime) = GameRunner.uhc.initialPlayerData(player.uniqueId)

		playerData.setSkull(player)

		while (playerData.actionsQueue.isNotEmpty()) {
			Util.log("PERFORMING ACTION FOR ${player.name}")
			playerData.actionsQueue.remove()(player)
		}

		playerData.replaceZombieWithPlayer(player)

		val team = TeamData.playersTeam(player.uniqueId)
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		val fakeTeam = scoreboard.getTeam(player.name) ?: makeFakeTeam(player.name)

		if (GameRunner.uhc.isPhase(PhaseType.WAITING) && LobbyPvp.getPvpData(player).inPvp) {
			player.setPlayerListName(null)
			updatePvp(fakeTeam)

		} else if (team == null) {
			player.setPlayerListName(null)
			updateTeam(fakeTeam, ColorPair(ChatColor.WHITE))

		} else {
			player.setPlayerListName(team.colorPair.colorString(player.name))
			updateTeam(fakeTeam, team.colorPair)
		}
	}

	fun makeFakeTeam(name: String): Team {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		val team = scoreboard.registerNewTeam(name)
		team.addEntry(name)

		return team
	}

	fun updatePvp(team: Team) {
		team.color = ChatColor.RED

		team.prefix = ""
		team.suffix = "${ChatColor.DARK_RED}${ChatColor.BOLD} PVP"
	}

	fun updateTeam(team: Team, colorPair: ColorPair) {
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

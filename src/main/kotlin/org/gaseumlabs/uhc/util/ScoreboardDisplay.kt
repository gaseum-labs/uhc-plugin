package org.gaseumlabs.uhc.util

import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import kotlin.random.Random

class ScoreboardDisplay(val name: TextComponent, var size: Int) {
	private val objective: Objective
	private val id: String
	private val teams: Array<Team>

	init {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

		val objectiveID = randomObjectiveID()
		id = objectiveID.substring(3)
		objective = scoreboard.registerNewObjective(objectiveID, "dummy", name)

		teams = Array(size) { i ->
			val team = scoreboard.registerNewTeam("$id$i")

			val teamEntry = generateTeamEntry(i)

			team.addEntry(teamEntry)
			objective.getScore(teamEntry).score = size - i - 1

			team
		}
	}

	fun setName(name: TextComponent) = objective.displayName(name)
	fun setLine(line: Int, value: TextComponent) = teams[line].prefix(value)

	fun show() {
		objective.displaySlot = DisplaySlot.SIDEBAR
	}

	fun hide() {
		objective.displaySlot = null
	}

	fun destroy() {
		objective.unregister()
		teams.forEach { it.unregister() }
	}

	companion object {
		private val chars = ('0'..'9') + ('A'..'Z') + ('a'..'z')

		fun randomObjectiveID() =
			String(CharArray(16) { chars[Random.nextInt(0, chars.size)] })

		fun generateTeamEntry(number: Int) = "${
			ChatColor.values()[number / ChatColor.values().size]
		}${
			ChatColor.values()[number % ChatColor.values().size]
		}"
	}
}
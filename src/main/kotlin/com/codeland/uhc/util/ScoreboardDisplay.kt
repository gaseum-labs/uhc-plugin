package com.codeland.uhc.util

import com.codeland.uhc.team.ColorPair
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team

class ScoreboardDisplay(val name: String, var size: Int) {
	var lines: Array<String>? = null
	val objective: Objective
	val id: String

	val teams: Array<Team>

	init {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

		val objectiveID = generateObjectiveID()
		id = objectiveID.substring(3)
		objective = scoreboard.registerNewObjective(objectiveID, "dummy", name)

		lines = Array(size) { "" }

		teams = Array(size) { i ->
			val team = scoreboard.registerNewTeam("$id$i")

			val teamEntry = generateTeamEntry(i)

			team.addEntry(teamEntry)
			objective.getScore(teamEntry).score = size - i - 1

			team
		}
	}

	fun setName(name: String) {
		objective.displayName = name
	}

	fun setLine(line: Int, value: String) {
		teams[line].prefix = value
	}

	fun destroy() {
		objective.unregister()

		teams.forEach { team ->
			team.unregister()
		}
	}

	fun show() {
		objective.displaySlot = DisplaySlot.SIDEBAR
	}

	fun hide() {
		objective.displaySlot = null
	}

	companion object {
		fun generateObjectiveID(): String {
			val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

			var id = randomObjectiveID()

			while (scoreboard.getObjective(id) != null) id = randomObjectiveID()

			return id
		}

		fun randomObjectiveID(): String {
			val array = CharArray(16) { i ->
				if (i < 3) 'U' else {
					val random = Math.random()

					when {
						random < 1 / 3.0 -> Util.randRange(48, 57)
						random < 2 / 3.0 -> Util.randRange(65, 90)
						else -> Util.randRange(97, 122)
					}.toChar()
				}
			}

			array[1] = 'H'
			array[2] = 'C'

			return String(array)
		}

		fun generateTeamEntry(number: Int): String {
			val colorIndex0 = number / ChatColor.values().size
			var colorIndex1 = number % ChatColor.values().size

			return "${ChatColor.values()[colorIndex0]}${ChatColor.values()[colorIndex1]}"
		}
	}
}
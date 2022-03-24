package org.gaseumlabs.uhc.util

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scoreboard.*
import kotlin.random.Random

class ScoreboardDisplay(val name: String, var size: Int) {
	var lines: Array<String>? = null
	val objective: Objective
	val id: String

	val teams: Array<Team>

	init {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

		val objectiveID = generateObjectiveID()
		id = objectiveID.substring(3)
		objective = scoreboard.registerNewObjective(objectiveID, "dummy", Component.text(name))

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
		objective.displayName(Component.text(name))
	}

	fun setLine(line: Int, value: String) {
		teams[line].prefix(Component.text(value))
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
		val chars = ('0'..'9').toList() + ('A'..'Z').toList() + ('a'..'z').toList()

		fun generateObjectiveID(): String {
			val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

			var id = randomObjectiveID()

			while (scoreboard.getObjective(id) != null) id = randomObjectiveID()

			return id
		}

		fun randomObjectiveID(): String {
			val random = Random((Math.random() * Int.MAX_VALUE).toInt())

			return String(CharArray(16) { chars[random.nextInt(0, chars.size)] })
		}

		fun generateTeamEntry(number: Int): String {
			return "${
				ChatColor.values()[
				number / ChatColor.values().size
				]
			}${
				ChatColor.values()[
				number % ChatColor.values().size
				]
			}"
		}
	}
}
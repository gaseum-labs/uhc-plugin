package com.codeland.uhc.team

import org.bukkit.ChatColor
import java.util.*
import kotlin.math.ceil

object TeamMaker {
	/*


	private fun createColorTally(): Array<Int> {
		val ret = Array(ChatColor.values().size) { 0 }

		TeamData.teams.forEach { team ->
			++ret[team.colorPair.color0.ordinal]

			val color1 = team.colorPair.color1
			if (color1 != null) ++ret[color1.ordinal]
		}

		return ret
	}

	private fun iterateTeamColors(colorArray: Array<Int>, onColor: (Int, Int) -> Unit) {
		for (teamColor in TeamData.teamColors) {
			onColor(teamColor.ordinal, colorArray[teamColor.ordinal])
		}
	}

	private fun selectNextColor(tally: Array<Int>): ChatColor {
		var minAmount = 9999
		var minColor = 0

		iterateTeamColors(tally) { ordinal, count ->
			if (count < minAmount) {
				minAmount = count
				minColor = ordinal
			}
		}

		++tally[minColor]
		return ChatColor.values()[minColor]
	}

	/**
	 * will return null if color list could not be created
	 */
	fun getColorList(size: Int): Array<ColorPair>? {
		if (size > TeamData.teamColors.size - TeamData.teams.size)
			return null

		val tally = createColorTally()

		return Array(size) { i ->
			if (Math.random() < 0.5) {
				ColorPair(selectNextColor(tally))
			} else {
				ColorPair(selectNextColor(tally), selectNextColor(tally))
			}
		}
	}
	*/

	fun getTeamsRandom(players: ArrayList<UUID>, teamSize: Int): Array<Array<UUID?>> {
		val used = Array(players.size) { false }

		val numTeams = ceil(players.size / teamSize.toDouble()).toInt()

		return Array(numTeams) {
			Array(teamSize) {
				var index = (Math.random() * players.size).toInt()
				val startIndex = index

				while (used[index]) {
					index = (index + 1) % players.size
					if (index == startIndex) return@Array null
				}
				used[index] = true

				players[index]
			}
		}
	}

	fun randomAvailable(numTeams: Int): List<ColorPair>? {
		return if (numTeams + TeamData.teams.size > TeamData.MAX_TEAMS)
			null
		else
			(0 until TeamData.MAX_TEAMS)
			.map { TeamData.colorPairFromIndex(it) }
			.filter { colorPair -> TeamData.teams.none { it.colorPair == colorPair } }
			.shuffled()
			.subList(0, numTeams)
	}
}

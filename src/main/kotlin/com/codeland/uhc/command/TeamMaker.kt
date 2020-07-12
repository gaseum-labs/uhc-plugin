package com.codeland.uhc.command

import org.bukkit.ChatColor
import org.bukkit.scoreboard.Scoreboard
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*
import kotlin.math.abs

object TeamMaker {
	fun getTeamsRandom(names: ArrayList<String>, teamSize: Int): Array<Array<String?>> {
		val ret = ArrayList<Array<String?>>()
		while (names.size > 0) {
			val tem = arrayOfNulls<String>(teamSize)
			for (i in tem.indices) {
				if (names.size > 0) {
					val rand = (Math.random() * names.size).toInt()
					tem[i] = names.removeAt(rand)
				}
			}
			ret.add(tem)
		}
		return ret.toArray(arrayOf())
	}

	/**
	 * will return null if color list could not be created
	 */
	fun getColorList(size: Int, scoreboard: Scoreboard): Array<ChatColor>? {
		if (size > TeamData.teamColours.size - scoreboard.teams.size)
			return null

		var availableColors = arrayOfNulls<ChatColor>(TeamData.teamColours.size)
		var numAvailableColors = 0

		TeamData.teamColours.forEach { color ->
			if(!scoreboard.teams.any { team ->
				if (color == team.color)
					return@any true

				return@any false
			}) {
				availableColors[numAvailableColors] = color
				++numAvailableColors
			}
		}

		var ret = arrayOfNulls<ChatColor>(size)

		ret.forEachIndexed { i, _ ->
			var position = (Math.random() * numAvailableColors).toInt()
			var usingColor = null as ChatColor?

			while (availableColors[position] == null) {
				++position
				position %= numAvailableColors
			}

			usingColor = availableColors[position]
			availableColors[position] = null
			ret[i] = usingColor
		}

		return ret as Array<ChatColor>
	}

	/* ranked */

	class ScoredPlayer(var name: String, var score: Float, var games: Int) {
		override fun toString(): String {
			return "$name $score $games"
		}
	}

	private val scores = ArrayList<ScoredPlayer>()
	val dataPath = "./scores.txt"

	private fun getScoredPlayer(name: String): ScoredPlayer {
		var scoredPlayer = scores.find { player ->
			player.name == name
		}

		return if (scoredPlayer == null) {
			val ret = ScoredPlayer(name, 0.5f, 0)
			scores.add(ret)

			ret
		} else {
			scoredPlayer
		}
	}

	fun getTeamsRandRanked(names: Array<String>, teamSize: Int, maxDisplace: Float): Array<Array<String>> {
		var defaultPlayer = ScoredPlayer("_ `", 0.5f, 0)
		var tempPlayers = ArrayList<ScoredPlayer>()

		names.forEach { name ->
			var player = getScoredPlayer(name)
			player.score += maxDisplace * (Math.random() * 2 - 1).toFloat()
			tempPlayers.add(player)
		}

		while (tempPlayers.size % teamSize > 0) {
			tempPlayers.add(defaultPlayer)
		}

		var dynamicRet = ArrayList<Array<ScoredPlayer>>()

		while (tempPlayers.size > 0) {
			var team = Array(teamSize) { defaultPlayer }

			for (i in 0..teamSize) {
				team[i] = tempPlayers[0]
				tempPlayers.removeAt(0)
			}

			dynamicRet.add(team)
		}

		while (doImproveRound(dynamicRet));

		var ret = Array(dynamicRet.size) {Array(teamSize) {""} }

		for (i in 0..ret.size)
			for (j in 0..teamSize)
				ret[i][j] = dynamicRet[i][j].name

		return ret
	}

	private fun doImproveRound(teams: ArrayList<Array<ScoredPlayer>>): Boolean {
		var ret = false

		for (i in 0..(teams.size))
			for (j in (i + 1)..(teams.size))
				if (trySwaps(teams[i], teams[j]))
					ret = true

		return ret
	}

	private fun calcScore(team: Array<ScoredPlayer>): Float {
		var ret = 0f
		for (p in team) {
			ret += p.score
		}
		return ret
	}

	private fun trySwaps(teamA: Array<ScoredPlayer>, teamB: Array<ScoredPlayer>): Boolean {
		var tempName = ""
		var tempScore = 0.0f
		var tempRounds = 0

		var teamAScore = calcScore(teamA)
		var teamBScore = calcScore(teamB)

		var mean = teamAScore + teamBScore
		mean /= 2
		var beforeMSE = abs(teamAScore - teamBScore)

		var ret = false

		for (i in 0..teamA.size) {
			for (j in 0..teamB.size) {
				var tempScoreA = teamAScore - teamA[i].score + teamB[j].score
				var tempScoreB = teamBScore - teamB[j].score + teamA[i].score
				var mse = Math.abs(tempScoreA - tempScoreB)

				/* perform swap */
				if (mse < beforeMSE) {
					var temp = teamA[i]
					teamA[i] = teamB[j]
					teamB[j] = temp

					ret = true
					beforeMSE = mse
					teamAScore = tempScoreA
					teamBScore = tempScoreB
				}
			}
		}

		return ret
	}

	fun addGame(losers: Array<String>, winners: Array<String>) {
		var totalPlayers = losers.size + winners.size

		losers.forEachIndexed { i, loser ->
			var player = getScoredPlayer(loser)

			var oldScoreWeight = player.score * player.games
			var newScore = (i + winners.size).toFloat() / totalPlayers
			++player.games
			player.score = (oldScoreWeight + newScore) / player.games
		}

		winners.forEach { winner ->
			var player = getScoredPlayer(winner)

			var oldScoreWeight = player.score * player.games;
			++player.games;
			player.score = oldScoreWeight / player.games;
		}

		saveData()
	}

	fun saveData() {
		var fr = FileWriter(File(dataPath), false);
		if (scores.size > 0) {
			fr.write("${scores[0]}")

			for (i in 1..scores.size)
				fr.write("\n${scores[i]}")
		}

		fr.close()
	}

	fun readData() {
		val file = File(dataPath)

		if (file.exists()) {
			var fr = FileReader(file)

			var lines = fr.readLines()

			lines.forEach { line ->
				var builder = StringBuilder()

				var name = ""
				var score = 0.0f
				var games = 0

				var mode = 0

				line.forEach { current ->
					if (current == ' ') {
						when (mode) {
							0 -> name = builder.toString()
							1 -> score = builder.toString().toFloat()
						}

						builder.clear()
						++mode

					} else {
						builder.append(current)
					}
				}

				games = builder.toString().toInt()

				scores.add(ScoredPlayer(name, score, games))
			}

			fr.close()

		} else {
			FileWriter(file).close()
		}
	}
}
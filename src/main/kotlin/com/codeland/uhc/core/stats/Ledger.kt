package com.codeland.uhc.core.stats

import com.codeland.uhc.core.UHC
import com.codeland.uhc.team.Team
import org.bukkit.Bukkit
import org.bukkit.World
import java.io.File
import java.io.FileWriter
import java.time.ZonedDateTime
import java.util.*
import javax.imageio.ImageIO

class Ledger(val worldSize: Int) {
	data class Entry(val uuid: UUID, val timeSurvived: Int, val killedBy: UUID?)

	val playerList = ArrayList<Entry>()
	val tracker = Tracker()

	fun addEntry(uuid: UUID, timeSurvived: Int, killedBy: UUID?) {
		/* remove any prior entries for this player */
		/* this would only happen if a player dies not as part of the game and is respawned manually */
		playerList.removeIf { entry -> entry.uuid == uuid }
		playerList.add(Entry(uuid, timeSurvived, killedBy))
	}

	/**
	 * winners are inserted first with place 1
	 * they are followed by everyone who has died on the ledger
	 * if two times survived are the same, the places are combined
	 */
	fun createPlayersList(time: Int, winners: List<UUID>): List<Summary.SummaryEntry> {
		/* first procedurally assign places */
		val entries = ArrayList<Summary.SummaryEntry>(playerList.size + winners.size)
		val losers = playerList.sortedByDescending { (_, time) -> time }

		for (i in losers.indices) {
			val (uuid, time, killedBy) = losers[i]

			entries.add(Summary.SummaryEntry(
				if (i > 0 && losers[i - 1].timeSurvived == time) entries.last().place else i + winners.size + 1,
				uuid,
				Bukkit.getOfflinePlayer(uuid).name ?: "Unknown",
				time,
				killedBy
			))
		}

		return winners.map { uuid ->
			Summary.SummaryEntry(
				1,
				uuid,
				Bukkit.getOfflinePlayer(uuid).name ?: "Unknown",
				time,
				null
			)
		}.plus(entries)
	}

	private fun selectSummaryFile(matchNumber: Int = 0): Pair<File, Int> {
		val file = File("./summaries/summary_$matchNumber.json")
		return if (file.exists()) selectSummaryFile(matchNumber + 1) else Pair(file, matchNumber)
	}

	private fun imageFile(environment: World.Environment, matchNumber: Int): File {
		return File("./summaries/summary_${matchNumber}_${environment.name.lowercase()}.png")
	}

	/**
	 * saves a local copy of the summary
	 * also pushes to the bot
	 */
	fun publish(gameType: GameType, startDate: ZonedDateTime, time: Int, teams: List<Team>, winners: List<UUID>) {
		/* get or create summaries directory */
		val directory = File("./summaries")
		if (!directory.exists()) directory.mkdir()

		val (summaryFile, summaryNo) = selectSummaryFile()

		val summary = Summary(gameType, startDate, time, teams, createPlayersList(time, winners))

		val writer = FileWriter(summaryFile)
		writer.write(summary.write(true))
		writer.close()

		ImageIO.write(
			tracker.generateImage(worldSize, World.Environment.NORMAL, 4.0f),
			"PNG",
			imageFile(World.Environment.NORMAL, summaryNo)
		)

		ImageIO.write(
			tracker.generateImage(worldSize, World.Environment.NETHER, 4.0f),
			"PNG",
			imageFile(World.Environment.NETHER, summaryNo)
		)

		UHC.bot?.SummaryManager?.stageSummary(summary)
	}
}

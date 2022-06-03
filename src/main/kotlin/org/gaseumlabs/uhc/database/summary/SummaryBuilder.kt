package org.gaseumlabs.uhc.database.summary

import com.google.gson.GsonBuilder
import org.bukkit.Bukkit
import org.gaseumlabs.uhc.team.Team
import java.io.File
import java.io.FileWriter
import java.time.ZonedDateTime
import java.util.*
import kotlin.random.Random

class SummaryBuilder {
	data class Entry(val uuid: UUID, val timeSurvived: Int, val killedBy: UUID?)

	val entries = ArrayList<Entry>()

	fun addEntry(uuid: UUID, timeSurvived: Int, killedBy: UUID?) {
		/* remove any prior entries for this player */
		/* this would only happen if a player dies not as part of the game and is respawned manually */
		entries.removeIf { entry -> entry.uuid == uuid }
		entries.add(Entry(uuid, timeSurvived, killedBy))
	}

	/**
	 * winners are inserted first with place 1
	 * they are followed by everyone who has died on the ledger
	 * if two times survived are the same, the places are combined
	 */
	private fun createPlayersList(time: Int, winners: List<UUID>): List<Summary.SummaryEntry> {
		/* first procedurally assign places */
		val entries = ArrayList<Summary.SummaryEntry>(entries.size + winners.size)
		val losers = this.entries.sortedByDescending { (_, time) -> time }

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

	fun toSummary(
		gameType: GameType,
		startDate: ZonedDateTime,
		time: Int,
		teams: List<Team>,
		winners: List<UUID>,
	): Summary {
		return Summary(gameType, startDate, time, teams, createPlayersList(time, winners))
	}

	companion object {
		val prettyGson = GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create()
		val sendGson = GsonBuilder().disableHtmlEscaping().create()

		private fun selectSummaryFile(): File {
			val num = Random.nextInt()
			val file = File("./summaries/summary_$num.json")
			return if (file.exists()) selectSummaryFile() else file
		}

		fun saveSummaryLocally(summary: Summary) {
			val folder = File("./summaries")
			if (!folder.exists()) folder.mkdir()

			val file = selectSummaryFile()
			val writer = FileWriter(file)
			writer.write(prettyGson.toJson(summary.serialize()))
			writer.close()
		}
	}
}

package com.codeland.uhc.core

import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime

class Ledger {
	class Entry(val username: String, val timeSurvived: Int, val killedBy: String, val winning: Boolean)

	val list = ArrayList<Entry>()

	fun addEntry(username: String, timeSurvived: Int, killedBy: String?, winning: Boolean = false) {
		/* remove any prior entries for this player */
		/* this would only happen if a player dies not as part of the game and is respawned manually */
		list.removeIf { entry -> entry.username == username }

		list.add(Entry(username, timeSurvived, killedBy ?: "environment", winning))
	}

	fun generateString(): String {
		var ret = ""
		var position = 1

		list.asReversed().forEach { entry ->
			ret += "$position ${entry.username} ${entry.timeSurvived} ${entry.killedBy}\n"

			if (!entry.winning) ++position
		}

		return ret
	}

	fun createTextFile() {
		val directory = File("./summaries")
		if (!directory.exists()) directory.mkdir()

		val date = LocalDateTime.now()
		var matchNumber = 1

		val filePath = { number: Int -> "${directory.path}/${number}_${date.dayOfMonth}_${date.monthValue}_${date.year}.txt"}

		while (File(filePath(matchNumber)).exists()) ++matchNumber

		val writer = FileWriter(File(filePath(matchNumber)))
		writer.write(generateString())
		writer.close()
	}
}

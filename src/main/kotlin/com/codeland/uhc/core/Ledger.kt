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

		var heldPosition = 1
		var position = 1

		var lastWinning = true
		var lastTime = -1

		list.asReversed().forEach { entry ->
			if (lastWinning) {
				heldPosition = if (!entry.winning) position else 1
				lastTime = -1
			} else {
				if (lastTime != entry.timeSurvived)
					heldPosition = position
			}

			ret += "$heldPosition ${entry.username} ${entry.timeSurvived} ${entry.killedBy}\n"

			lastWinning = entry.winning
			lastTime = entry.timeSurvived

			++position
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

	fun makeTest() {
		addEntry("test_user_8",  45, "environment")
		addEntry("test_user_0",  400, "environment")
		addEntry("test_user_5",  400, "environment")
		addEntry("test_user_11", 567, "environment")
		addEntry("test_user_1",  1945, "environment")
		addEntry("test_user_2",  1945, "environment")
		addEntry("test_user_7",  1945, "environment")
		addEntry("test_user_4",  2666, "environment")
		addEntry("test_user_9",  3011, "environment")
		addEntry("test_user_3",  4567, "environment")
		addEntry("test_user_6",  4567, "winning", true)
		addEntry("test_user_10", 4567, "winning", true)
	}
}

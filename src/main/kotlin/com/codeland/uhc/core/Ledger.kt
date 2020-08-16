package com.codeland.uhc.core

import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime

class Ledger {
	class Entry(val username: String, val timeSurvived: Int, val winning: Boolean)

	val list = ArrayList<Entry>()

	fun addEntry(username: String, timeSurvived: Int, winning: Boolean = false) {
		list.add(Entry(username, timeSurvived, winning))
	}

	fun generateString(): String {
		var ret = ""
		var position = 1

		for (i in list.lastIndex downTo 0) {
			ret += "$position ${list[i].username} ${list[i].timeSurvived}\n"

			if (!list[i].winning) ++position
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

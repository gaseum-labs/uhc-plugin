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
			ret += "$position: ${list[i].username} | survived ${Util.timeString(list[i].timeSurvived)}\n"

			if (!list[i].winning) ++position
		}

		return ret
	}

	fun createTextFile() {
		val directory = File("./summaries")
		if (!directory.exists()) directory.mkdir()

		val date = LocalDateTime.now()
		val baseFilepath = "${directory.path}/${date.hour}_${date.dayOfMonth}_${date.month}_${date.year}"

		/* add numbers until file name conflict is resolved */
		val filename = if (File("$baseFilepath.txt").exists()) {
			var count = 0

			while (File("$baseFilepath$count.txt").exists()) ++count

			"$baseFilepath$count.txt"
		} else "$baseFilepath.txt"

		val writer = FileWriter(File(filename))
		writer.write(generateString())
		writer.close()
	}
}

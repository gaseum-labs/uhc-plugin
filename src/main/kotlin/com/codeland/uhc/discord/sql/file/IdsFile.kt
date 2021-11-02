package com.codeland.uhc.discord.sql.file

import com.codeland.uhc.discord.sql.DatabaseFile
import java.sql.ResultSet

class IdsFile : DatabaseFile<IdsFile.Ids, IdsFile.IdsEntry>() {
	class Ids(
		val voiceCategory: Long,
		val generalVoiceChannel: Long,
		val summaryStagingChannel: Long,
		val summaryChannel: Long,
		val adminRole: Long,
	)

	data class IdsEntry(
		val voiceCategory: Long?,
		val generalVoiceChannel: Long?,
		val summaryStagingChannel: Long?,
		val summaryChannel: Long?,
		val adminRole: Long?,
	)

	override fun query(): String {
		return "SELECT TOP 1 voiceCategory, generalVoiceChannel, summaryStagingChannel, summaryChannel, adminRole FROM DiscordData"
	}

	override fun parseResults(results: ResultSet): Ids {
		results.next()

		return Ids(
			results.getLong(0),
			results.getLong(1),
			results.getLong(2),
			results.getLong(3),
			results.getLong(4)
		)
	}

	override fun defaultData(): Ids {
		return Ids(0L, 0L, 0L, 0L, 0L)
	}

	override fun pushQuery(entry: IdsEntry): String {
		val (f0, f1, f2, f3, f4) = entry

		return if (f0 == null && f1 == null && f2 == null && f3 == null && f4 == null) {
			";"
		} else {
			"UPDATE DiscordData SET \n" +
				if (f0 != null) "voiceCategory = $f0,\n" else "" +
				if (f1 != null) "generalVoiceChannel = $f1,\n" else ""+
				if (f2 != null) "summaryStagingChannel = $f2,\n" else ""+
				if (f3 != null) "summaryChannel = $f3,\n" else "" +
				if (f4 != null) "adminRole = $f4;" else ";"
		}
	}
}

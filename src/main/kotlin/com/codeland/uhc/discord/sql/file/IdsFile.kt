package com.codeland.uhc.discord.sql.file

import com.codeland.uhc.discord.sql.DatabaseFile
import java.sql.ResultSet

class IdsFile : DatabaseFile<IdsFile.Ids, IdsFile.IdsEntry>() {
	companion object {
		const val INVALID_ID = 0L
	}

	class Ids(
		var voiceCategory: Long,
		var generalVoiceChannel: Long,
		var summaryStagingChannel: Long,
		var summaryChannel: Long,
		var adminRole: Long,
	)

	data class IdsEntry(
		val voiceCategory: Long? = null,
		val generalVoiceChannel: Long? = null,
		val summaryStagingChannel: Long? = null,
		val summaryChannel: Long? = null,
		val adminRole: Long? = null,
	)

	override fun query(): String {
		//language=sql
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
		return Ids(INVALID_ID, INVALID_ID, INVALID_ID, INVALID_ID, INVALID_ID)
	}

	override fun pushQuery(entry: IdsEntry): String {
		//language=sql
		return "EXECUTE updateDiscordData " +
			"${sqlNullString(entry.voiceCategory)}," +
			"${sqlNullString(entry.generalVoiceChannel)}," +
			"${sqlNullString(entry.summaryStagingChannel)}," +
			"${sqlNullString(entry.summaryChannel)}," +
			"${sqlNullString(entry.adminRole)};"
	}
}

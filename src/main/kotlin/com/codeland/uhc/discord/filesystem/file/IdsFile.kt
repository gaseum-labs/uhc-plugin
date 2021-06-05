package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DiscordFile

class IdsFile(header: String, channelName: String) : DiscordFile<IdsFile.Companion.Ids>(header, channelName) {
	companion object {
		val fieldNames = arrayOf(
			"voiceCategory",
			"generalVoiceChannel",
			"summaryChannel",
		)

		data class Ids(
			var voiceCategoryId: Long,
			var generalVoiceChannelId: Long,
			var summaryChannelId: Long
		)
	}

	override fun fromContents(contents: String): Ids? {
		val lines = contents.lines()
		if (lines.size < 3) return null

		val fieldValues = Array<Long>(fieldNames.size) { 0 }

		for (i in fieldNames.indices) {
			val parts = lines[i].split(',')

			if (parts.size != 2) return null

			val fieldNameIndex = fieldNames.indexOf(parts[0])
			val value = parts[1].toLongOrNull()

			if (fieldNameIndex == -1 || value == null) return null

			fieldValues[fieldNameIndex] = value
		}

		return Ids(fieldValues[0], fieldValues[1], fieldValues[2])
	}

	override fun writeContents(data: Ids): String {
		return "${fieldNames[0]},${data.voiceCategoryId}\n" +
			"${fieldNames[1]},${data.generalVoiceChannelId}\n" +
			"${fieldNames[2]},${data.summaryChannelId}"
	}

	override fun defaultContents(): String {
		return fieldNames.joinToString("\n") { "$it 0" }
	}

	override fun updateContents(dataManager: DataManager, contents: String): Boolean {
		val ids = fromContents(contents)

		return if (ids != null) {
			dataManager.ids = ids
			true

		} else {
			false
		}
	}
}

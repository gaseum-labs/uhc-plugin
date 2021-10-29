package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DiscordFile
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Result
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.io.InputStreamReader

class IdsFile(header: String, channelName: String) : DiscordFile<IdsFile.Companion.Ids>(header, channelName) {
	companion object {
		val fieldNames = arrayOf(
			"voiceCategory",
			"generalVoiceChannel",
			"summaryChannel",
			"adminRole",
			"summaryStagingChannel",
			"summariesChannel"
		)

		data class Ids(
			var voiceCategoryId: Long = -1L,
			var generalVoiceChannelId: Long = -1L,
			var summaryChannelId: Long = -1L,
			var adminRoleId: Long = -1L,
			var summaryStagingChannelId: Long = -1L,
			var summariesChannelId: Long = -1L,
		)
	}

	override fun fromStream(stream: InputStream): Result<Ids> {
		val fieldValues = Array(fieldNames.size) { -1L }

		return try {
			val gson = Gson().fromJson(InputStreamReader(stream), Map::class.java)

			for (i in fieldValues.indices) {
				fieldValues[i] = (gson[fieldNames[i]] as String).toLong()
			}

			for (i in fieldNames.indices) {
				if (fieldValues[i] == -1L) return DiscordFilesystem.fieldError(fieldNames[i], "string")
			}

			Good(Ids(fieldValues[0], fieldValues[1], fieldValues[2], fieldValues[3], fieldValues[4], fieldValues[5]))

		} catch (ex: Exception) {
			Bad(ex.message ?: "Unknown JSON error")
		}
	}

	override fun write(data: Ids): ByteArray {
		val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

		val map = HashMap<String, String>()

		arrayOf(
			data.voiceCategoryId,
			data.generalVoiceChannelId,
			data.summaryChannelId,
			data.adminRoleId,
			data.summaryStagingChannelId,
			data.summariesChannelId
		).forEachIndexed { i, d ->
			map[fieldNames[i]] = d.toString()
		}

		return gson.toJson(map).toByteArray()
	}

	override fun defaultData(): Ids {
		return Ids()
	}
}

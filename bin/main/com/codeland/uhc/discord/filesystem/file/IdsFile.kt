package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DataManager.void
import com.codeland.uhc.discord.filesystem.DiscordFile
import com.google.gson.*
import java.io.InputStream
import java.io.InputStreamReader

class IdsFile(header: String, channelName: String) : DiscordFile<IdsFile.Companion.Ids>(header, channelName) {
	companion object {
		val fieldNames = arrayOf(
			"voiceCategory",
			"generalVoiceChannel",
			"summaryChannel",
			"adminRole"
		)

		data class Ids(
			var voiceCategoryId: Long = -1L,
			var generalVoiceChannelId: Long = -1L,
			var summaryChannelId: Long = -1L,
			var adminRoleId: Long = -1L,
		)
	}

	override fun fromStream(stream: InputStream, onError: (String) -> Unit): Ids? {
		val fieldValues = Array(fieldNames.size) { -1L }

		return try {
			val gson = Gson().fromJson(InputStreamReader(stream), Map::class.java)

			for (i in fieldValues.indices) {
				fieldValues[i] = (gson[fieldNames[i]] as String).toLong()
			}

			for (i in fieldNames.indices) {
				if (fieldValues[i] == -1L) return onError("No value for ${fieldNames[i]} found").void()
			}

			Ids(fieldValues[0], fieldValues[1], fieldValues[2], fieldValues[3])

		} catch (ex: Exception) {
			onError(ex.message ?: "Unknown JSON error").void()
		}
	}

	override fun write(data: Ids): ByteArray {
		val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

		val map = HashMap<String, String>()

		arrayOf(
			data.voiceCategoryId,
			data.generalVoiceChannelId,
			data.summaryChannelId,
			data.adminRoleId
		).forEachIndexed { i, d ->
			map[fieldNames[i]] = d.toString()
		}

		return gson.toJson(map).toByteArray()
	}

	override fun defaultData(): Ids {
		return Ids()
	}
}

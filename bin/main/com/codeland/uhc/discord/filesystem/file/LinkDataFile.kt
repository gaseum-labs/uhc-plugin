package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DataManager.void
import com.codeland.uhc.discord.filesystem.DiscordFile
import com.google.gson.*
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LinkDataFile(header: String, channelName: String) : DiscordFile<LinkDataFile.Companion.LinkData>(header, channelName) {
	companion object {
		data class LinkData(
			val minecraftIds: ArrayList<UUID> = ArrayList(),
			val discordIds: ArrayList<Long> = ArrayList()
		)
	}

	override fun fromStream(stream: InputStream, onError: (String) -> Unit): LinkData? {
		val minecraftIds = ArrayList<UUID>()
		val discordIds = ArrayList<Long>()

		return try {
			val gson = Gson().fromJson(InputStreamReader(stream), ArrayList::class.java) as ArrayList<Map<String, String>>

			gson.forEach { element ->
				minecraftIds.add(UUID.fromString(element["minecraftId"]))
				discordIds.add(element["discordId"]!!.toLong())
			}

			LinkData(minecraftIds, discordIds)

		} catch (ex: Exception) {
			onError(ex.message ?: "Unknown JSON error").void()
		}
	}

	override fun write(data: LinkData): ByteArray {
		val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

		val array = ArrayList<Map<String, String>>(data.discordIds.size)

		data.discordIds.indices.forEach { i ->
			array.add(hashMapOf(
				Pair("minecraftId", data.minecraftIds[i].toString()),
				Pair("discordId", data.discordIds[i].toString())
			))
		}

		return gson.toJson(array).toByteArray()
	}

	override fun defaultData(): LinkData {
		return LinkData(arrayListOf(UUID.randomUUID()), arrayListOf(-1L))
	}
}

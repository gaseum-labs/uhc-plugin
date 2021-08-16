package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DataManager.void
import com.codeland.uhc.discord.filesystem.DiscordFile
import com.google.gson.*
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList

class NicknamesFile(header: String, channelName: String) : DiscordFile<NicknamesFile.Companion.Nicknames>(header, channelName) {
	companion object {
		data class Nicknames(
			val minecraftIds: ArrayList<UUID> = ArrayList(),
			val nicknames: ArrayList<ArrayList<String>> = ArrayList()
		)
	}

	override fun fromStream(stream: InputStream, onError: (String) -> Unit): Nicknames? {
		val minecraftIds = ArrayList<UUID>()
		val nicknames = ArrayList<ArrayList<String>>()

		return try {
			val gson = Gson().fromJson(InputStreamReader(stream), ArrayList::class.java) as ArrayList<Map<String, Any>>

			gson.forEach { element ->
				minecraftIds.add(UUID.fromString(element["id"] as String))
				nicknames.add(element["names"] as ArrayList<String>)
			}

			Nicknames(minecraftIds, nicknames)

		} catch (ex: Exception) {
			onError(ex.message ?: "Unknown JSON error").void()
		}
	}

	override fun write(data: Nicknames): ByteArray {
		val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

		val array = ArrayList<Map<String, Any>>(data.minecraftIds.size)

		data.nicknames.indices.forEach { i ->
			array.add(mapOf(
				Pair("id", data.minecraftIds[i].toString()),
				Pair("names", data.nicknames[i])
			))
		}

		return gson.toJson(array).toByteArray()
	}

	override fun defaultData(): Nicknames {
		return Nicknames(arrayListOf(UUID.randomUUID()), arrayListOf(arrayListOf("(Dummy data, UUID is random)", "name0", "name1", "name2")))
	}
}

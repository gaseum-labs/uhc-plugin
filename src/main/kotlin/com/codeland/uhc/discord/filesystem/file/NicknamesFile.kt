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
import java.util.*

class NicknamesFile(header: String, channelName: String) : DiscordFile<NicknamesFile.Companion.Nicknames>(header, channelName) {
	companion object {
		data class Nicknames(
			val minecraftIds: ArrayList<UUID> = ArrayList(),
			val nicknames: ArrayList<ArrayList<String>> = ArrayList()
		)
	}

	override fun fromStream(stream: InputStream): Result<Nicknames> {
		val minecraftIds = ArrayList<UUID>()
		val nicknames = ArrayList<ArrayList<String>>()

		return try {
			val gson = Gson().fromJson(InputStreamReader(stream), ArrayList::class.java) as ArrayList<Map<String, Any>>

			gson.forEach { element ->
				minecraftIds.add(UUID.fromString(element["id"] as? String ?: return DiscordFilesystem.fieldError("id", "string")))
				nicknames.add(element["names"] as? ArrayList<String> ?: return DiscordFilesystem.fieldError("names", "array"))
			}

			Good(Nicknames(minecraftIds, nicknames))

		} catch (ex: Exception) {
			Bad(ex.message ?: "Unknown JSON error")
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

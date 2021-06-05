package com.codeland.uhc.discord.filesystem

import java.util.*
import kotlin.collections.ArrayList

class NicknamesFile(header: String, channelName: String) : DiscordFile <NicknamesFile.Companion.Nicknames> (header, channelName) {
	companion object {
		data class Nicknames(
			val minecraftIds: ArrayList<UUID> = ArrayList(),
			val nicknames: ArrayList<ArrayList<String>> = ArrayList()
		)
	}

	override fun fromContents(contents: String): Nicknames {
		val lines = contents.lines()

		val minecraftIds: ArrayList<UUID> = ArrayList()
		val nicknames: ArrayList<ArrayList<String>> = ArrayList()

		lines.forEach { line ->
			val parts = line.split(',')

			if (parts.size >= 2) {
				try {
					val uuid = UUID.fromString(parts[0])
					val names = ArrayList<String>(parts.subList(1, parts.size))

					minecraftIds.add(uuid)
					nicknames.add(names)

				} catch (ex: Exception) {}
			}
		}

		return Nicknames(minecraftIds, nicknames)
	}

	override fun writeContents(data: Nicknames): String {
		return data.minecraftIds.indices.joinToString("\n") { i ->
			"${data.minecraftIds[i]},${data.nicknames[i].joinToString(",")}"
		}
	}

	override fun defaultContents(): String {
		return "MINECRAFT_UUID,NICKNAME0,NICKNAME1,NICKNAME2\nMINECRAFT_UUID,NICKNAME0,NICKNAME1\n..."
	}
}

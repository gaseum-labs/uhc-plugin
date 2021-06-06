package com.codeland.uhc.discord.filesystem.file

import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DiscordFile
import java.util.*
import kotlin.collections.ArrayList

class LinkDataFile(header: String, channelName: String) : DiscordFile<LinkDataFile.Companion.LinkData>(header, channelName) {
	companion object {
		data class LinkData(
			val minecraftIds: ArrayList<UUID> = ArrayList(),
			val discordIds: ArrayList<Long> = ArrayList()
		)
	}

	override fun fromContents(contents: String, onError: (String) -> Unit): LinkData {
		val lines = contents.lines()

		val minecraftIds = ArrayList<UUID>()
		val discordIds = ArrayList<Long>()

		lines.forEachIndexed { i, line ->
			val parts = line.split(',')

			if (parts.size == 2) {
				try {
					val uuid = UUID.fromString(parts[0])
					val discordID = parts[1].trim().toLong()

					minecraftIds.add(uuid)
					discordIds.add(discordID)

				} catch (ex: Exception) {
					onError(ex.toString())
				}
			} else {
				onError("There must be exactly one comma on line $i")
			}
		}

		return LinkData(minecraftIds, discordIds)
	}

	override fun writeContents(data: LinkData): String {
		return data.minecraftIds.indices.joinToString("\n") { i ->
			"${data.minecraftIds[i]},${data.discordIds[i]}"
		}
	}

	override fun defaultContents(): String {
		return "MINECRAFT_UUID,DISCORD_ID\nMINECRAFT_UUID,DISCORD_ID\n..."
	}

	override fun defaultData(): LinkData {
		return LinkData(ArrayList(), ArrayList())
	}

	override fun updateContents(dataManager: DataManager, contents: String, onError: (String) -> Unit): Boolean {
		val linkData = fromContents(contents, onError)

		return if (linkData != null) {
			dataManager.linkData = linkData
			true

		} else {
			false
		}
	}
}

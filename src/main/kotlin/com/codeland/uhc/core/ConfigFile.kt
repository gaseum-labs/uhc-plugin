package com.codeland.uhc.core

import com.google.gson.GsonBuilder
import org.bukkit.ChatColor
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ConfigFile(val production: Boolean, val botToken: String, val serverId: Long) {
	companion object {
		const val FILENAME = "./config.json"

		private const val KEY_PRODUCTION = "production"
		private const val KEY_BOT_TOKEN = "botToken"
		private const val KEY_SERVER_ID = "serverId"

		fun load(): ConfigFile {
			return try {
				val fileReader = FileReader(File(FILENAME))

				val parsed = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().fromJson(fileReader.readText(), HashMap::class.java)

				fileReader.close()

				val serverId = (parsed[KEY_SERVER_ID] as String).toLong()

				ConfigFile(
					parsed[KEY_PRODUCTION] as Boolean,
					parsed[KEY_BOT_TOKEN] as String,
					serverId
				)

			} catch (ex: Exception) {
				ex.printStackTrace()
				println("${ChatColor.RED}Config file could not be loaded, creating a default")

				save(false, "Discord bot token goes here", "Discord server id goes here")
				ConfigFile(false, "", 0L)
			}
		}

		fun save(production: Boolean, botToken: String, serverId: String) {
			try {
				val fileWriter = FileWriter(File(FILENAME), false)

				fileWriter.write(
					GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(
						hashMapOf(
							Pair(KEY_PRODUCTION, production),
							Pair(KEY_BOT_TOKEN, botToken),
							Pair(KEY_SERVER_ID, serverId),
						)
					)
				)

				fileWriter.close()

			} catch (ex: Exception) {
				ex.printStackTrace()
				println("${ChatColor.RED}Tried to save config file but couldn't")
			}
		}
	}
}
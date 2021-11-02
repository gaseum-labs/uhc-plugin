package com.codeland.uhc.core

import com.google.gson.GsonBuilder
import org.bukkit.ChatColor
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ConfigFile(
	val production: Boolean,
	val botToken: String,
	val serverId: Long,
	val databaseUrl: String,
	val databaseName: String,
	val databaseUsername: String,
	val databasePassword: String,
) {
	companion object {
		const val FILENAME = "./config.json"

		private const val KEY_PRODUCTION = "production"
		private const val KEY_BOT_TOKEN = "botToken"
		private const val KEY_SERVER_ID = "serverId"

		private const val KEY_DATABASE_URL = "databaseUrl"
		private const val KEY_DATABASE_NAME = "databaseName"
		private const val KEY_DATABASE_USERNAME = "databaseUsername"
		private const val KEY_DATABASE_PASSWORD = "databasePassword"

		fun load(): ConfigFile {
			return try {
				val fileReader = FileReader(File(FILENAME))
				val parsed = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().fromJson(fileReader.readText(), HashMap::class.java)
				fileReader.close()

				ConfigFile(
					parsed[KEY_PRODUCTION] as Boolean,
					parsed[KEY_BOT_TOKEN] as String,
					(parsed[KEY_SERVER_ID] as String).toLong(),
					parsed[KEY_DATABASE_URL] as String,
					parsed[KEY_DATABASE_NAME] as String,
					parsed[KEY_DATABASE_USERNAME] as String,
					parsed[KEY_DATABASE_PASSWORD] as String,
				)

			} catch (ex: Exception) {
				ex.printStackTrace()
				println("${ChatColor.RED}Config file could not be loaded, creating a default")

				ConfigFile(false, "", 0L, "", "", "", "").write()
			}
		}
	}

	fun write(): ConfigFile {
		try {
			val fileWriter = FileWriter(File(FILENAME), false)

			fileWriter.write(
				GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(
					hashMapOf(
						Pair(KEY_PRODUCTION, production),
						Pair(KEY_BOT_TOKEN, botToken),
						Pair(KEY_SERVER_ID, serverId),
						Pair(KEY_DATABASE_URL, databaseUrl),
						Pair(KEY_DATABASE_NAME, databaseName),
						Pair(KEY_DATABASE_USERNAME, databaseUsername),
						Pair(KEY_DATABASE_PASSWORD, databasePassword),
					)
				)
			)

			fileWriter.close()

		} catch (ex: Exception) {
			ex.printStackTrace()
			println("${ChatColor.RED}Tried to save config file but couldn't")
		}

		return this
	}
}
package com.codeland.uhc.core

import com.google.gson.GsonBuilder
import org.bukkit.ChatColor.RED
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
			val file = File(FILENAME)
			if (!file.exists() || file.isDirectory) {
				println("${RED}Config file could not be loaded, creating a default")
				return errorDefault().write()
			}

			return try {
				val reader = FileReader(file)
				val parsed = GsonBuilder()
					.disableHtmlEscaping()
					.create()
					.fromJson(FileReader(file).readText(), HashMap::class.java)
				reader.close()

				fun <T> fieldError(name: String, errorValue: T): T {
					println("${RED}MISSING '${name}' in config file")
					return errorValue
				}

				val production = parsed[KEY_PRODUCTION] as? Boolean
					?: fieldError(KEY_PRODUCTION, false)
				val token = parsed[KEY_BOT_TOKEN] as? String
					?: fieldError(KEY_PRODUCTION, "")
				val serverId = (parsed[KEY_SERVER_ID] as? String)?.toLong()
					?: fieldError(KEY_SERVER_ID, 0L)
				val databaseUrl = parsed[KEY_DATABASE_URL] as? String
					?: fieldError(KEY_DATABASE_URL, "")
				val databaseName = parsed[KEY_DATABASE_NAME] as? String
					?: fieldError(KEY_DATABASE_NAME, "")
				val databaseUsername = parsed[KEY_DATABASE_USERNAME] as? String
					?: fieldError(KEY_DATABASE_USERNAME, "")
				val databasePassword = parsed[KEY_DATABASE_PASSWORD] as? String
					?: fieldError(KEY_DATABASE_PASSWORD, "")

				ConfigFile(production, token, serverId, databaseUrl, databaseName, databaseUsername, databasePassword)

			} catch (ex: Exception) {
				ex.printStackTrace()
				errorDefault()
			}
		}

		fun errorDefault(): ConfigFile {
			return ConfigFile(false, "", 0L, "", "", "", "")
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
			println("${RED}Tried to save config file but couldn't")
		}

		return this
	}
}
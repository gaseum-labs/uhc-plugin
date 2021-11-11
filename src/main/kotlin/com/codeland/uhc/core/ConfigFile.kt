package com.codeland.uhc.core

import com.google.gson.GsonBuilder
import org.bukkit.ChatColor.RED
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ConfigFile(
	val botToken: String? = null,
	val serverId: Long? = null,
	val databaseUrl: String? = null,
	val databaseName: String? = null,
	val databaseUsername: String? = null,
	val databasePassword: String? = null,
	val ddnsUsername: String? = null,
	val ddnsPassword: String? = null,
	val ddnsDomain: String? = null,
) {
	companion object {
		const val FILENAME = "./config.json"

		fun load(): ConfigFile {
			val file = File(FILENAME)
			if (!file.exists() || file.isDirectory) {
				println("${RED}Config file could not be loaded, creating a default")
				return ConfigFile().write()
			}

			return try {
				val reader = FileReader(file)
				val parsed = GsonBuilder()
					.disableHtmlEscaping()
					.create()
					.fromJson(FileReader(file).readText(), ConfigFile::class.java)
				reader.close()
				parsed

			} catch (ex: Exception) {
				ex.printStackTrace()
				ConfigFile()
			}
		}
	}

	fun write(): ConfigFile {
		try {
			val fileWriter = FileWriter(File(FILENAME), false)

			fileWriter.write(
				GsonBuilder()
					.setPrettyPrinting()
					.disableHtmlEscaping()
					.create()
					.toJson(this, ConfigFile::class.java)
			)

			fileWriter.close()

		} catch (ex: Exception) {
			ex.printStackTrace()
			println("${RED}Tried to save config file but couldn't")
		}

		return this
	}
}
package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.util.Util
import com.google.gson.GsonBuilder
import java.io.*

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
				Util.warn("Config file could not be loaded, creating a default")
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
			Util.warn("Tried to save config file but couldn't")
			Util.warn(ex)
		}

		return this
	}
}
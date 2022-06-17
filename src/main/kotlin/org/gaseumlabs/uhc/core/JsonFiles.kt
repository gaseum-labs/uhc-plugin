package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.util.Util
import com.google.gson.GsonBuilder
import java.io.*

object JsonFiles {
	fun <T> load(clazz: Class<T>, filename: String): T {
		val file = File(filename)
		if (!file.exists()) {
			Util.warn("Config file could not be loaded, creating a default")
			return write(newDefault(clazz), filename)
		}

		return try {
			val reader = FileReader(file)
			val parsed = GsonBuilder()
				.disableHtmlEscaping()
				.create()
				.fromJson(FileReader(file).readText(), clazz)
			reader.close()
			parsed

		} catch (ex: Exception) {
			ex.printStackTrace()
			newDefault(clazz)
		}
	}

	private fun <T> newDefault(clazz: Class<T>): T {
		val constructor = clazz.constructors[0]
		return constructor.newInstance() as T
	}

	fun <T> write(file: T, filename: String): T {
		try {
			val fileWriter = FileWriter(File(filename), false)

			fileWriter.write(
				GsonBuilder()
					.setPrettyPrinting()
					.disableHtmlEscaping()
					.create()
					.toJson(file, ConfigFile::class.java)
			)

			fileWriter.close()

		} catch (ex: Exception) {
			Util.warn("Tried to save config file but couldn't")
			Util.warn(ex)
		}

		return file
	}
}

class UHCDbFile(
	val token: String? = null,
)

class ConfigFile(
	val botToken: String? = null,
	val serverId: Long? = null,
	val ddnsUsername: String? = null,
	val ddnsPassword: String? = null,
	val ddnsDomain: String? = null,
	val dbUrl: String? = null,
)

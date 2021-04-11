package com.codeland.uhc.core

import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import java.io.File
import java.io.FileReader
import java.io.FileWriter

enum class WorldGenOption(val propertyName: String, val defaultValue: Any) {
	NETHER_FIX       ("nether-fix", true),
	MUSHROOM_FIX     ("mushroom-fix", true),
	ORE_FIX          ("ore-fix", true),
	MELON_FIX        ("melon-fix", true),
	DUNGEON_FIX      ("dungeon-fix", true),
	SUGAR_CANE_FIX   ("sugar-cane-fix", true),
	NETHER_INDICATORS("nether-indicators", true),
	HALLOWEEN        ("halloween", false),
	CHRISTMAS        ("christmas", false),
	CENTER_BIOME     ("center-biome", "none");

	private var value = defaultValue

	fun bool(): Boolean {
		return value as? Boolean ?: false
	}

	fun <T> get(): T {
		return value as T
	}

	companion object {
		fun readFile(filename: String) {
			val file = File(filename)

			if (file.exists()) {
				val reader = FileReader(file)

				reader.forEachLine { line ->
					if (!line.startsWith("#")) {
						val equalsIndex = line.indexOf('=')
						val propertyName = line.substring(0, equalsIndex).trim()

						val option = values().find { it.propertyName == propertyName }

						if (option != null) {
							val propertyValue = line.substring(equalsIndex + 1)

							option.value = when (option.defaultValue::class) {
								Boolean::class -> propertyValue.toBoolean()
								String::class -> propertyValue.toLowerCase()
								else -> option.value
							}
						}
					}
				}
			} else {
				createDefaultFile(file)
			}
		}

		fun createDefaultFile(file: File) {
			val writer = FileWriter(file)

			writer.write("#UHC World Generator Settings\n#Regenerate the world for changes to take effect\n")

			values().forEach { option ->
				writer.write("${option.propertyName}=${option.defaultValue}\n")
			}

			writer.close()
		}

		fun displayOptions() {
			values().forEach { option ->
				Util.log("${ChatColor.GOLD}${option.propertyName}: ${ChatColor.RED}${option.value}")
			}
		}
	}
}

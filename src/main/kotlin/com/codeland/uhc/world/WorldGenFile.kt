package com.codeland.uhc.world

import java.io.File
import java.io.FileReader
import java.io.FileWriter

object WorldGenFile {
	val filename = "uhc.properties"

	data class WorldGenProperty(val name: String, val defaultEnabled: Boolean)

	val properties = arrayOf(
		WorldGenProperty("nether-fix", true),
		WorldGenProperty("mushroom-fix", true),
		WorldGenProperty("ore-fix", true),
		WorldGenProperty("melon-fix", true),
		WorldGenProperty("halloween", false),
		WorldGenProperty("chunk-swap", false)
	)

	fun getSettings(): Array<Boolean> {
		val file = File(filename)

		return if (file.exists()) {
			val reader = FileReader(file)

			val values = Array(properties.size) { false }

			reader.forEachLine { line ->
				if (!line.startsWith("#")) {
					for (i in properties.indices) {
						if (line.startsWith(properties[i].name)) {
							val equalsIndex = line.indexOf('=')

							if (equalsIndex != -1 && equalsIndex != line.lastIndex)
								values[i] = line.substring(equalsIndex + 1).toBoolean()

							break
						}
					}
				}
			}

			values

		} else {
			createDefaultFile()

			Array(properties.size) { i -> properties[i].defaultEnabled }
		}
	}

	fun createDefaultFile() {
		val writer = FileWriter(File(filename))

		writer.write("#UHC World Generator Settings\n#Regenerate the world for changes to take effect\n")

		properties.forEach { property ->
			writer.write("${property.name}=${property.defaultEnabled}\n")
		}

		writer.close()
	}
}

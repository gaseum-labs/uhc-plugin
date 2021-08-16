package com.codeland.uhc.discord

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object MojangAPI {
	class UUIDResponse(val uuid: String, val name: String) {
		fun convertUuid(): UUID? {
			if (uuid.length != 32) return null

			val mostSignificant = strToLong(uuid, 0) ?: return null
			val leastSignificant = strToLong(uuid, 16) ?: return null

			return UUID(mostSignificant, leastSignificant)
		}

		companion object {
			fun strToLong(hexStr: String, start: Int): Long? {
				var ret = 0L

				for (i in 0 until 16) {
					val char = hexStr[i + start].lowercaseChar()
					val digit = if (char in '0'..'9') char - '0' else if (char in 'a'..'f') char - 'a' + 10 else return null
					ret = digit + ret * 16
				}

				return ret
			}
		}
	}

	/**
	 * string will be null if request could not be made
	 *
	 * string will be blank if the username is invalid
	 *
	 * string will be a uuid otherwise
	 */
	fun getUUIDFromUsername(inputUsername: String): UUIDResponse? {
		val url = URL("https://api.mojang.com/profiles/minecraft")
		val connection = url.openConnection() as HttpURLConnection

		connection.requestMethod = "POST"
		connection.doInput = true
		connection.doOutput = true
		connection.setRequestProperty("Content-Type", "application/json")
		connection.setChunkedStreamingMode(0)
		connection.connect()
		connection.outputStream.write("\"$inputUsername\"".toByteArray())

		if (connection.responseCode == 200) {
			val response = String(connection.inputStream.readBytes())

			val parser = JSONParser()
			val UUIDarray = parser.parse(response) as JSONArray

			if (UUIDarray.isEmpty()) {
				return UUIDResponse("", "")

			} else {
				val userObject = UUIDarray[0] as? JSONObject ?: return null

				val uuid = userObject["id"] as String?
				val name = userObject["name"] as String?

				return UUIDResponse(uuid ?: "", name ?: "")
			}

		} else {
			return null
		}
	}
}

package com.codeland.uhc.discord

import com.codeland.uhc.util.Util
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.net.HttpURLConnection
import java.net.URL

object MojangAPI {
	class UUIDResponse(val uuid: String, val name: String) {
		fun isValid(): Boolean {
			return name != ""
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

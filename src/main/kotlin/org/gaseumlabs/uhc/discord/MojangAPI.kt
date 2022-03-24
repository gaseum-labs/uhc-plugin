package org.gaseumlabs.uhc.discord

import org.gaseumlabs.uhc.util.*
import com.google.gson.GsonBuilder
import java.net.URI
import java.net.http.*
import java.util.*
import java.util.concurrent.*

object MojangAPI {
	class ApiResponse(val name: String, val id: String)

	fun subString16ToLong(hexStr: String, startIndex: Int): Long? {
		var ret = 0L

		for (i in 0 until 16) {
			val char = hexStr[i + startIndex].lowercaseChar()
			val digit = if (char in '0'..'9') char - '0' else if (char in 'a'..'f') char - 'a' + 10 else return null
			ret = digit + ret * 16
		}

		return ret
	}

	fun uuidFromCompact(string: String): UUID? {
		if (string.length != 32) return null

		val mostSignificant = subString16ToLong(string, 0) ?: return null
		val leastSignificant = subString16ToLong(string, 16) ?: return null

		return UUID(mostSignificant, leastSignificant)
	}

	fun getUUIDFromUsername(inputUsername: String): CompletableFuture<Result<Pair<UUID, String>>> {
		return HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build()
			.sendAsync(
				HttpRequest.newBuilder()
					.uri(URI.create("https://api.mojang.com/users/profiles/minecraft/${inputUsername}"))
					.GET().build(),
				HttpResponse.BodyHandlers.ofString()
			).thenApply { response ->
				if (response.statusCode() == 204) return@thenApply Bad("No such player exists")
				if (response.statusCode() != 200) throw Exception()

				val responseObject = try {
					GsonBuilder().create().fromJson(response.body(), ApiResponse::class.java)
				} catch (ex: Exception) {
					throw Exception()
				}

				Good(Pair(uuidFromCompact(responseObject.id) ?: throw Exception(), responseObject.name))
			}
	}
}

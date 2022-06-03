package org.gaseumlabs.uhc.database

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.ConfigFile
import org.gaseumlabs.uhc.core.UHCDbFile
import org.gaseumlabs.uhc.database.summary.Summary
import org.gaseumlabs.uhc.lobbyPvp.Loadouts
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.Util.void
import java.io.*
import java.net.*
import java.net.http.*
import java.net.http.HttpClient.Redirect.ALWAYS
import java.net.http.HttpClient.Version.HTTP_2
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.*
import java.util.concurrent.*

class DataManager(
	val dbUrl: String,
	val client: HttpClient,
	val token: String,
) {
	var online = false

	/* initial poisioned state */
	var poisoned = dbUrl == ""

	val gson = GsonBuilder().create()
	val loadouts = Loadouts(HashMap())
	val nicknames = Nicknames(HashMap())
	val linkData = LinkData(HashMap(), HashMap())

	class OfflineException : Exception()
	class UnauthorizedException : Exception()
	class BadRequestException : Exception()

	init {
		object : Thread() {
			override fun run() {
				while (!poisoned) {
					if (!online) {
						makePing().thenAccept {
							Util.log("Reconnected to UHC database")
							online = true
						}
					}

					sleep(Duration.ofMinutes(5).toMillis())
				}
			}
		}.start()
	}

	fun isLinked(uuid: UUID): Boolean {
		return if (!online) true else linkData.isLinked(uuid)
	}

	fun postRequest(endpoint: String, body: JsonObject? = null): CompletableFuture<HttpResponse<String>> {
		val request = HttpRequest
			.newBuilder()
			.uri(URI.create(dbUrl + endpoint))
			.setHeader("Content-type", "application/json")
			.setHeader("Authorization", "Bearer $token")
			.POST(if (body == null) BodyPublishers.noBody() else BodyPublishers.ofString(gson.toJson(body)))
			.build()

		return client.sendAsync(request, BodyHandlers.ofString()).thenApply { response ->
			when (response.statusCode()) {
				400 -> throw BadRequestException()
				401 -> throw UnauthorizedException()
			}
			response
		}.exceptionally { ex ->
			when (ex.cause) {
				is BadRequestException -> {
					Util.log("Bad request to ${request.uri()}")
				}
				is UnauthorizedException -> {
					Util.log("Not authorized to connect to UHC database, shutting down")
					poisoned = true
					online = false
				}
				else -> {
					Util.log("Lost connection to UHC database")
					online = false
				}
			}
			throw ex
		}
	}

	/* ENDPOINTS */

	fun makePing(): CompletableFuture<HttpResponse<String>> {
		if (poisoned) return CompletableFuture.failedFuture(OfflineException())
		return postRequest("/api/bot/ping")
	}

	fun verifyCode(player: Player, code: String): CompletableFuture<HttpResponse<String>> {
		if (poisoned) return CompletableFuture.failedFuture(OfflineException())

		val body = JsonObject()
		body.addProperty("code", code)
		body.addProperty("uuid", player.uniqueId.toString())
		body.addProperty("username", player.name)

		return postRequest("/api/bot/verifyMinecraftCode", body)
	}

	fun uploadSummary(summary: Summary): CompletableFuture<HttpResponse<String>> {
		if (poisoned) return CompletableFuture.failedFuture(OfflineException())
		return postRequest("/api/bot/summary", summary.serialize())
	}

	companion object {
		fun createDataManager(configFile: ConfigFile, uhcDbFile: UHCDbFile): DataManager {
			val token = uhcDbFile.token ?: ""
			val dbUrl = configFile.dbUrl ?: ""

			if (token == "" || dbUrl == "") {
				Util.warn("Bad input files for datamanager")
			}

			val client = HttpClient
				.newBuilder()
				.followRedirects(ALWAYS)
				.version(HTTP_2)
				.build()

			return DataManager(dbUrl, client, token)
		}
	}
}

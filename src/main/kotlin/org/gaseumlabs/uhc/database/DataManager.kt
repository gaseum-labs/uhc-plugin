package org.gaseumlabs.uhc.database

import com.google.gson.*
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.core.ConfigFile
import org.gaseumlabs.uhc.core.UHCDbFile
import org.gaseumlabs.uhc.database.summary.Summary
import org.gaseumlabs.uhc.lobbyPvp.Loadouts
import org.gaseumlabs.uhc.util.Util
import java.net.*
import java.net.http.*
import java.net.http.HttpClient.Redirect.ALWAYS
import java.net.http.HttpClient.Version.HTTP_2
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import kotlin.collections.HashMap

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
	val linkData = LinkData()

	abstract class ResponseCodeException(val code: Int) : Exception()
	class OfflineException : ResponseCodeException(0)
	class UnauthorizedException : ResponseCodeException(401)
	class BadRequestException : ResponseCodeException(400)
	class NotFoundException : ResponseCodeException(404)

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

	fun postRequest(endpoint: String, body: JsonElement? = null): CompletableFuture<HttpResponse<String>> {
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
				404 -> throw NotFoundException()
			}
			response
		}.exceptionally { ex ->
			when (ex.cause) {
				/* expected errors, nothing bad */
				is BadRequestException -> {
					Util.log("Bad request to ${request.uri()}")
				}
				is NotFoundException -> {
					Util.log("404 request to ${request.uri()}")
				}
				/* catastrophic errors */
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
			throw ex.cause!!
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

	fun getSingleDiscordId(uuid: UUID): CompletableFuture<Long?> {
		val body = JsonObject()
		body.addProperty("uuid", uuid.toString())

		return postRequest("/api/bot/discordId", body).thenApply { response ->
			val responseBody = JsonParser.parseString(response.body()) as JsonObject

			responseBody.get("discordId")?.asString?.toLong()
		}
	}

	fun getMassDiscordIds(uuids: List<UUID>): CompletableFuture<HashMap<UUID, Long?>> {
		val body = JsonArray()
		for (uuid in uuids) body.add(uuid.toString())

		return postRequest("/api/bot/discordIds", body).thenApply { response ->
			val responseBody = JsonParser.parseString(response.body()) as JsonObject
			
			val map = HashMap<UUID, Long?>()
			for ((uuidString, element) in responseBody.entrySet()) {
				map[UUID.fromString(uuidString)] = if (element.isJsonNull) null else element.asString.toLong()
			}

			map
		}
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

		fun clientFacingErrorMessage(ex: Throwable, errorMap: Map<Int, String>, onString: (String) -> Unit): Void? {
			when (val err = ex.cause) {
				is OfflineException -> onString("The server is in offline mode")
				is UnauthorizedException -> onString("Website connection has been disabled on this server")
				is ResponseCodeException -> onString(errorMap[err.code] ?: "Unknown error")
				else -> onString("Unknown error")
			}
			return null
		}
	}
}

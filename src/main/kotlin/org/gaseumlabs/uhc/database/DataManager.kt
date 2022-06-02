package org.gaseumlabs.uhc.database

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.ConfigFile
import org.gaseumlabs.uhc.core.UHCDbFile
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

	val gson = GsonBuilder().create()
	val loadouts = Loadouts(HashMap())
	val nicknames = Nicknames(HashMap())
	val linkData = LinkData(HashMap(), HashMap())

	class OfflineException : Exception()
	class UnauthorizedException : Exception()
	class BadRequestException : Exception()

	val pingThread = object : Thread() {
		fun makePing(): CompletableFuture<Void> {
			return postRequest("/api/bot/ping").thenAccept { response ->
				if (response.statusCode() != 200) throw UnauthorizedException()
			}.exceptionally {
				throw OfflineException()
			}
		}

		override fun run() {
			try {
				makePing().thenAccept {
					Util.log("Connected to UHC database")
					online = true
				}.get()
			} catch (ex: UnauthorizedException) {
				Util.log("Not authorized to connect to UHC database")
				Util.log("Running in offline mode")
				return
			} catch (ex: Exception) {
				Util.log("Could not connect to UHC database, retrying in 5 minutes")
				online = false
			}

			var running = true
			while (running) {
				makePing().thenAccept {
					if (!online) {
						Util.log("Reconnected to UHC database")
						online = true
					}
				}.exceptionally { ex ->
					when (ex) {
						is UnauthorizedException -> {
							Util.log("No longer authorized to connected to UHC database, shutting down connection")
							running = false
						}
						else -> {
							if (online) {
								Util.log("Disconnected from UHC database, retrying in 5 minutes")
								online = false
							}
						}
					}
					null
				}.get()

				sleep(Duration.ofMinutes(5).toMillis())
			}
		}
	}

	init {
		/* only bother pinging if configuration is set up */
		if (dbUrl != "") pingThread.start()
	}

	fun isLinked(uuid: UUID): Boolean {
		return if (!online) true else linkData.isLinked(uuid)
	}

	fun postRequest(endpoint: String, body: JsonObject? = null): CompletableFuture<HttpResponse<String>> {
		val builder = HttpRequest
			.newBuilder()
			.uri(URI.create(dbUrl + endpoint))
			.setHeader("Content-type", "application/json")
			.setHeader("Authorization", "Bearer $token")

		if (body == null) {
			builder.POST(BodyPublishers.noBody())
		} else {
			builder.POST(BodyPublishers.ofString(gson.toJson(body)))
		}

		return client.sendAsync(builder.build(), BodyHandlers.ofString())
	}

	fun verifyCode(player: Player, code: String): CompletableFuture<Void> {
		if (!online) return CompletableFuture.failedFuture(OfflineException())

		val body = JsonObject()
		body.addProperty("code", code)
		body.addProperty("uuid", player.uniqueId.toString())
		body.addProperty("username", player.name)

		return postRequest("/api/bot/verifyMinecraftCode", body).thenAccept { response ->
			if (response.statusCode() != 200) {
				throw BadRequestException()
			}
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
	}
}

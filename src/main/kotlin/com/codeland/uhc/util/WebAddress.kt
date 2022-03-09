package com.codeland.uhc.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.net.http.*
import java.util.concurrent.*

class WebAddress {
	companion object {
		const val WEBSITE = "http://checkip.amazonaws.com"

		var cachedAddress: String? = null
		var cacheTime: Long = 0L

		fun getLocalAddress(): String {
			val time = System.currentTimeMillis()

			/* 10 minutes */
			return if (time - cacheTime >= 600000) {
				val ip = BufferedReader(InputStreamReader(URL(WEBSITE).openStream())).readLine().trim { it <= ' ' }
				cachedAddress = ip
				cacheTime = time
				ip

			} else {
				cachedAddress ?: "unknown ip"
			}
		}

		fun getLocalAddressAsync(): CompletableFuture<String> {
			return HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build()
				.sendAsync(
					HttpRequest.newBuilder().uri(URI.create(WEBSITE)).GET().build(),
					HttpResponse.BodyHandlers.ofString()
				).thenApply { response ->
					if (response.statusCode() != 200) throw Exception("Could not get IP")

					val ip = response.body().lines().first().trim { it <= ' ' }
					cachedAddress = ip
					ip
				}
		}
	}
}

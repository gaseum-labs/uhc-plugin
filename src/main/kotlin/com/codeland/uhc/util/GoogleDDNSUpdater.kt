package com.codeland.uhc.util

import com.codeland.uhc.core.ConfigFile
import java.net.*
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.net.http.HttpClient.Version
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.concurrent.*

object GoogleDDNSUpdater {
	fun sendRequest(
		username: String,
		password: String,
		domain: String,
		newAddress: String,
		oldAddress: String?,
	): CompletableFuture<String> {
		val client = HttpClient
			.newBuilder()
			.version(Version.HTTP_2)
			.followRedirects(Redirect.NORMAL)
			.authenticator(object : Authenticator() {
				override fun getPasswordAuthentication(): PasswordAuthentication {
					return PasswordAuthentication(username, password.toCharArray())
				}
			})
			.build()

		val request = HttpRequest
			.newBuilder()
			.uri(URI.create("https://domains.google.com/nic/update?hostname=${domain}&myip=${newAddress}"))
			.POST(BodyPublishers.noBody())
			.header("User-Agent", "UHCast/1.0")
			.build()

		return client.sendAsync(request, BodyHandlers.ofString()).thenApply { response ->
			if (response.statusCode() != 200) throw Exception(
				"HTTP Request failed | Status Code: ${response.statusCode()}, Body: ${response.body()}"
			)

			"IP updated from $oldAddress to $newAddress | Starting server at $domain"
		}
	}

	fun updateDomain(configFile: ConfigFile): CompletableFuture<String> {
		val username = configFile.ddnsUsername
			?: return CompletableFuture.failedFuture(Exception("No ddnsUsername in config file"))
		val password = configFile.ddnsPassword
			?: return CompletableFuture.failedFuture(Exception("No ddnsPassword in config file"))
		val domain =
			configFile.ddnsDomain ?: return CompletableFuture.failedFuture(Exception("No ddnsDomain in config file"))

		return WebAddress.getLocalAddressAsync().thenApply { newAddress ->
			val oldAddress = InetAddress.getByName(domain)?.hostAddress

			if (newAddress != oldAddress) {
				sendRequest(username, password, domain, newAddress, oldAddress).get()

			} else {
				"No IP update required ($newAddress) | Starting server at $domain"
			}
		}
	}
}

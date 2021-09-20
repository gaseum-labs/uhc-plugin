package com.codeland.uhc.util

import java.io.File
import java.io.FileInputStream
import java.net.Authenticator
import java.net.InetAddress
import java.net.PasswordAuthentication
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect
import java.net.http.HttpClient.Version
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.*

class GoogleDDNSUpdater {
	companion object {
		fun updateDomain(address: String): String {
			val propertiesFile = File("./ddns.properties")
			if (propertiesFile.exists()) {
				val properties = Properties()
				properties.load(FileInputStream(propertiesFile))
				val username = properties.getProperty("username") ?: throw Exception("No DDNS username provided")
				val password = properties.getProperty("password") ?: throw Exception("No DDNS password provided")
				val domain   = properties.getProperty("domain")   ?: throw Exception("No DDNS domain provided")

				val currentAddress = InetAddress.getByName(domain)?.hostAddress

				if (address != currentAddress) {
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
						.uri(URI.create("https://domains.google.com/nic/update?hostname=${domain}&myip=${address}"))
						.POST(BodyPublishers.noBody())
						.header("User-Agent", "UHCast/1.0")
						.build()

					val response = client.send(request, BodyHandlers.ofString())
					if (response.statusCode() != 200) {
						throw Exception("HTTP Request failed | Status Code: ${response.statusCode()}, Body: ${response.body()}")
					}
					return "IP updated to $address | Starting server at $domain"
				} else {
					return "No IP update required | Starting server at $domain"
				}
			} else {
				return "No DDNS properties found. Starting server at $address"
			}
		}
	}
}

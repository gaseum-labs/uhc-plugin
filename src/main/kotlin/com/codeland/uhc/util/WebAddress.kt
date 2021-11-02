package com.codeland.uhc.util

import java.io.*
import java.net.URL

class WebAddress {
	companion object {
		var cachedAddress: String? = null
		var cacheTime: Long = 0L

		fun getLocalAddress(): String {
			val time = System.currentTimeMillis()

			/* 10 minutes */
			return if (time - cacheTime >= 600000) {
				val ip = BufferedReader(InputStreamReader(URL("http://checkip.amazonaws.com").openStream())).readLine().trim { it <= ' ' }
				cachedAddress = ip
				cacheTime = time
				ip

			} else {
				cachedAddress ?: "unknown ip"
			}
		}
	}
}

package com.codeland.uhc.util

import java.io.*
import java.net.URL

class WebAddress {
	companion object {
		fun getLocalAddress(): String {
			val niceWebsite = URL("http://checkip.amazonaws.com")
			val `in` = BufferedReader(InputStreamReader(niceWebsite.openStream()))
			return `in`.readLine().trim { it <= ' ' }		
		}
	}
}

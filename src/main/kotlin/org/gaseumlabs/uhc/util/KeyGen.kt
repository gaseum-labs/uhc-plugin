package org.gaseumlabs.uhc.util

import org.bukkit.NamespacedKey
import org.gaseumlabs.uhc.UHCPlugin

object KeyGen {
	fun genKey(name: String) = NamespacedKey(UHCPlugin.plugin, name)
}

package com.codeland.uhc.core

import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.craftbukkit.v1_16_R3.CraftServer

object MOTD {
	val l = 2770.toChar()
	val r = 2771.toChar()

	fun genMOTD(): String {
		return "$LIGHT_PURPLE${BOLD}$l$RED${BOLD}$l${GOLD}${BOLD}$l${YELLOW}${BOLD}$l UHC ${YELLOW}${BOLD}$r$GOLD${BOLD}$r${RED}${BOLD}$r${LIGHT_PURPLE}${BOLD}$r"
	}

	fun setMOTD() {
		val dedicatedServer = (Bukkit.getServer() as CraftServer).handle.server

		dedicatedServer.motd = genMOTD()
	}
}

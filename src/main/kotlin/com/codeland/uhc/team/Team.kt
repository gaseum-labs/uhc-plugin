package com.codeland.uhc.team

import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class Team(var color: ChatColor, var colorModifer: ChatColor) {
	val members = ArrayList<OfflinePlayer>()

	fun getColorString(): String {
		return "${color}${colorModifer}"
	}

	fun modifyMember(player: Player) {

	}
}

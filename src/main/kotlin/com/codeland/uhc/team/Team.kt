package com.codeland.uhc.team

import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

class Team(var colorPair: ColorPair) {
	val members = ArrayList<UUID>()

	var displayName = colorPair.getName()

	fun isDefaultName(): Boolean {
		return displayName == colorPair.getName()
	}

	companion object {
		fun isValidColor(color: ChatColor): Boolean {
			return color.isColor && color != ChatColor.WHITE && color != ChatColor.BLACK && color != ChatColor.GOLD
		}
	}
}

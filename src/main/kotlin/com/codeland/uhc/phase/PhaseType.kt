package com.codeland.uhc.phase;

import org.bukkit.ChatColor
import org.bukkit.boss.BarColor

enum class PhaseType(val prettyName: String, val hasTimer: Boolean, val gameGoing: Boolean, val barColor: BarColor, val chatColor: ChatColor) {
	 WAITING(   "Waiting lobby", false, false,  BarColor.WHITE, ChatColor.WHITE),
	   GRACE(    "Grace period",  true,  true,   BarColor.BLUE, ChatColor.AQUA),
	  SHRINK("Shrinking period",  true,  true,  BarColor.RED, ChatColor.RED),
	 ENDGAME(         "Endgame", false,  true, BarColor.YELLOW, ChatColor.GOLD),
	POSTGAME(        "Postgame", false, false,  BarColor.WHITE, ChatColor.WHITE);
}

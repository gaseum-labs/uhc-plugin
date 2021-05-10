package com.codeland.uhc.phase;

import net.kyori.adventure.bossbar.BossBar
import org.bukkit.ChatColor

enum class PhaseType(val prettyName: String, val hasTimer: Boolean, val gameGoing: Boolean, val barColor: BossBar.Color, val chatColor: ChatColor) {
	 WAITING(   "Waiting lobby", false, false,  BossBar.Color.WHITE, ChatColor.WHITE),
	   GRACE(    "Grace period",  true,  true,   BossBar.Color.BLUE, ChatColor.AQUA ),
	  SHRINK("Shrinking period",  true,  true,    BossBar.Color.RED, ChatColor.RED  ),
	 ENDGAME(         "Endgame", false,  true, BossBar.Color.YELLOW, ChatColor.GOLD ),
	POSTGAME(        "Postgame", false, false,  BossBar.Color.WHITE, ChatColor.WHITE);
}

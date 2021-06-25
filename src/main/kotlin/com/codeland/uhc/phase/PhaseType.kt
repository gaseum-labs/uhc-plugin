package com.codeland.uhc.phase;

import net.kyori.adventure.bossbar.BossBar
import net.minecraft.world.BossBattle
import org.bukkit.ChatColor

enum class PhaseType(val prettyName: String, val hasTimer: Boolean, val gameGoing: Boolean, val barColor: BossBattle.BarColor, val chatColor: ChatColor) {
	 WAITING(   "Waiting lobby", false, false, BossBattle.BarColor.g, ChatColor.WHITE),
	   GRACE(    "Grace period",  true,  true, BossBattle.BarColor.b, ChatColor.AQUA ),
	  SHRINK("Shrinking period",  true,  true, BossBattle.BarColor.c, ChatColor.RED  ),
	 ENDGAME(         "Endgame", false,  true, BossBattle.BarColor.e, ChatColor.GOLD ),
	POSTGAME(        "Postgame", false, false, BossBattle.BarColor.a, ChatColor.LIGHT_PURPLE);
}

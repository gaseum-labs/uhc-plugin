package com.codeland.uhc.core.phase;

import com.codeland.uhc.gui.ItemCreator
import net.minecraft.world.BossBattle
import org.bukkit.ChatColor
import org.bukkit.Material

enum class PhaseType(
	val prettyName: String,
	val representation: () -> ItemCreator,
	val hasTimer: Boolean,
	val barColor: BossBattle.BarColor,
	val chatColor: ChatColor,
	val description: Array<String>,
) {
	GRACE("Grace period",
		{ ItemCreator.fromType(Material.PAPER) },
		true,
		BossBattle.BarColor.b,
		ChatColor.AQUA,
		arrayOf(
			"Natural regeneration is enabled",
			"Respawning is enabled",
			"Pvp is disabled",
			"Border is static",
		)),
	SHRINK("Shrinking",
		{ ItemCreator.fromType(Material.GLOWSTONE_DUST) },
		true,
		BossBattle.BarColor.c,
		ChatColor.RED,
		arrayOf(
			"Natural regeneration is disabled",
			"If you die you are eliminated",
			"Pvp is enabled",
			"Border closes in",
		)),
	ENDGAME("Endgame",
		{ ItemCreator.fromType(Material.PHANTOM_MEMBRANE) },
		true,
		BossBattle.BarColor.e,
		ChatColor.YELLOW,
		arrayOf(
			"Bedrock floor moves up from below",
			"Skybases are destroyed from above",
			"Fight on the surface",
		)),
	POSTGAME("Postgame",
		{ ItemCreator.fromType(Material.FILLED_MAP) },
		true,
		BossBattle.BarColor.a,
		ChatColor.LIGHT_PURPLE,
		arrayOf(
			"The game has completed",
		)),
}

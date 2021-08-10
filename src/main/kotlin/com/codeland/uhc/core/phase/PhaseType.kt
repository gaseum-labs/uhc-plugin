package com.codeland.uhc.core.phase;

import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.core.phase.phases.Endgame
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.core.phase.phases.Shrink
import net.minecraft.world.BossBattle
import org.bukkit.ChatColor
import org.bukkit.Material

enum class PhaseType(
	val prettyName: String,
	val representation: () -> ItemCreator,
	val hasTimer: Boolean,
	val barColor: BossBattle.BarColor,
	val chatColor: ChatColor,
	val create: () -> Phase,
	val description: Array<String>,
) {
	GRACE("Grace period", { ItemCreator.fromType(Material.PAPER) }, true, BossBattle.BarColor.b, ChatColor.AQUA, ::Grace, arrayOf(
		"Natural regeneration is enabled",
		"Respawning is enabled",
		"Pvp is disabled",
		"Border is static",
	)),
	SHRINK("Shrinking", { ItemCreator.fromType(Material.GLOWSTONE_DUST) }, true, BossBattle.BarColor.c, ChatColor.RED, ::Shrink, arrayOf(
		"Natural regeneration is disabled",
		"If you die you are eliminated",
		"Pvp is enabled",
		"Border closes in",
	)),
	ENDGAME("Endgame", { ItemCreator.fromType(Material.PHANTOM_MEMBRANE) }, true, BossBattle.BarColor.e, ChatColor.YELLOW, ::Endgame, arrayOf(
		"Bedrock floor moves up from below",
		"Skybases are destroyed from above",
		"Fight on the surface",
	)),
	POSTGAME("Postgame", { ItemCreator.fromType(Material.FILLED_MAP) }, true, BossBattle.BarColor.a, ChatColor.LIGHT_PURPLE, ::Postgame, arrayOf(
		"The game has completed",
	)),
}

package com.codeland.uhc.core.phase;

import com.codeland.uhc.component.UHCColor
import com.codeland.uhc.gui.ItemCreator
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.TextColor
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarColor.*
import org.bukkit.Material

enum class PhaseType(
	val prettyName: String,
	val representation: () -> ItemCreator,
	val hasTimer: Boolean,
	val barColor: BossBarColor,
	val color: TextColor,
	val description: Array<String>,
) {
	GRACE("Grace period",
		{ ItemCreator.fromType(Material.PAPER) },
		true,
		BLUE,
		UHCColor.GRACE,
		arrayOf(
			"Natural regeneration is enabled",
			"Respawning is enabled",
			"Pvp is disabled",
			"Border is static",
		)),
	SHRINK("Shrinking",
		{ ItemCreator.fromType(Material.GLOWSTONE_DUST) },
		true,
		RED,
		UHCColor.SHRINK,
		arrayOf(
			"Natural regeneration is disabled",
			"If you die you are eliminated",
			"Pvp is enabled",
			"Border closes in",
		)),
	ENDGAME("Endgame",
		{ ItemCreator.fromType(Material.PHANTOM_MEMBRANE) },
		true,
		YELLOW,
		UHCColor.ENDGAME,
		arrayOf(
			"Bedrock floor moves up from below",
			"Skybases are destroyed from above",
			"Fight on the surface",
		)),
	POSTGAME("Postgame",
		{ ItemCreator.fromType(Material.FILLED_MAP) },
		true,
		PURPLE,
		UHCColor.POSTGAME,
		arrayOf(
			"The game has completed",
		)),
}

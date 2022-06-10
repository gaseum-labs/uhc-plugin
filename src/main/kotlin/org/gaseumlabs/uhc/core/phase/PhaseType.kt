package org.gaseumlabs.uhc.core.phase;

import org.gaseumlabs.uhc.component.UHCColor
import org.gaseumlabs.uhc.gui.ItemCreator
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.TextColor
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarColor.*
import org.bukkit.Material

enum class PhaseType(
	val prettyName: String,
	val barColor: BossBarColor,
	val color: TextColor,
	val description: Array<String>,
) {
	GRACE("Grace period",
		BLUE,
		UHCColor.GRACE,
		arrayOf(
			"Natural regeneration is enabled",
			"Respawning is enabled",
			"Pvp is disabled",
			"Border is static",
		)),
	SHRINK("Shrinking",
		RED,
		UHCColor.SHRINK,
		arrayOf(
			"Natural regeneration is disabled",
			"If you die you are eliminated",
			"Pvp is enabled",
			"Border closes in",
		)),
	BATTLEGROUND("Battleground",
		PINK,
		UHCColor.BATTLEGROUND,
		arrayOf(
			"Small static border",
			"Try to kill each other",
			"Can still collect resources",
		)),
	ENDGAME("Endgame",
		YELLOW,
		UHCColor.ENDGAME,
		arrayOf(
			"Bedrock floor moves up from below",
			"Skybases are destroyed from above",
			"Fight on the surface",
		)),
	POSTGAME("Postgame",
		PURPLE,
		UHCColor.POSTGAME,
		arrayOf(
			"The game has completed",
		)),
}

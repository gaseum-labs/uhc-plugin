package com.codeland.uhc.phase.phases.shrink

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.BOLD
import net.md_5.bungee.api.ChatColor.RESET
import org.bukkit.Bukkit
import org.bukkit.World

class ShrinkDefault : Phase() {
	override fun perSecond(remainingSeconds: Int) {
		uhc.updateMobCaps()
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return if (world.environment == uhc.defaultEnvironment)
			"${RESET}Border radius: ${phaseType.chatColor}${BOLD}${(world.worldBorder.size / 2).toInt()} ${RESET}reaching ${phaseType.chatColor}${BOLD}${uhc.endRadius.toInt()} ${RESET}in ${phaseType.chatColor}${BOLD}${Util.timeString(remainingSeconds)}"
		else
			"${RESET}Dimension closes in ${phaseType.chatColor}${BOLD}${Util.timeString(remainingSeconds)}"	}

	override fun customStart() {
		val world = Util.worldFromEnvironment(uhc.defaultEnvironment)
		world.worldBorder.setSize(uhc.endRadius * 2 + 1.0, length.toLong())

		Bukkit.getOnlinePlayers().forEach { player ->
			GameRunner.sendGameMessage(player, "Grace period has ended!")
			GameRunner.sendGameMessage(player, "The border is now shrinking")
		}
	}

	override fun customEnd() {}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return barLengthRemaining(remainingSeconds, currentTick)
	}

	override fun perTick(currentTick: Int) {}

	override fun endPhrase(): String {
		return "Endgame Starting"
	}
}

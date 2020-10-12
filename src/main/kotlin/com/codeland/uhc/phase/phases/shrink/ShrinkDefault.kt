package com.codeland.uhc.phase.phases.shrink

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor.BOLD
import net.md_5.bungee.api.ChatColor.RESET
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.boss.BossBar

class ShrinkDefault : Phase() {
	override fun perSecond(remainingSeconds: Int) {
		uhc.updateMobCaps()
	}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		if (world.environment == World.Environment.NETHER)
			bossBar.setTitle("${RESET}Nether closes in ${phaseType.chatColor}${BOLD}${Util.timeString(remainingSeconds)}")
		else
			bossBar.setTitle("${RESET}Border radius: ${phaseType.chatColor}${BOLD}${(world.worldBorder.size / 2).toInt()} ${RESET}reaching ${phaseType.chatColor}${BOLD}${uhc.endRadius.toInt()} ${RESET}in ${phaseType.chatColor}${BOLD}${Util.timeString(remainingSeconds)}")
	}

	override fun customStart() {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendGameMessage(player, "Grace period has ended!")
		}

		val world = Bukkit.getServer().worlds[0]
		world.worldBorder.setSize(uhc.endRadius * 2 + 1, length.toLong())

		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendGameMessage(player, "The border is now shrinking")
		}
	}

	override fun customEnd() {}

	override fun onTick(currentTick: Int) {}

	override fun endPhrase(): String {
		return "Endgame Starting"
	}
}

package com.codeland.uhc.phase.phases.shrink

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.boss.BossBar

class ShrinkDefault : Phase() {
	override fun perSecond(remainingSeconds: Int) {
		GameRunner.uhc.updateMobCaps()
	}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		if (world.environment == World.Environment.NETHER)
			bossBar.setTitle("${phaseType.chatColor}${ChatColor.BOLD}Nether closes in ${Util.timeString(remainingSeconds)}")
		else
			bossBar.setTitle("${phaseType.chatColor}${ChatColor.BOLD}Border radius: ${(world.worldBorder.size / 2).toInt()} reaching ${uhc.endRadius.toInt()} in ${Util.timeString(remainingSeconds)}")
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

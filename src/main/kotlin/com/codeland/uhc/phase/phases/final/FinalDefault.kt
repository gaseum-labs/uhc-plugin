package com.codeland.uhc.phase.phases.final

import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.boss.BossBar
import org.bukkit.event.entity.EntityDamageEvent

class FinalDefault : Phase() {
	override fun customStart() {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendGameMessage(player, "The border has stopped shrinking")

			/* kill people in the nether */
			if (player.world.environment == World.Environment.NETHER) {
				Commands.errorMessage(player, "The Nether has closed!")
				player.damage(100000000.0)
			}
		}
	}

	override fun customEnd() {}
	override fun onTick(currentTick: Int) {}
	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		barTimer(bossBar, remainingSeconds, "Endgame starts in")
	}

	override fun endPhrase(): String {
		return "GLOWING WILL BE APPLIED"
	}
}

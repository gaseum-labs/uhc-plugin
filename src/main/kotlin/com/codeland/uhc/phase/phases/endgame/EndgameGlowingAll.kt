package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.boss.BossBar
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EndgameGlowingAll : Phase() {
	override fun customStart() {
		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendGameMessage(player, "Glowing has been applied")
		}

		for (player in Bukkit.getServer().onlinePlayers) {
			if (player.gameMode == GameMode.SURVIVAL) {
				player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, false, false, false))
			}
		}
	}

	override fun customEnd() {}
	override fun onTick(currentTick: Int) {}
	override fun perSecond(remainingSeconds: Int) {}

	override fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int) {
		barStatic(bossBar)
	}

	override fun endPhrase() = ""
}

package com.codeland.uhc.phase.phases.endgame

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phase.Phase
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EndgameGlowingAll : Phase() {
	override fun customStart() {
		EndgameNone.closeNether()

		for (player in Bukkit.getServer().onlinePlayers) {
			GameRunner.sendGameMessage(player, "Glowing has been applied")
		}
	}

	override fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double {
		return 1.0
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String {
		return barStatic()
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		uhc.allCurrentPlayers { uuid ->
			GameRunner.playerAction(uuid) { player ->
				player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, Int.MAX_VALUE, 1, false, false, false))
			}
		}
	}

	override fun endPhrase() = ""
}

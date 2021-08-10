package com.codeland.uhc.core.phase.phases

import com.codeland.uhc.core.Game
import com.codeland.uhc.util.Action
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.md_5.bungee.api.ChatColor.BOLD
import net.md_5.bungee.api.ChatColor.RESET
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Animals
import kotlin.math.abs

class Shrink(game: Game, time: Int) : Phase(PhaseType.SHRINK, time, game) {
	init {
		game.world.worldBorder.setSize(game.config.endgameRadius.get() * 2 + 1.0, length.toLong())
		game.world.worldBorder.damageBuffer = 0.0

		Bukkit.getOnlinePlayers().forEach { player ->
			Action.sendGameMessage(player, "Grace period has ended!")
			Action.sendGameMessage(player, "The border is now shrinking")
		}
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int): String {
		return if (world === game.world)
			"${RESET}Border radius: ${phaseType.chatColor}${BOLD}${(world.worldBorder.size / 2).toInt()} ${RESET}reaching ${phaseType.chatColor}${BOLD}${game.config.endgameRadius.get()} ${RESET}in ${phaseType.chatColor}${BOLD}${Util.timeString(remainingSeconds)}"
		else
			"${RESET}Dimension closes in ${phaseType.chatColor}${BOLD}${Util.timeString(remainingSeconds)}"	}

	override fun updateBarLength(remainingTicks: Int): Float {
		return barLengthRemaining(remainingTicks)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		/* delete animals outside the border so new ones can spawn */
		val overworld = WorldManager.getGameWorld() ?: return

		val killRadius = (overworld.worldBorder.size / 2) + 8

		overworld.entities.forEach { entity ->
			if (
				entity is Animals &&
				(abs(entity.location.x) > killRadius || abs(entity.location.z) > killRadius)
			) {
				entity.remove()
			}
		}
	}

	override fun endPhrase(): String {
		return "Endgame Starting"
	}
}

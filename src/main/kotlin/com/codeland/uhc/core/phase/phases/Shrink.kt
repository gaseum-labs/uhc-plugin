package com.codeland.uhc.core.phase.phases

import com.codeland.uhc.component.*
import com.codeland.uhc.component.UHCComponent.Companion
import com.codeland.uhc.core.Game
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.util.Action
import com.codeland.uhc.util.Util
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

	override fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent {
		val borderRadius = ((game.world.worldBorder.size - 1) / 2).toInt()

		return UHCComponent.text()
			.andSwitch((remainingSeconds / 10) % 3 > 0) {
				Companion.text()
					.andSwitch(world === game.world) {
						Companion.text("Border Radius: ", UHCColor.U_WHITE)
							.and(borderRadius.toString(), phaseType.color, UHCStyle.BOLD)
							.and(" Reaching ", UHCColor.U_WHITE)
							.and(game.config.endgameRadius.get().toString(), phaseType.color, UHCStyle.BOLD)
							.and(" in ", UHCColor.U_WHITE)
							.and(Util.timeString(remainingSeconds), phaseType.color, UHCStyle.BOLD)
					}
					.andSwitch(true) {
						Companion.text("Overworld Border Radius: ", UHCColor.U_WHITE)
							.and(borderRadius.toString(), phaseType.color, UHCStyle.BOLD)
							.and(" Dimension Closes in ", UHCColor.U_WHITE)
							.and(Util.timeString(remainingSeconds), phaseType.color, UHCStyle.BOLD)
					}
			}
			.andSwitch(true) {
				UHCComponent.text("Endgame Y Range ", phaseType.color)
					.and(game.endgameLowY.toString(), phaseType.color, UHCStyle.BOLD)
					.and(" - ", UHCColor.U_WHITE)
					.and(game.endgameHighY.toString(), phaseType.color, UHCStyle.BOLD)
			}
	}

	override fun updateBarLength(remainingTicks: Int): Float {
		return barLengthRemaining(remainingTicks)
	}

	override fun perTick(currentTick: Int) {}

	override fun perSecond(remainingSeconds: Int) {
		/* delete animals outside the border so new ones can spawn */
		val overworld = game.getOverworld()

		val killRadius = (game.world.worldBorder.size / 2) + 8

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

package org.gaseumlabs.uhc.core.phase.phases

import org.gaseumlabs.uhc.component.*
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Animals
import kotlin.math.abs

class Shrink(game: Game, time: Int) : Phase(PhaseType.SHRINK, time, game) {
	init {
		game.world.worldBorder.setSize(game.config.battlegroundRadius.get() * 2.0 + 1.0, length.toLong())
		game.world.worldBorder.damageBuffer = 0.0

		Bukkit.getOnlinePlayers().forEach { player ->
			Action.sendGameMessage(player, "Grace period has ended!")
			Action.sendGameMessage(player, "The border is now shrinking")
		}
	}

	override fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent {
		val borderRadius = ((game.world.worldBorder.size - 1) / 2).toInt()

		return UHCComponent.text("Shrink", phaseType.color)
			.and(" Border Radius ", UHCColor.U_WHITE)
			.and(borderRadius.toString(), phaseType.color, UHCStyle.BOLD)
			.and(" Reaching ", UHCColor.U_WHITE)
			.and(game.config.battlegroundRadius.get().toString(), phaseType.color, UHCStyle.BOLD)
			.and(" in ", UHCColor.U_WHITE)
			.and(Util.timeString(remainingSeconds), phaseType.color, UHCStyle.BOLD)
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
		return "Battleground Starting"
	}
}

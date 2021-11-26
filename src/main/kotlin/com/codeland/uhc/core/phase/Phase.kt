package com.codeland.uhc.core.phase

import com.codeland.uhc.core.Game
import org.bukkit.*
import kotlin.math.ceil

abstract class Phase(val phaseType: PhaseType, val length: Int, val game: Game) {
	var remainingTicks = length * 20

	fun remainingSeconds() = ceil(remainingTicks / 20.0f).toInt()

	/**
	 * @return if the phase should end and the next phase should start
	 */
	fun tick(currentTick: Int): Boolean {
		if (length != 0) --remainingTicks

		perTick(currentTick)

		return if (currentTick % 20 == 0) {
			second()
		} else {
			false
		}
	}

	/**
	 * @return if the phase should end and the next phase should start
	 */
	private fun second(): Boolean {
		/* phases without timer going */
		if (length != 0) {
			if (remainingSeconds() == 0) return true

			if (remainingSeconds() <= 3) Bukkit.getServer().onlinePlayers.forEach { player ->
				player.sendTitle("${countDownColor(remainingSeconds())}${ChatColor.BOLD}${remainingSeconds()}",
					"${phaseType.chatColor}${ChatColor.BOLD}${endPhrase()}",
					0,
					21,
					0)
			}
		}

		perSecond(remainingSeconds())

		return false
	}

	private fun countDownColor(secondsLeft: Int): ChatColor {
		return when (secondsLeft) {
			3 -> ChatColor.RED
			2 -> ChatColor.GREEN
			1 -> ChatColor.BLUE
			else -> ChatColor.GRAY
		}
	}

	/* bar helper functions */
	protected fun barStatic(): String {
		return "${phaseType.chatColor}${ChatColor.BOLD}${phaseType.prettyName}"
	}

	protected fun barLengthRemaining(remainingTicks: Int): Float {
		return remainingTicks / (length * 20.0f)
	}

	/* abstract */

	abstract fun updateBarTitle(world: World, remainingSeconds: Int): String
	abstract fun updateBarLength(remainingTicks: Int): Float

	abstract fun perTick(currentTick: Int)
	abstract fun perSecond(remainingSeconds: Int)

	abstract fun endPhrase(): String
}

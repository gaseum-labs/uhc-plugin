package org.gaseumlabs.uhc.core.phase

import org.gaseumlabs.uhc.component.ComponentAction.uhcTitle
import org.gaseumlabs.uhc.component.UHCColor.CNTDWN0
import org.gaseumlabs.uhc.component.UHCColor.CNTDWN1
import org.gaseumlabs.uhc.component.UHCColor.CNTDWN2
import org.gaseumlabs.uhc.component.UHCColor.U_BLUE
import org.gaseumlabs.uhc.component.UHCComponent
import org.gaseumlabs.uhc.component.UHCStyle.BOLD
import org.gaseumlabs.uhc.core.Game
import net.minecraft.network.chat.*
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
				val color = countDownColor(remainingSeconds())

				player.uhcTitle(
					UHCComponent.text(remainingSeconds().toString(), color, BOLD),
					UHCComponent.text(endPhrase(), phaseType.color, BOLD),
					0, 21, 0
				)
			}
		}

		perSecond(remainingSeconds())

		return false
	}

	private fun countDownColor(secondsLeft: Int): TextColor {
		return when (secondsLeft) {
			3 -> CNTDWN0
			2 -> CNTDWN1
			1 -> CNTDWN2
			else -> U_BLUE
		}
	}

	protected fun barLengthRemaining(remainingTicks: Int): Float {
		return remainingTicks / (length * 20.0f)
	}

	/* abstract */

	abstract fun updateBarTitle(world: World, remainingSeconds: Int): UHCComponent
	abstract fun updateBarLength(remainingTicks: Int): Float

	abstract fun perTick(currentTick: Int)
	abstract fun perSecond(remainingSeconds: Int)

	abstract fun endPhrase(): String
}

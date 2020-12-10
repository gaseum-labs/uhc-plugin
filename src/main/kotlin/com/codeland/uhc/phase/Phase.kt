package com.codeland.uhc.phase

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player

abstract class Phase {
	lateinit var phaseType: PhaseType
	lateinit var phaseVariant: PhaseVariant
	lateinit var uhc: UHC
	var length = 0
	var currentTick = 0
	var remainingSeconds = 0

	var taskID = -1

	fun start(phaseType: PhaseType, phaseVariant: PhaseVariant, uhc: UHC, length: Int, onInject: (Phase) -> Unit) {
		this.phaseType = phaseType
		this.phaseVariant = phaseVariant
		this.uhc = uhc
		this.length = length
		this.currentTick = 0
		this.remainingSeconds = length

		/* pre startup */
		onInject(this)
		customStart()

		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			if (tick()) uhc.startNextPhase()
		}, 0, 1)
	}

	/**
	 * @return if the phase should end and the next phase should start
	 */
	private fun tick(): Boolean {
		DimensionBar.dimensionBars.forEach { dimensionBar ->
			dimensionBar.bossBar.setTitle(updateBarTitle(dimensionBar.world, remainingSeconds, currentTick))
			dimensionBar.bossBar.progress = updateBarLength(remainingSeconds, currentTick)
			dimensionBar.bossBar.color = phaseType.barColor
		}

		Bukkit.getOnlinePlayers().forEach { player ->
			DimensionBar.setPlayerBarDimension(player)
		}

		perTick(currentTick)

		var endResult = if (currentTick == 0) second()
		else false

		/* advance time */

		currentTick = (currentTick + 1) % 20

		if (currentTick == 0) {
			if (length == 0) ++remainingSeconds else --remainingSeconds
			if (phaseType.gameGoing) ++uhc.elapsedTime
		}

		return endResult
	}

	/**
	 * @return if the phase should end and the next phase should start
	 */
	private fun second(): Boolean {
		/* phases without timer going */
		if (length != 0) {
			if (remainingSeconds == 0) return true

			if (remainingSeconds <= 3) Bukkit.getServer().onlinePlayers.forEach { player ->
				player.sendTitle("${countDownColor(remainingSeconds)}${ChatColor.BOLD}$remainingSeconds", "${phaseType.chatColor}${ChatColor.BOLD}${endPhrase()}", 0, 21, 0)
			}
		}

		perSecond(remainingSeconds)

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

	fun updateLength(newLength: Int) {
		length = newLength + 1
		currentTick = 0
		remainingSeconds = newLength
	}

	fun onEnd() {
		Bukkit.getScheduler().cancelTask(taskID)

		customEnd()
	}

	/* bar helper functions */
	protected fun barStatic(): String {
		return "${phaseType.chatColor}${ChatColor.BOLD}${phaseType.prettyName}"
	}

	protected fun barLengthRemaining(remainingSeconds: Int, currentTick: Int): Double {
		return (remainingSeconds - (currentTick / 20.0)) / length.toDouble()
	}

	/* abstract */

	abstract fun customStart()
	abstract fun customEnd()

	abstract fun updateBarLength(remainingSeconds: Int, currentTick: Int): Double
	abstract fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String

	abstract fun perTick(currentTick: Int)
	abstract fun perSecond(remainingSeconds: Int)

	abstract fun endPhrase(): String
}

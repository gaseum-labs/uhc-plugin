package com.codeland.uhc.phases

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.scheduler.BukkitRunnable

abstract class Phase {
	companion object {
		var bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID)
	}

	protected var runnable : BukkitRunnable? = null

	/* default values */

	lateinit var phaseType: PhaseType
	lateinit var uhc: UHC
	var length = 0L

	fun start(phaseType: PhaseType, uhc : UHC, length : Long, onInject: (Phase) -> Unit) {
		this.phaseType = phaseType
		this.uhc = uhc
		this.length = length

		bossBar.progress = 1.0
		bossBar.color = phaseType.color
		bossBar.setTitle(phaseType.prettyName)

		if (length > 0) {
			for (player in Bukkit.getServer().onlinePlayers) {
				bossBar.addPlayer(player)
			}

			runnable = object : BukkitRunnable() {
				var remainingSeconds = length
				var currentTick = 0
				override fun run() {
					if (currentTick == 0) {
						if (remainingSeconds == 0L) {
							Bukkit.getServer().onlinePlayers.forEach { player ->
								player.sendActionBar("")
							}

							uhc.startNextPhase()

							return
						} else {
							updateActionBar(remainingSeconds)
						}

						if (remainingSeconds <= 3) {
							Bukkit.getServer().onlinePlayers.forEach { player ->
								player.sendTitle("${ChatColor.BOLD}${countDownColor(remainingSeconds)}$remainingSeconds", "${ChatColor.BOLD}${endPhrase()}", 0, 21, 0)
							}
						}

						perSecond(remainingSeconds)

						--remainingSeconds
					}

					bossBar.progress = (remainingSeconds.toDouble() + 1 - (currentTick.toDouble() / 20.0)) / length.toDouble()
					currentTick = (currentTick + 1) % 20
				}
			}

			runnable!!.runTaskTimer(GameRunner.plugin, 0, 1)
		}

		onInject(this)

		customStart()
	}

	private fun countDownColor(secondsLeft: Long): ChatColor {
		return when (secondsLeft) {
			3L -> ChatColor.RED
			2L -> ChatColor.GREEN
			1L -> ChatColor.BLUE
			else -> ChatColor.GRAY
		}
	}

	public open fun onEnd() {
		runnable?.cancel()
	}

	protected open fun updateActionBar(remainingSeconds : Long) {
		Bukkit.getServer().onlinePlayers.forEach { player ->
			player.sendActionBar("${ChatColor.GOLD}${ChatColor.BOLD}${getCountdownString()} ${getRemainingTimeString(remainingSeconds)}")
		}
	}

	protected open fun getRemainingTimeString(remainingSeconds : Long) : String {
		var timeRemaining = remainingSeconds
		var units : String =
				if (remainingSeconds >= 60) {
					timeRemaining = timeRemaining / 60 + 1
					" minute"
				} else {
					" second"
				}
		if (timeRemaining > 1) {
			units += "s"
		}
		return timeRemaining.toString() + units
	}

	/* abstract */

	abstract fun customStart()
	protected abstract fun perSecond(remainingSeconds: Long)
	abstract fun getCountdownString() : String
	abstract fun endPhrase() : String
}

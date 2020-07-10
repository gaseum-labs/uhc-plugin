package com.codeland.uhc.phases

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.scheduler.BukkitRunnable

abstract class Phase {

	protected var runnable : BukkitRunnable? = null
	var phaseType = PhaseType.WAITING

	open fun start(uhc : UHC, length : Long) {
		if (length > 0) {

			val bar = Bukkit.createBossBar("$phaseType", BarColor.BLUE, BarStyle.SOLID)
			bar.progress = 1.0

			for (player in Bukkit.getServer().onlinePlayers) {
				bar.addPlayer(player)
			}

			runnable = object : BukkitRunnable() {
				var remainingSeconds = length
				var currentTick = 0
				override fun run() {
					if (currentTick == 0) {
						if (remainingSeconds == 0L) {
							cancel()
							for (player in Bukkit.getServer().onlinePlayers) {
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(""))
							}
							bar.removeAll()//TEST AGAIN ALSO COUNTDOWN TILL START
							onEnd()
							uhc.startNextPhase()
							return
						}
						if (remainingSeconds <= 3) {
							for (onlinePlayer in Bukkit.getServer().onlinePlayers) {
								onlinePlayer.sendTitle("" + remainingSeconds, endPhrase(), 0, 21, 0)
							}
						}
						perSecond(remainingSeconds)
						--remainingSeconds
					}
					bar.progress = (remainingSeconds.toDouble() + 1 - (currentTick.toDouble() / 20.0)) / length.toDouble()
					currentTick = (currentTick + 1) % 20
				}
			}
			runnable!!.runTaskTimer(GameRunner.plugin!!, 0, 1)
		}
	}

	public fun onEnd() {
		runnable?.cancel()
	}

	protected open fun perSecond(second : Long) {
		updateActionBar(second)
	}

	protected open fun updateActionBar(remainingSeconds : Long) {
		val countdownComponent = TextComponent(getCountdownString())
		val remainingTimeComponent = TextComponent(getRemainingTimeString(remainingSeconds))
		remainingTimeComponent.color = ChatColor.GOLD
		remainingTimeComponent.isBold = true
		for (player in Bukkit.getServer().onlinePlayers) {
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, countdownComponent, remainingTimeComponent)
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

	abstract fun getCountdownString() : String

	abstract fun endPhrase() : String
}

// nether
//endgames
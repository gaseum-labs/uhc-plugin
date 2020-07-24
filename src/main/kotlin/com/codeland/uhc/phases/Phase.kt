package com.codeland.uhc.phases

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Boss
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

abstract class Phase {
	companion object {
		class DimensionBar(var bossBar: BossBar, var world: World)

		protected var dimensionBars = emptyArray<DimensionBar>()

		fun createBossBars(worlds: List<World>) {
			dimensionBars = Array(worlds.size) { i -> DimensionBar(Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID), worlds[i]) }
		}

		fun setPlayerBarDimension(player: Player) {
			var world = player.world

			dimensionBars.forEach { dimensionBar ->
				if (world === dimensionBar.world)
					dimensionBar.bossBar.addPlayer(player)
				else
					dimensionBar.bossBar.removePlayer(player)
			}
		}

		fun dimensionOne(player: Player) {
			dimensionBars.forEachIndexed { i, dimensionBar ->
				if (i == 0)
					dimensionBar.bossBar.addPlayer(player)
				else
					dimensionBar.bossBar.removePlayer(player)
			}
		}
	}

	protected var runnable : BukkitRunnable? = null

	/* default values */

	lateinit var phaseType: PhaseType
	lateinit var uhc: UHC
	var length = 0L

	var remainingSeconds = length

	fun start(phaseType: PhaseType, uhc: UHC, length: Long, onInject: (Phase) -> Unit) {
		this.phaseType = phaseType
		this.uhc = uhc
		this.length = length
		this.remainingSeconds = length

		dimensionBars.forEach { dimensionBar ->
			dimensionBar.bossBar.progress = 1.0
			dimensionBar.bossBar.color = phaseType.color
			dimensionBar.bossBar.setTitle("${ChatColor.GOLD}${ChatColor.BOLD}${phaseType.prettyName}")
		}

		if (length > 0) {
			runnable = object : BukkitRunnable() {
				var currentTick = 0

				override fun run() {
					if (currentTick == 0) {
						if (remainingSeconds == 0L) {
							uhc.startNextPhase()

							return
						}

						Bukkit.getOnlinePlayers().forEach { player ->
							setPlayerBarDimension(player)
						}

						dimensionBars.forEach { dimensionBar ->
							updateActionBar(dimensionBar.bossBar, dimensionBar.world, remainingSeconds)
						}

						if (remainingSeconds <= 3) {
							Bukkit.getServer().onlinePlayers.forEach { player ->
								player.sendTitle("${countDownColor(remainingSeconds)}${ChatColor.BOLD}$remainingSeconds", "${ChatColor.GOLD}${ChatColor.BOLD}${endPhrase()}", 0, 21, 0)
							}
						}

						perSecond(remainingSeconds)

						--remainingSeconds
					}

					dimensionBars.forEach { dimensionBar ->
						dimensionBar.bossBar.progress = (remainingSeconds.toDouble() + 1 - (currentTick.toDouble() / 20.0)) / length.toDouble()
					}

					currentTick = (currentTick + 1) % 20
				}
			}

			runnable!!.runTaskTimer(GameRunner.plugin, 0, 1)

		} else {
			Bukkit.getOnlinePlayers().forEach { player ->
				dimensionOne(player)
			}
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

	protected open fun updateActionBar(bossBar: BossBar, world: World, remainingSeconds : Long) {
		bossBar.setTitle("${ChatColor.GOLD}${ChatColor.BOLD}${getCountdownString()} ${getRemainingTimeString(remainingSeconds)}")
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

	public fun getTimeRemaining(): Long {
		return remainingSeconds
	}

	/* abstract */

	abstract fun customStart()
	protected abstract fun perSecond(remainingSeconds: Long)
	abstract fun getCountdownString() : String
	abstract fun endPhrase() : String
}

package com.codeland.uhc.phases

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.util.Util
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
			dimensionBars = Array(worlds.size) { i ->
				val key = NamespacedKey(GameRunner.plugin, "B$i")

				DimensionBar(Bukkit.getBossBar(key) ?: Bukkit.createBossBar(key, "",  BarColor.WHITE, BarStyle.SOLID), worlds[i])
			}
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

	private var taskID = -1

	/* default values */

	lateinit var phaseType: PhaseType
	lateinit var phaseVariant: PhaseVariant
	lateinit var uhc: UHC
	var length = 0

	var remainingSeconds = length

	fun start(phaseType: PhaseType, phaseVariant: PhaseVariant, uhc: UHC, length: Int, onInject: (Phase) -> Unit) {
		this.phaseType = phaseType
		this.phaseVariant = phaseVariant
		this.uhc = uhc
		this.length = length
		this.remainingSeconds = length

		dimensionBars.forEach { dimensionBar ->
			dimensionBar.bossBar.progress = 1.0
			dimensionBar.bossBar.color = phaseType.color
			dimensionBar.bossBar.setTitle("${ChatColor.GOLD}${ChatColor.BOLD}${phaseType.prettyName}")
		}

		Bukkit.getOnlinePlayers().forEach { player ->
			dimensionOne(player)
		}

		var currentTick = 0

		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameRunner.plugin, {
			if (length > 0) {
				if (currentTick == 0) {
					if (remainingSeconds == 0) return@scheduleSyncRepeatingTask uhc.startNextPhase()

					Bukkit.getOnlinePlayers().forEach { player ->
						setPlayerBarDimension(player)
					}

					dimensionBars.forEach { dimensionBar ->
						updateActionBar(dimensionBar.bossBar, dimensionBar.world, remainingSeconds)
					}

					if (remainingSeconds <= 3)
						Bukkit.getServer().onlinePlayers.forEach { player ->
							player.sendTitle("${countDownColor(remainingSeconds)}${ChatColor.BOLD}$remainingSeconds", "${ChatColor.GOLD}${ChatColor.BOLD}${endPhrase()}", 0, 21, 0)
						}

					perSecond(remainingSeconds)

					--remainingSeconds
					++uhc.elapsedTime
				}

				dimensionBars.forEach { dimensionBar ->
					dimensionBar.bossBar.progress = (remainingSeconds.toDouble() + 1 - (currentTick.toDouble() / 20.0)) / length.toDouble()
				}
			}

			currentTick = (currentTick + 1) % 20

			onTick(currentTick)
		}, 0, 1)

		onInject(this)

		customStart()
	}

	private fun countDownColor(secondsLeft: Int): ChatColor {
		return when (secondsLeft) {
			3 -> ChatColor.RED
			2 -> ChatColor.GREEN
			1 -> ChatColor.BLUE
			else -> ChatColor.GRAY
		}
	}

	fun onEnd() {
		Bukkit.getScheduler().cancelTask(taskID)

		customEnd()
	}

	open fun updateActionBar(bossBar: BossBar, world: World, remainingSeconds: Int) {
		bossBar.setTitle("${ChatColor.GOLD}${ChatColor.BOLD}${getCountdownString()} ${getRemainingTimeString(remainingSeconds)}")
	}

	protected open fun getRemainingTimeString(seconds: Int) : String {
		var time: Int

		var units = if (seconds >= 60) {
			time = seconds / 60 + 1
			"minute"
		} else {
			time = seconds
			"second"
		}

		if (time > 1) units += "s"

		return "$time $units"
	}

	/* abstract */

	abstract fun customStart()
	abstract fun customEnd()
	abstract fun onTick(currentTick: Int)

	abstract fun perSecond(remainingSeconds: Int)
	abstract fun getCountdownString() : String
	abstract fun endPhrase() : String
}

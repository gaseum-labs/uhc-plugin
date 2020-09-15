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
	companion object {
		class DimensionBar(var bossBar: BossBar, var world: World)

		protected var dimensionBars = emptyArray<DimensionBar>()

		fun createBossBars(worlds: List<World>) {
			dimensionBars = Array(worlds.size) { i ->
				val key = NamespacedKey(UHCPlugin.plugin, "B$i")

				DimensionBar(Bukkit.getBossBar(key) ?: Bukkit.createBossBar(key, "",  BarColor.WHITE, BarStyle.SOLID), worlds[i])
			}
		}

		fun setPlayerBarDimension(player: Player) {
			var world = player.world

			dimensionBars.forEach { dimensionBar ->
				if (world === dimensionBar.world) dimensionBar.bossBar.addPlayer(player)
				else dimensionBar.bossBar.removePlayer(player)
			}
		}

		fun dimensionOne(player: Player) {
			dimensionBars.forEachIndexed { i, dimensionBar ->
				if (i == 0) dimensionBar.bossBar.addPlayer(player)
				else dimensionBar.bossBar.removePlayer(player)
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
			dimensionBar.bossBar.color = phaseType.barColor
			updateBarPerSecond(dimensionBar.bossBar, dimensionBar.world, remainingSeconds)
		}

		Bukkit.getOnlinePlayers().forEach { player ->
			dimensionOne(player)
		}

		var currentTick = 0

		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			if (currentTick == 0) {
				/* for phases that have a timer */
				if (length != 0) {
					if (remainingSeconds == 0) return@scheduleSyncRepeatingTask uhc.startNextPhase()

					if (remainingSeconds <= 3) Bukkit.getServer().onlinePlayers.forEach { player ->
						player.sendTitle("${countDownColor(remainingSeconds)}${ChatColor.BOLD}$remainingSeconds", "${phaseType.chatColor}${ChatColor.BOLD}${endPhrase()}", 0, 21, 0)
					}

					--remainingSeconds
				}

				/* general per second for all phases regardless of having a timer */

				dimensionBars.forEach { dimensionBar ->
					updateBarPerSecond(dimensionBar.bossBar, dimensionBar.world, remainingSeconds)
				}

				perSecond(remainingSeconds)
				if (phaseType.gameGoing) ++uhc.elapsedTime
			}

			/* bar progress section */

			val progress = if (length == 0) 1.0
			else (remainingSeconds + 1.0 - (currentTick / 20.0)) / length

			dimensionBars.forEach { dimensionBar ->
				dimensionBar.bossBar.progress = progress
			}

			Bukkit.getOnlinePlayers().forEach { player ->
				setPlayerBarDimension(player)
			}

			/* every tick determine the subtick within the second */

			currentTick = (currentTick + 1) % 20
			onTick(currentTick)
		}, 20, 1)

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

	/* helpers for update bar per second */

	protected fun barTimer(bossBar: BossBar, remainingSeconds: Int, countdownString: String) {
		bossBar.setTitle("${phaseType.chatColor}${ChatColor.BOLD}$countdownString ${Util.timeString(remainingSeconds)}")
	}

	protected fun barStatic(bossBar: BossBar) {
		bossBar.setTitle("${phaseType.chatColor}${ChatColor.BOLD}${phaseType.prettyName}")
	}

	/* abstract */

	abstract fun customStart()
	abstract fun customEnd()
	abstract fun onTick(currentTick: Int)

	abstract fun perSecond(remainingSeconds: Int)
	abstract fun updateBarPerSecond(bossBar: BossBar, world: World, remainingSeconds: Int)
	abstract fun endPhrase() : String
}

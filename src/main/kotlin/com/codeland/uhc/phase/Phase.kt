package com.codeland.uhc.phase

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.UHCBar
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.minecraft.world.BossBattle
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World

abstract class Phase {
	lateinit var phaseType: PhaseType
	lateinit var phaseVariant: PhaseVariant
	var length = 0
	var currentTick = 0
	var remainingSeconds = 0

	var taskID = -1

	fun start(phaseType: PhaseType, phaseVariant: PhaseVariant, length: Int, onInject: (Phase) -> Unit) {
		this.phaseType = phaseType
		this.phaseVariant = phaseVariant
		this.length = length
		this.currentTick = 0
		this.remainingSeconds = length

		/* pre startup */
		onInject(this)
		customStart()

		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			if (tick()) UHC.startNextPhase()
		}, 0, 1)
	}

	/**
	 * @return if the phase should end and the next phase should start
	 */
	private fun tick(): Boolean {
		currentTick = (currentTick + 1) % 20

		if (currentTick == 0) {
			if (length == 0) ++remainingSeconds else --remainingSeconds
			if (phaseType.gameGoing) ++UHC.elapsedTime
		}

		/* update boss bars */
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			val player = Bukkit.getPlayer(uuid)

			if (player != null) {
				val game = PvpGameManager.playersGame(uuid)

				when {
					game != null -> {
						UHCBar.updateBossBar(
							player,
							if (game.isOver()) {
								"${ChatColor.RED}Game Over"
							} else {
								"${ChatColor.RED}${PvpGameManager.typeName(game.type)} PVP | " +
								if (game.shouldGlow()) {
									"${ChatColor.GOLD}Glowing"
								} else {
									"Glowing in ${Util.timeString(game.glowTimer)}"
								}
							},
							if (game.glowPeriod == 0 || game.glowTimer <= 0) {
								1.0f
						    } else {
								1.0f - (game.glowTimer.toFloat() / game.glowPeriod)
				            },
							BossBattle.BarColor.c
						)
					}
					player.world.name == WorldManager.LOBBY_WORLD_NAME -> {
						val phaseType = UHC.currentPhase?.phaseType ?: PhaseType.WAITING

						UHCBar.updateBossBar(
							player,
							"${ChatColor.WHITE}Waiting Lobby" +
							if (phaseType != PhaseType.WAITING) {
								" | ${phaseType.chatColor}Game Ongoing: ${phaseType.prettyName}"
							} else {
								""
							},
							updateBarLength(remainingSeconds, currentTick),
							BossBattle.BarColor.g
						)
					}
					else -> {
						UHCBar.updateBossBar(
							player,
							updateBarTitle(player.world, remainingSeconds, currentTick),
							updateBarLength(remainingSeconds, currentTick),
							phaseType.barColor
						)
					}
				}
			}
		}

		perTick(currentTick)

		return if (currentTick == 0) {
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
	}

	/* bar helper functions */
	protected fun barStatic(): String {
		return "${phaseType.chatColor}${ChatColor.BOLD}${phaseType.prettyName}"
	}

	protected fun barLengthRemaining(remainingSeconds: Int, currentTick: Int): Float {
		return (remainingSeconds - (currentTick / 20.0f)) / length.toFloat()
	}

	/* abstract */

	abstract fun customStart()

	abstract fun updateBarLength(remainingSeconds: Int, currentTick: Int): Float
	abstract fun updateBarTitle(world: World, remainingSeconds: Int, currentTick: Int): String

	abstract fun perTick(currentTick: Int)
	abstract fun perSecond(remainingSeconds: Int)

	abstract fun endPhrase(): String
}

package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.collections.ArrayList
import kotlin.math.ceil

class Deathswap(type: QuirkType, game: Game) : Quirk(type, game) {
	init {
		resetTimer()
		taskId = SchedulerUtil.everyTick(::runTask)
	}

	override fun customDestroy() {
		Bukkit.getScheduler().cancelTask(taskId)
	}

	override fun onPhaseSwitch(phase: Phase) {
		if (phase is Postgame) Bukkit.getScheduler().cancelTask(taskId)
	}

	companion object {
		var WARNING = 10 * 20
		var IMMUNITY = 10 * 20

		var taskId = 0
		var swapTime = 0

		val random = Random()

		const val MIN_TIME = 120
		const val MAX_TIME = 300

		fun doSwaps(game: Game) {
			val used = ArrayList<Boolean>()
			val players = ArrayList<UUID>()
			val locations = ArrayList<Location>()

			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if (playerData.participating && playerData.alive) {
					used.add(false)
					players.add(uuid)
					locations.add(GameRunner.getPlayerLocation(uuid) ?: game.spectatorSpawnLocation())
				}
			}

			if (players.size > 1) {
				players.forEachIndexed { i, uuid ->
					var index = Util.randRange(0, locations.lastIndex)

					while (used[index] || index == i) index = (index + 1) % used.size

					used[index] = true
					GameRunner.teleportPlayer(uuid, locations[index])
				}
			}
		}

		fun resetTimer() {
			swapTime = ((ThreadLocalRandom.current().nextInt() % (MAX_TIME - MIN_TIME + 1) + MIN_TIME) * 20 + IMMUNITY)
		}

		private fun runTask() {
			--swapTime

			when {
				swapTime <= 0 -> {
					resetTimer()
					sendAll("${ChatColor.GOLD}You are no longer immune.", false)
				}
				swapTime < IMMUNITY -> {
					sendAll(generateImmunity(swapTime / IMMUNITY.toFloat()), false)
				}
				swapTime < IMMUNITY + WARNING -> {
					sendAll("${ChatColor.GOLD}Swapping in ${ChatColor.BLUE}${ceil((swapTime - IMMUNITY) / 20.0).toInt()}...", true)
				}
				else -> {
					sendAll("${ChatColor.GOLD}(d3bug) Swapping in ${ChatColor.BLUE}${ceil((swapTime - IMMUNITY) / 20.0).toInt()}...", true)
				}
			}
		}

		private fun sendAll(message: String, allowSpecs: Boolean) {
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if ((playerData.participating && playerData.alive) || allowSpecs)
					Bukkit.getPlayer(uuid)?.sendActionBar(message)
			}
		}

		private fun generateImmunity(percent: Float): String {
			val message = StringBuilder("${ChatColor.GOLD}Immune ${ChatColor.GRAY}- ")

			for (i in 0 until ceil(10 * percent).toInt())
				message.append(ChatColor.GOLD.toString() + "▮")

			for (i in 0 until 10 - ceil(10 * percent).toInt())
				message.append(ChatColor.GRAY.toString() + "▮")

			return message.toString()
		}
	}
}

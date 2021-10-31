package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.util.Action
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.UUID
import kotlin.math.ceil
import kotlin.random.Random

class Deathswap(type: QuirkType, game: Game) : Quirk(type, game) {
	init {
		resetTimer()
		taskId = SchedulerUtil.everyN(20, ::runTask)
	}

	override fun customDestroy() {
		Bukkit.getScheduler().cancelTask(taskId)
	}

	override fun onPhaseSwitch(phase: Phase) {
		if (phase is Postgame) Bukkit.getScheduler().cancelTask(taskId)
	}

	fun doSwaps() {
		val players = PlayerData.playerDataList
				.filter { it.value.participating && it.value.alive }
				.keys.toList()
		if (players.size < 2) return
		var playersShuffled: List<UUID>
		// make sure everyone teleports to a different location
		// takes on average 2.71 (e?) trials to ensure this
		do {
			playersShuffled = players.shuffled()
		} while (players.zip(playersShuffled).any { it.first == it.second })
		players
				.zip(playersShuffled.map { Action.getPlayerLocation(it) ?: game.spectatorSpawnLocation() })
				.forEach { (uuid, location) -> Action.teleportPlayer(uuid, location)
        }
	}

	fun resetTimer() {
		untilNextSequence = Random.nextInt(MIN_TIME, MAX_TIME) + IMMUNITY
		swapped = false
	}

	private fun runTask() {
		--untilNextSequence

		when {
			untilNextSequence <= 0 -> {
				resetTimer()
				sendAll("${ChatColor.GOLD}You are no longer immune.", false)
			}
			untilNextSequence <= IMMUNITY -> {
				if (!swapped) {
					doSwaps()
					swapped = true
				}
				sendAll(generateImmunity(untilNextSequence / IMMUNITY.toFloat()), false)
			}
			untilNextSequence <= IMMUNITY + WARNING -> {
				sendAll("${ChatColor.GOLD}Swapping in ${ChatColor.BLUE}${untilNextSequence - IMMUNITY}...", true)
			}
		}
	}

	private fun sendAll(message: String, allowSpecs: Boolean) {
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if ((playerData.participating && playerData.alive) || allowSpecs)
				Bukkit.getPlayer(uuid)?.sendActionBar(message)
		}
	}

	// TODO: componentize
	private fun generateImmunity(percent: Float): String {
		val bars = "▮".repeat(10)
		val nGold = ceil(percent * 10).toInt()
		val coloredBars =
				ChatColor.GOLD.toString() + bars.subSequence(0, nGold) +
			  	ChatColor.GRAY.toString() + bars.subSequence(nGold, bars.length)

		return "${ChatColor.GOLD}Immune ${ChatColor.GRAY}- $coloredBars"
	}

	companion object {
		const val WARNING = 5
		const val IMMUNITY = 10

		var taskId = 0
		var untilNextSequence = 0
		var swapped = false

		const val MIN_TIME = 120
		const val MAX_TIME = 300
	}
}

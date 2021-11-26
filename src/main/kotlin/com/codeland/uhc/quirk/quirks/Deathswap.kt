package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.Action
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*
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

	/**
	 * Return a random permutation of the given list with the property
	 * that no element is in the same position as it was before.
	 */
	fun <T> derangement(list: List<T>): List<T> {
		if (list.size < 2) throw IllegalArgumentException("The list can't have one or zero elements.")
		var res: List<T>
		do {
			res = list.shuffled()
		} while (res.zip(list).any { it.first == it.second })
		return res
	}

	/**
	 * Zip two lists with duplicate elements used if necessary.
	 *
	 * For example, `unbalancedZip(listOf(1, 2, 3), listOf(4, 5)) == listOf((1, 4), (2, 5), (3, 4))`
	 *
	 * @see List.zip
	 */
	fun <T, R> unbalancedZip(list1: List<T>, list2: List<R>): List<Pair<T, R>> {
		return if (list1.size <= list2.size) {
			list1.zip(list2)
		} else {
			unbalancedZip(list1, list2.plus(list2))
		}
	}

	fun doSwaps() {
		val teams = game.teams.teams()
		if (teams.size < 2) return
		val shuffledTeams = derangement(teams)
		teams.zip(shuffledTeams).forEach {
			fun getSwapPositions(team: List<UUID>) =
				team.map(Action::getPlayerLocation).map { it ?: game.spectatorSpawnLocation() }

			val team1 = it.first.members
			val team2 = it.second.members
			val team1Locations = getSwapPositions(team1)
			val team2Locations = getSwapPositions(team2)

			unbalancedZip(team1.shuffled(), team2Locations.shuffled()).forEach { (player, location) ->
				Action.teleportPlayer(player, location)
			}
			unbalancedZip(team2.shuffled(), team1Locations.shuffled()).forEach { (player, location) ->
				Action.teleportPlayer(player, location)
			}
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

package com.codeland.uhc.core

import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.core.phase.phases.Endgame
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.core.phase.phases.Shrink
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import java.time.Duration
import java.util.*
import kotlin.math.roundToInt

class Game(val config: GameConfig, val initialRadius: Int, val world: World) {
	var phase = getPhase(PhaseType.GRACE)

	var naturalRegeneration = UHCProperty(false)

	var quirks = Array(QuirkType.values().size) { i ->
		if (config.quirksEnabled[i].get()) {
			QuirkType.values()[i].createQuirk(this)
		} else {
			null
		}
	}
	init {
		config.quirksEnabled.forEachIndexed { i, property ->
			property.watch {
				quirks[i] = if (property.get()) {
					QuirkType.values()[i].createQuirk(this)
				} else {
					null
				}
			}
		}
	}

	val ledger = Ledger(initialRadius)

	/* getters */

	fun getQuirk(quirkType: QuirkType): Quirk? {
		return quirks[quirkType.ordinal]
	}

	fun isOver(): Boolean {
		return phase is Postgame
	}

	/* flow */

	fun startPlayer(uuid: UUID, location: Location) {
		val playerData = PlayerData.getPlayerData(uuid)

		playerData.lifeNo = 0
		playerData.staged = false
		playerData.alive = true
		playerData.participating = true

		playerData.inLobbyPvpQueue.set(0)
		if (ArenaManager.playersArena(uuid) != null) ArenaManager.removePlayer(uuid)

		GameRunner.teleportPlayer(uuid, location)

		GameRunner.playerAction(uuid) { player ->
			Lobby.resetPlayerStats(player)

			/* remove all advancements */
			Bukkit.getServer().advancementIterator().forEach { advancement ->
				val progress = player.getAdvancementProgress(advancement)

				progress.awardedCriteria.forEach { criteria -> progress.revokeCriteria(criteria) }
			}

			player.gameMode = GameMode.SURVIVAL
		}

		quirks.forEach { quirk -> quirk?.onStartPlayer(uuid) }
	}

	private fun getPhase(phaseType: PhaseType): Phase {
		return when (phaseType) {
			PhaseType.GRACE -> Grace(this, config.graceTime.get())
			PhaseType.SHRINK -> Shrink(this, config.shrinkTime.get())
			PhaseType.ENDGAME -> Endgame(this, config.collapseTime.get())
			PhaseType.POSTGAME -> Postgame(this)
		}
	}

	fun setPhase(phaseType: PhaseType) {
		phase = getPhase(phaseType)
	}

	fun nextPhase(phaseType: PhaseType) {
		phase = getPhase(
			PhaseType.values()[(phaseType.ordinal + 1) % PhaseType.values().size]
		)
	}

	fun end(winners: List<UUID>, elapsedTime: Int) {
		/* if someone won */
		val title = if (winners.isNotEmpty()) {
			val winningTeam = TeamData.playersTeam(winners[0])

			val topMessage: Component
			val bottomMessage: Component

			if (winningTeam == null) {
				val winningPlayer = Bukkit.getPlayer(winners[0])

				topMessage = Component.text("${winningPlayer?.name} has won!", NamedTextColor.GOLD, TextDecoration.BOLD)
				bottomMessage = Component.empty()

				ledger.addEntry(winningPlayer?.name ?: "NULL", elapsedTime, "winning", true)

			} else {
				topMessage = winningTeam.apply("${winningTeam.gameName()} has won!")

				val playerString = winners.joinToString(" ") { Bukkit.getOfflinePlayer(it).name ?: "NULL" }
				winners.forEach { ledger.addEntry(Bukkit.getOfflinePlayer(it).name ?: "NULL", elapsedTime, "winning", true) }

				bottomMessage = winningTeam.apply(playerString)
			}

			Title.title(topMessage, bottomMessage, Title.Times.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2)))

			/* no one won the game */
		} else {
			Title.title(
				Component.text("No one wins?", NamedTextColor.GOLD, TextDecoration.BOLD), Component.empty(), Title.Times.of(
					Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2)))
		}

		ledger.createFile()

		Bukkit.getServer().onlinePlayers.forEach { player -> player.showTitle(title) }

		/* remove all teams */
		TeamData.destroyTeam(null, true, true) {}

		/* reset all player data states */
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			playerData.participating = false
			playerData.alive = false
		}

		/* stop all world borders */
		Bukkit.getWorlds().forEach { world ->
			world.worldBorder.size = world.worldBorder.size
		}

		/* go to postgame immediately */
		setPhase(PhaseType.POSTGAME)
	}

	/* other */

	fun spectatorSpawnLocation(): Location {
		for ((uuid, playerData) in PlayerData.playerDataList) {
			if (playerData.alive && playerData.participating) {
				return GameRunner.getPlayerLocation(uuid)?.clone()?.add(0.0, 2.0, 0.0)
					?: Location(world, 0.5, 100.0, 0.5)
			}
		}

		return Location(world, 0.5, 100.0, 0.5)
	}

	fun updateMobCaps() {
		val borderRadius = world.worldBorder.size / 2

		var spawnModifier = borderRadius / 128.0
		if (spawnModifier > 1.0) spawnModifier = 1.0

		world.     monsterSpawnLimit = (0 * 70 * spawnModifier).roundToInt()
		world.      animalSpawnLimit = (0 * 10 * spawnModifier).roundToInt()
		world.     ambientSpawnLimit = (    15 * spawnModifier).roundToInt().coerceAtLeast(1)
		world. waterAnimalSpawnLimit = (     5 * spawnModifier).roundToInt().coerceAtLeast(1)
		world.waterAmbientSpawnLimit = (    20 * spawnModifier).roundToInt().coerceAtLeast(1)
	}
}

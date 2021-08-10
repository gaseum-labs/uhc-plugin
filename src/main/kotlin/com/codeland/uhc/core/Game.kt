package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.core.phase.phases.Endgame
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.core.phase.phases.Shrink
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.Pests
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Action
import com.codeland.uhc.util.UHCProperty
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.Player
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
	fun quirkEnabled(quirkType: QuirkType): Boolean {
		return getQuirk(quirkType) != null
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

		Action.teleportPlayer(uuid, location)

		Action.playerAction(uuid) { player ->
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

	fun nextPhase() {
		phase = getPhase(
			PhaseType.values()[(phase.phaseType.ordinal + 1) % PhaseType.values().size]
		)
	}

	fun end(winners: List<UUID>) {
		/* if someone won */
		val title = if (winners.isNotEmpty()) {
			val winningTeam = TeamData.playersTeam(winners[0])

			val topMessage: Component
			val bottomMessage: Component

			if (winningTeam == null) {
				val winningPlayer = Bukkit.getPlayer(winners[0])

				topMessage = Component.text("${winningPlayer?.name} has won!", NamedTextColor.GOLD, TextDecoration.BOLD)
				bottomMessage = Component.empty()

				ledger.addEntry(winningPlayer?.name ?: "NULL", UHC.timer, "winning", true)

			} else {
				topMessage = winningTeam.apply("${winningTeam.gameName()} has won!")

				val playerString = winners.joinToString(" ") { Bukkit.getOfflinePlayer(it).name ?: "NULL" }
				winners.forEach { ledger.addEntry(Bukkit.getOfflinePlayer(it).name ?: "NULL", UHC.timer, "winning", true) }

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

	/* death */

	data class RemainingTeamsReturn(val remaining: Int, val lastAlive: List<UUID>?, val teamAlive: Boolean)

	/**
	 * returns both the number of remaining teams
	 * and the last remaining team if there is exactly 1
	 */
	private fun remainingTeams(focusTeam: Team?) : RemainingTeamsReturn {
		val remainingTeams = TeamData.teams.filter { teamIsAlive(it) }

		val remainingIndividuals = PlayerData.playerDataList.filter { (uuid, playerData) ->
			playerData.alive && TeamData.playersTeam(uuid) == null
		}.map { (uuid, _) -> uuid }

		val remainingCount = remainingTeams.size + remainingIndividuals.size

		val lastAlive = remainingTeams.firstOrNull()?.members ?: remainingIndividuals

		/* lastAlive is only set if only one group of players remains */
		return RemainingTeamsReturn(
			remainingCount,
			if (remainingCount == 1) lastAlive else null,
			remainingTeams.any { it === focusTeam }
		)
	}

	private fun teamIsAlive(team: Team) = team.members.any { member -> PlayerData.isAlive(member) }

	/**
	 * @param group the last remaining group of players, can be null for no last remaining group
	 * @return all the uuids of currently alive players in the group
	 */
	private fun constructAliveList(group: List<UUID>?): List<UUID> {
		return group?.map { Pair(it, PlayerData.getPlayerData(it)) }
			?.filter { (_, playerData) -> playerData.alive }
			?.map { (uuid, _) -> uuid }
			?: emptyList()
	}

	fun playerDeath(uuid: UUID, killer: Player?, playerData: PlayerData, force: Boolean) {
		if (shouldRespawn(playerData) && !force) {
			playerRespawn(uuid)
		} else {
			playerPermaDeath(uuid, killer, respawn = quirkEnabled(QuirkType.PESTS)) { Pests.onBecomePest(it) }
		}
	}

	private fun shouldRespawn(playerData: PlayerData): Boolean {
		return phase is Grace || playerData.undead()
	}

	private fun playerPermaDeath(uuid: UUID, killer: Player?, respawn: Boolean, setupRespawn: (UUID) -> Unit) {
		val playerData = PlayerData.getPlayerData(uuid)
		playerData.alive = false
		playerData.participating = respawn

		val team = TeamData.playersTeam(uuid)
		val (remainingTeams, lastRemaining, teamIsAlive) = remainingTeams(team)

		val killerTeam = if (killer == null) null else TeamData.playersTeam(killer.uniqueId)

		val killerName = when {
			killer == null -> null
			killer.uniqueId == uuid -> "self"
			team === killerTeam -> "teammate"
			else -> killer.name
		}

		/* add to ledger */
		val deadPlayerName = Bukkit.getOfflinePlayer(uuid).name ?: "NULL"
		ledger.addEntry(deadPlayerName, UHC.timer, killerName)

		val hasBeenEliminated = Component.text(" has been eliminated!", NamedTextColor.GOLD, TextDecoration.BOLD)

		val elimMessage1: Component
		val elimMessage2: Component

		/* full team elimination */
		if (team == null || !teamIsAlive) {
			elimMessage1 = team?.apply(team.gameName())?.style(Style.style(TextDecoration.BOLD))?.append(hasBeenEliminated)
				?: Component.text(deadPlayerName, NamedTextColor.GRAY, TextDecoration.BOLD).append(hasBeenEliminated)

			elimMessage2 = Component.text("$remainingTeams teams remain", NamedTextColor.GOLD, TextDecoration.BOLD)
			/* team member elimination */
		} else {
			elimMessage1 = team.apply(deadPlayerName).style(Style.style(TextDecoration.BOLD)).append(hasBeenEliminated)
			elimMessage2 = Component.empty()
		}

		Bukkit.getServer().onlinePlayers.filter { WorldManager.isGameWorld(it.world) }.forEach { player ->
			player.sendMessage(elimMessage1)
			player.sendMessage(elimMessage2)
		}

		/* does the UHC end here? */
		if (remainingTeams <= 1) {
			Action.playerAction(uuid) { it.gameMode = GameMode.SPECTATOR }
			end(constructAliveList(lastRemaining))

			/* or does it keep going */
		} else {
			/* apply kill reward */
			if (killer != null) config.killReward.get().applyReward(arrayListOf(killer))

			/* tell player they died */
			if (respawn) {
				setupRespawn(uuid)
				playerRespawn(uuid)
			} else {
				Action.playerAction(uuid) { deathTitle(it, killer, false) }
			}
		}
	}

	private fun playerRespawn(uuid: UUID) {
		Action.playerAction(uuid) { deathTitle(it, null, true) }

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin, {
			++PlayerData.getPlayerData(uuid).lifeNo

			Action.teleportPlayer(uuid, respawnLocation())

			Action.playerAction(uuid) { it.gameMode = GameMode.SURVIVAL }

			quirks.filterNotNull().forEach { it.onStartPlayer(uuid) }
		}, 100)
	}

	private fun deathTitle(player: Player, killer: Player?, respawn: Boolean) {
		player.gameMode = GameMode.SPECTATOR
		Lobby.resetPlayerStats(player)

		player.sendTitle(
			"${ChatColor.RED}You died!",
			"${ChatColor.DARK_RED}${if (respawn) "Prepare to respawn" else {
				if (killer == null) "Killed by environment" else "Killed by ${killer.name}"
			}}",
			0, 80, 20
		)
	}

	private fun respawnLocation(): Location {
		return PlayerSpreader.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
			?: Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
	}

	/* other */

	fun spectatorSpawnLocation(): Location {
		for ((uuid, playerData) in PlayerData.playerDataList) {
			if (playerData.alive && playerData.participating) {
				return Action.getPlayerLocation(uuid)?.clone()?.add(0.0, 2.0, 0.0)
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

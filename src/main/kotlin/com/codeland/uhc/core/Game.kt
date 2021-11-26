package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.core.phase.phases.*
import com.codeland.uhc.core.stats.Ledger
import com.codeland.uhc.customSpawning.regeneration.*
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.Teams
import com.codeland.uhc.util.*
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.*
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.entity.Player
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.roundToInt

class Game(
	val config: GameConfig,
	val teams: Teams<Team>,
	val initialRadius: Int,
	val world: World,
	val otherWorld: World,
) {
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

	val startDate = ZonedDateTime.now()

	val ledger = Ledger(initialRadius)

	val sugarCaneRegen = SugarCaneRegen(this)
	val leatherRegen = LeatherRegen(this)
	val melonRegen = MelonRegen(this)

	val endgameLowY: Int
	val endgameHighY: Int

	init {
		val (low, high) = Endgame.determineMinMax(world, config.endgameRadius.get(), 100)
		endgameLowY = low
		endgameHighY = high
	}

	/* getters */

	fun <T : Quirk> getQuirk(quirkType: QuirkType): T? {
		return quirks[quirkType.ordinal] as T?
	}

	fun quirkEnabled(quirkType: QuirkType): Boolean {
		return getQuirk<Quirk>(quirkType) != null
	}

	fun isOver(): Boolean {
		return phase is Postgame
	}

	/* flow */

	fun startPlayer(uuid: UUID, location: Location) {
		val playerData = PlayerData.getPlayerData(uuid)

		playerData.lifeNo = 0
		playerData.alive = true
		playerData.participating = true

		playerData.inLobbyPvpQueue.set(0)
		ArenaManager.removePlayer(uuid)

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

	fun end(winningTeam: Team?) {
		/* game summary */
		if (winningTeam != null) {
			ledger.publish(
				config.gameType.get(),
				startDate,
				UHC.timer,
				teams.teams(),
				winningTeam.members.filter { PlayerData.isAlive(it) }
			)
		}

		val endTitle = createEndTitle(winningTeam)
		Bukkit.getServer().onlinePlayers.forEach { player -> player.showTitle(endTitle) }

		/* put everyone back in the general channel */
		UHC.bot?.clearTeamVCs()

		/* reset all player data states */
		PlayerData.playerDataList.forEach { (_, playerData) ->
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

	fun createEndTitle(winningTeam: Team?): Title {
		return if (winningTeam == null) {
			Title.title(
				Component.text("No one wins?", NamedTextColor.GOLD, TextDecoration.BOLD),
				Component.empty(),
				Title.Times.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2))
			)
		} else {
			Title.title(
				winningTeam.apply("${winningTeam.name} has won!"),
				winningTeam.apply(winningTeam.members.filter { PlayerData.isAlive(it) }
					.joinToString(", ") { Bukkit.getOfflinePlayer(it).name ?: "NULL" }),
				Title.Times.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2))
			)
		}
	}

	/* death */

	fun playerDeath(uuid: UUID, killer: Player?, playerData: PlayerData, forcePermaDeath: Boolean) {
		if (!forcePermaDeath && shouldRespawn(playerData)) {
			playerRespawn(uuid)
		} else {
			playerPermaDeath(uuid, killer, respawn = quirkEnabled(QuirkType.PESTS)) { teams.leaveTeam(uuid) }
		}
	}

	private data class RemainingTeamsReturn(val numRemaining: Int, val lastTeamAlive: Team?, val teamAlive: Boolean)

	/**
	 * @return the number of remaining teams,
	 * the last remaining team if there is exactly 1 else null,
	 * and if a provided team is among the remaining
	 */
	private fun remainingTeamsFocusOn(focusTeam: Team?): RemainingTeamsReturn {
		val remainingTeams = teams.teams().filter { it.members.any { member -> PlayerData.isAlive(member) } }

		return RemainingTeamsReturn(
			remainingTeams.size,
			if (remainingTeams.size == 1) remainingTeams.firstOrNull() else null,
			remainingTeams.any { it === focusTeam }
		)
	}

	private fun shouldRespawn(playerData: PlayerData): Boolean {
		return phase is Grace || playerData.undead()
	}

	private fun playerPermaDeath(uuid: UUID, killer: Player?, respawn: Boolean, setupRespawn: (UUID) -> Unit) {
		val playerData = PlayerData.getPlayerData(uuid)
		val playerTeam = teams.playersTeam(uuid)
		val killerTeam = if (killer == null) null else teams.playersTeam(killer.uniqueId)

		/* make them dead */
		playerData.alive = false
		playerData.participating = respawn

		val (numRemaining, lastTeamAlive, teamIsAlive) = remainingTeamsFocusOn(playerTeam)

		/* broadcast elimination */
		val eliminationMessages = eliminationMessages(uuid, playerTeam, numRemaining, teamIsAlive)
		Bukkit.getOnlinePlayers().filter { WorldManager.isGameWorld(it.world) }.forEach { player ->
			eliminationMessages.forEach { player.sendMessage(it) }
		}

		/* add to ledger */
		ledger.addEntry(uuid, UHC.timer, killer?.uniqueId)

		/* does the UHC end here? */
		if (numRemaining <= 1) {
			Action.playerAction(uuid) { it.gameMode = GameMode.SPECTATOR }
			end(lastTeamAlive)

			/* or does it keep going */
		} else {
			/* apply kill reward (no team kills) */
			if (killer != null && playerTeam !== killerTeam) {
				config.killReward.get().apply(
					killer.uniqueId,
					killerTeam?.members ?: arrayListOf(),
					Action.getPlayerLocation(uuid) ?: spectatorSpawnLocation()
				)
			}

			/* tell player they died */
			if (respawn) {
				setupRespawn(uuid)
				playerRespawn(uuid)
			} else {
				Action.playerAction(uuid) { deathTitle(it, killer, false) }
			}
		}
	}

	private fun eliminationMessages(
		uuid: UUID,
		playerTeam: Team?,
		numRemaining: Int,
		teamIsAlive: Boolean,
	): List<Component> {
		val playerName = Bukkit.getOfflinePlayer(uuid).name ?: "Unknown"

		/* should never happen */
		if (playerTeam == null) return listOf(Component.text(playerName, NamedTextColor.GRAY, TextDecoration.BOLD))

		val color0 = TextColor.color(0xf0e118)
		val color1 = TextColor.color(0xedd42f)

		return listOfNotNull(
			Component.empty()
				.append(playerTeam.apply(playerName).decorate(TextDecoration.BOLD))
				.append(Util.gradientString(" has been eliminated!", color0, color1)),

			if (!teamIsAlive) {
				Util.gradientString("All members of ", color0, color1)
					.append(playerTeam.apply(playerTeam.name).decorate(TextDecoration.BOLD))
					.append(Util.gradientString(" have been eliminated!", color0, color1))
			} else null,

			if (!teamIsAlive) {
				Util.gradientString(
					if (numRemaining == 1) {
						"Final team remains"
					} else {
						"$numRemaining teams remain"
					}, color0, color1
				)
			} else null
		)
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
			"${ChatColor.DARK_RED}${
				when {
					respawn -> "Prepare to respawn"
					killer != null -> "killed by ${killer.name}"
					else -> ""
				}
			}",
			0, 80, 20
		)
	}

	private fun respawnLocation(): Location {
		return PlayerSpreader.spreadSinglePlayer(world, (world.worldBorder.size / 2) - 5)
			?: Location(world, 0.5, Util.topBlockY(world, 0, 0) + 1.0, 0.5)
	}

	fun getOverworld(): World {
		return if (world.environment === World.Environment.NORMAL) world else otherWorld
	}

	fun getNetherWorld(): World {
		return if (world.environment === World.Environment.NETHER) world else otherWorld
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

	fun updateMobCaps(world: World) {
		val borderRadius = world.worldBorder.size / 2

		var spawnModifier = borderRadius / 128.0
		if (spawnModifier > 1.0) spawnModifier = 1.0

		world.monsterSpawnLimit = 0
		world.animalSpawnLimit = 0
		world.ambientSpawnLimit = (15 * spawnModifier).roundToInt().coerceAtLeast(1)
		world.waterAnimalSpawnLimit = (5 * spawnModifier).roundToInt().coerceAtLeast(1)
		world.waterAmbientSpawnLimit = (20 * spawnModifier).roundToInt().coerceAtLeast(1)
	}
}

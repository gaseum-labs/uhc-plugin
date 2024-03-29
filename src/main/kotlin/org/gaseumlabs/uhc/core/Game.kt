package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.core.phase.phases.*
import org.gaseumlabs.uhc.database.summary.SummaryBuilder
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.team.Teams
import org.gaseumlabs.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.*
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.TextDecoration.BOLD
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.Title.Times
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.entity.SpawnCategory
import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.chc.chcs.Pests
import org.gaseumlabs.uhc.database.summary.GameType
import org.gaseumlabs.uhc.util.*
import org.gaseumlabs.uhc.world.regenresource.GlobalResources
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.roundToInt

class Game(
	val config: GameConfig,
	val preset: GamePreset,
	val teams: Teams<Team>,
	val heightmap: Heightmap,
	val chc: CHC<*>?,
	val world: World,
	val otherWorld: World,
	val worldRadius: Int,
) {
	private val startDate: ZonedDateTime = ZonedDateTime.now()
	private val summaryBuilder = SummaryBuilder()

	val globalResources = GlobalResources()
	val trader = Trader()

	var phase = getPhase(PhaseType.GRACE)

	fun startPlayer(uuid: UUID, location: Location) {
		val playerData = PlayerData.get(uuid)

		playerData.lifeNo = 0
		playerData.alive = true
		playerData.participating = true
		playerData.inLobbyPvpQueue = 0

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
		chc?.onStartPlayer(this, uuid)
	}

	private fun getPhase(phaseType: PhaseType): Phase {
		return when (phaseType) {
			PhaseType.GRACE -> Grace(this, preset.graceTime)
			PhaseType.SHRINK -> Shrink(this, preset.shrinkTime)
			PhaseType.BATTLEGROUND -> Battleground(this, preset.battlegroundTime)
			PhaseType.ENDGAME -> Endgame(this, heightmap, preset.battlegroundRadius, preset.collapseTime)
			PhaseType.POSTGAME -> Postgame(this)
		}
	}

	fun setPhase(phaseType: PhaseType) {
		phase = getPhase(phaseType)
		chc?.onPhaseSwitch(this, phase)
	}

	fun nextPhase() = setPhase(
		PhaseType.values()[(phase.phaseType.ordinal + 1) % PhaseType.values().size]
	)

	private fun end(winningTeam: Team?) {
		/* game summary */
		if (winningTeam != null) {
			val summary = summaryBuilder.toSummary(
				if (chc == null) GameType.UHC else GameType.CHC,
				startDate,
				UHC.timer.get(),
				teams.teams(),
				winningTeam.members.filter { PlayerData.get(it).alive }
			)

			SummaryBuilder.saveSummaryLocally(summary)
			UHC.dataManager.uploadSummary(summary)
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

	private fun createEndTitle(winningTeam: Team?) = if (winningTeam == null)
			Title.title(
				Component.text("No one wins?", GOLD, BOLD),
				Component.empty(),
				Times.times(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2))
			)
		else
			Title.title(
				winningTeam.apply("${winningTeam.name} has won!"),
				winningTeam.apply(winningTeam.members.filter { PlayerData.get(it).alive }
					.joinToString(", ") { Bukkit.getOfflinePlayer(it).name ?: "NULL" }),
				Times.times(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2))
			)

	/**
	 * everyone who can see a death message
	 * spectators and game players, dead or alive (anyone in the game world)
	 */
	private fun deathMessagePlayers() = Bukkit.getOnlinePlayers().filter { WorldManager.isGameWorld(it.world) }

	fun playerDeath(
		uuid: UUID,
		location: Location?,
		killer: Player?,
		playerData: PlayerData,
		forcePermaDeath: Boolean,
		deathMessage: Component?
	) {
		deathMessage?.let { deathMessagePlayers().forEach { it.sendMessage(deathMessage) } }

		if (!forcePermaDeath && shouldRespawn(playerData)) {
			playerRespawn(uuid)
		} else {
			playerPermaDeath(uuid, location, killer, chc is Pests)
		}
	}

	private data class RemainingTeamsReturn(val numRemaining: Int, val lastTeamAlive: Team?, val teamAlive: Boolean)

	/**
	 * @return the number of remaining teams,
	 * the last remaining team if there is exactly 1 else null,
	 * and if a provided team is among the remaining
	 */
	private fun remainingTeamsFocusOn(focusTeam: Team?): RemainingTeamsReturn {
		val remainingTeams = teams.teams().filter { it.members.any { member -> PlayerData.get(member).alive } }

		return RemainingTeamsReturn(
			remainingTeams.size,
			if (remainingTeams.size == 1) remainingTeams.firstOrNull() else null,
			remainingTeams.any { it === focusTeam }
		)
	}

	private fun shouldRespawn(playerData: PlayerData): Boolean {
		return phase is Grace || playerData.undead()
	}

	private fun playerPermaDeath(
		uuid: UUID,
		location: Location?,
		killer: Player?,
		respawn: Boolean,
	) {
		val playerData = PlayerData.get(uuid)
		val playerTeam = teams.playersTeam(uuid)
		val killerTeam = if (killer == null) null else teams.playersTeam(killer.uniqueId)

		/* make them dead */
		playerData.alive = false
		playerData.participating = respawn

		/* check game state after death */
		val (numRemaining, lastTeamAlive, teamIsAlive) = remainingTeamsFocusOn(playerTeam)

		/* broadcast elimination */
		val eliminationMessages = eliminationMessages(uuid, playerTeam, numRemaining, teamIsAlive)
		Bukkit.getOnlinePlayers().forEach { player ->
			if (WorldManager.isGameWorld(player.world)) {
				eliminationMessages.forEach { player.sendMessage(it) }
			}
		}

		/* add to ledger */
		summaryBuilder.addEntry(uuid, UHC.timer.get(), killer?.uniqueId)

		/* chc undoes them */
		chc?.onEndPlayer(this, uuid)

		/* end the game if everyone else has died */
		if (numRemaining <= 1) {
			Action.playerAction(uuid) { it.gameMode = GameMode.SPECTATOR }
			end(lastTeamAlive)

		} else if (respawn) {
			teams.leaveTeam(uuid)
			playerRespawn(uuid)

		/* regular kill */
		} else {
			/* apply kill reward, only on player kill, no team kills */
			if (
				killer != null &&
				playerTeam !== killerTeam &&
				location != null
			) preset.killReward.apply(
				killer.uniqueId,
				killerTeam?.members ?: arrayListOf(),
				location
			)

			Action.playerAction(uuid) { makePlayerDead(it, killer, false) }
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
		if (playerTeam == null) return listOf(Component.text(playerName, NamedTextColor.GRAY, BOLD))

		val color0 = TextColor.color(0xf0e118)
		val color1 = TextColor.color(0xedd42f)

		return listOfNotNull(
			Component.empty()
				.append(playerTeam.apply(playerName).decorate(BOLD))
				.append(Util.gradientString(" has been eliminated!", color0, color1)),

			if (!teamIsAlive) {
				Util.gradientString("All members of ", color0, color1)
					.append(playerTeam.apply(playerTeam.name).decorate(BOLD))
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
		Action.playerAction(uuid) { makePlayerDead(it, null, true) }

		Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin, {
			++PlayerData.get(uuid).lifeNo

			Action.teleportPlayer(uuid, respawnLocation())
			Action.playerAction(uuid) { it.gameMode = GameMode.SURVIVAL }

			chc?.onStartPlayer(this, uuid)
		}, 100)
	}

	private fun makePlayerDead(player: Player, killer: Player?, respawn: Boolean) {
		player.gameMode = GameMode.SPECTATOR
		Lobby.resetPlayerStats(player)

		player.showTitle(Title.title(
			Component.text("You died!", NamedTextColor.RED),
			Component.text(when {
				respawn -> "Prepare to respawn"
				killer != null -> "killed by ${killer.name}"
				else -> ""
			}, NamedTextColor.DARK_RED),
			Times.times(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(1))
		))
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

	private fun spawnLimit(max: Int, borderRadius: Double) =
		(max * (borderRadius / 128.0).coerceAtMost(1.0))
			.roundToInt().coerceAtLeast(1)

	fun updateMobCaps(world: World) {
		val borderRadius = world.worldBorder.size / 2

		world.setSpawnLimit(SpawnCategory.MONSTER, 0)
		world.setSpawnLimit(SpawnCategory.ANIMAL, 0)

		world.setSpawnLimit(SpawnCategory.AMBIENT, spawnLimit(15, borderRadius))
		world.setSpawnLimit(SpawnCategory.WATER_AMBIENT, spawnLimit(20, borderRadius))
		world.setSpawnLimit(SpawnCategory.AXOLOTL, spawnLimit(5, borderRadius))
		world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, spawnLimit(5, borderRadius))
		world.setSpawnLimit(SpawnCategory.WATER_UNDERGROUND_CREATURE, spawnLimit(5, borderRadius))
	}

	companion object {
		fun bloodCloud(location: Location) {
			location.world.spawnParticle(Particle.REDSTONE,
				location.clone().add(0.0, 1.0, 0.0),
				64,
				0.5,
				1.0,
				0.5,
				Particle.DustOptions(Color.RED, 2.0f))
		}
	}
}

package com.codeland.uhc.core

import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.customSpawning.CustomSpawningType
import com.codeland.uhc.core.phase.phases.Grace
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.core.phase.phases.Postgame
import com.codeland.uhc.core.phase.phases.Shrink
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.event.Portal
import com.codeland.uhc.gui.gui.CreateGameGui
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Action
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import net.minecraft.world.BossBattle
import org.bukkit.*
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.RenderType
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object UHC {
	private var preGameConfig: GameConfig = GameConfig()

	var game: Game? = null
	var timer = 0
	var timerGoing = false

	var teleportGroups = HashMap<UUID, Location>()
	var worldRadius: Int = 375

	var bot: MixerBot? = null
	lateinit var heartsObjective: Objective

	val areaPerPlayer = area(375.0f) / 8
	fun area(radius: Float) = ((radius * 2) + 1).pow(2)
	fun radius(area: Float) = (sqrt(area) - 1) / 2

	fun getConfig(): GameConfig {
		return game?.config ?: preGameConfig
	}

	/* game flow modifiers */

	private fun countdownColor(number: Int): TextColor {
		return Util.interpColor(1.0f - (number / 10.0f), TextColor.color(0xebd80c), TextColor.color(0xeb0c0c))
	}

	fun startLobby() {
		/* register hearts objective */
		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		val objective = scoreboard.getObjective("hp")
			?: scoreboard.registerNewObjective("hp", "health", Component.text("hp"), RenderType.HEARTS)

		objective.renderType = RenderType.HEARTS
		objective.displayName(Component.text("hp"))
		objective.displaySlot = DisplaySlot.PLAYER_LIST

		heartsObjective = objective

		/* clear residual teams */
		TeamData.destroyTeam(null, true, true) {}
		Bukkit.getServer().onlinePlayers.forEach { player -> Lobby.onSpawnLobby(player) }

		/* begin global ticking task */
		/* holds a centralized list of all general continuous tasks throughout the game */
		var currentTick = 0

		SchedulerUtil.everyTick {
			val currentGame = game
			if (currentGame != null) {
				val switchResult = currentGame.phase.tick(currentTick)

				if (currentGame.phase is Grace || currentGame.phase is Shrink) {
					CustomSpawning.spawnTick(CustomSpawningType.HOSTILE, currentTick, currentGame)
					CustomSpawning.spawnTick(CustomSpawningType.PASSIVE, currentTick, currentGame)
				}

				Portal.portalTick(currentGame)
				PlayerData.zombieBorderTick(currentTick, currentGame)

				ledgerTrailTick(currentGame, currentTick)

				if (currentTick % 20 == 0) {
					currentGame.updateMobCaps()
					containSpecs()
				}

				if (switchResult) currentGame.nextPhase()
				if (currentGame.phase !is Postgame) ++timer

			} else if (currentTick % 20 == 0 && timerGoing) {
				++timer

				if (timer < 0) {
					val countdownTitle = Title.title(
						Component.text("${-timer}", countdownColor(-timer), TextDecoration.BOLD),
						Component.text("Game starts in"),
						Title.Times.of(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(2))
					)

					Bukkit.getOnlinePlayers().forEach { player ->
						if (PlayerData.getPlayerData(player.uniqueId).staged) {
							player.showTitle(countdownTitle)
						}
					}

				} else if (timer == 0) {
					val newGame = Game(
						preGameConfig,
						worldRadius,
						preGameConfig.getWorld()!!
					)

					/* set border in each game dimension */
					listOf(WorldManager.getGameWorldGame(), WorldManager.getNetherWorldGame()).forEach { world ->
						world.worldBorder.setCenter(0.5, 0.5)
						world.worldBorder.size = worldRadius * 2 + 1.0

						world.time = 0
						world.isThundering = false
						world.setStorm(false)
					}

					/* give all teams that don't have names a name */
					/* add people to team vcs */
					TeamData.teams.forEach { team ->
						if (team.name == null) team.automaticName()
						if (preGameConfig.usingBot.get()) bot?.addToTeamChannel(team, team.members)
					}

					/* teleport and set playerData to current */
					teleportGroups.forEach { (uuid, location) ->
						newGame.startPlayer(uuid, location)
					}

					game = newGame
				}
			}

			updateBossbarTick()
			Lobby.lobbyTipsTick(currentTick)
			ArenaManager.perTick(currentTick)

			/* highly composite number */
			currentTick = (currentTick + 1) % 294053760
		}
	}

	private fun ledgerTrailTick(game: Game, currentTick: Int) {
		if (currentTick % 40 != 0) return

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			val player = Bukkit.getPlayer(uuid)

			if (playerData.participating && (player == null || player.gameMode !== GameMode.SPECTATOR)) {
				val block = Action.getPlayerLocation(uuid)?.block
				if (block != null) game.ledger.addPlayerPosition(uuid, block)
			}
		}
	}

	private fun updateBossbarTick() {
		Bukkit.getOnlinePlayers().forEach { player ->
			val arena = ArenaManager.playersArena(player.uniqueId)

			when {
				arena is PvpArena -> {
					UHCBar.updateBossBar(
						player,
						if (arena.isOver()) {
							"${ChatColor.RED}Game Over"
						} else {
							"${ChatColor.RED}${PvpArena.typeName(arena.matchType)} PVP" +
								if (arena.startTime >= 0) {
									" | " + if (arena.shouldGlow()) {
										"${ChatColor.GOLD}Glowing"
									} else {
										"Glowing in ${Util.timeString(arena.glowTimer)}"
									}
								} else {
									""
								}
						},
						if (arena.isOver() || arena.glowPeriod == 0 || arena.glowTimer <= 0) {
							1.0f
						} else if (arena.startTime < 0) {
							0.0f
						} else {
							1.0f - (arena.glowTimer.toFloat() / arena.glowPeriod)
						},
						BossBattle.BarColor.c
					)
				}
				arena is ParkourArena -> {
					UHCBar.updateBossBar(
						player,
						"Parkour",
						1.0f,
						BossBattle.BarColor.g
					)
				}
				player.world.name == WorldManager.LOBBY_WORLD_NAME -> {
					val phase = game?.phase
					val phaseType = phase?.phaseType

					UHCBar.updateBossBar(
						player,
						"${ChatColor.WHITE}Waiting Lobby" +
							if (phaseType != null) {
								" | ${phaseType.chatColor}Game Ongoing: ${phaseType.prettyName}"
							} else {
								""
							},
						phase?.updateBarLength(phase.remainingTicks) ?: 1.0f,
						BossBattle.BarColor.g
					)
				}
				else -> {
					val phase = game?.phase
					if (phase != null) UHCBar.updateBossBar(
						player,
						phase.updateBarTitle(player.world, phase.remainingSeconds()),
						phase.updateBarLength(phase.remainingTicks),
						phase.phaseType.barColor
					)
				}
			}
		}
	}

	/**
	 * @param messageStream send status messages and error messages to the caller,
	 * true indicates an error
	 */
	fun startGame(messageStream: (Boolean, String) -> Unit): Boolean {
		if (game != null) {
			messageStream(true, "Game has already started")
			return false
		}

		/* compile a list of all individuals that will play */
		val individuals = PlayerData.playerDataList
			.filter { (uuid, playerData) -> playerData.staged && !TeamData.isOnTeam(uuid) }
			.map { (uuid, _) -> uuid }

		val numGroups = TeamData.teams.size + individuals.size
		val numPlayers = TeamData.teams.fold(0) { acc, team -> acc + team.members.size } + individuals.size

		if (numGroups == 0) {
			messageStream(true, "No one is playing")
			return false
		}

		messageStream(false, "Creating game worlds for $numPlayers players")

		worldRadius = radius(numPlayers * preGameConfig.scale.get() * areaPerPlayer).roundToInt()

		/* create worlds */
		WorldManager.refreshGameWorlds()

		/* get where players are teleporting */
		val world = preGameConfig.getWorld()
		if (world == null) {
			messageStream(true, "Worlds did not initialize")
			return false
		}

		val tempTeleportLocations = PlayerSpreader.spreadPlayers(
			world,
			numGroups,
			worldRadius - 16.0,
			if (world.environment == World.Environment.NETHER) PlayerSpreader::findYMid else PlayerSpreader::findYTop
		)

		if (tempTeleportLocations.isEmpty()) {
			messageStream(true, "Not enough valid starting locations found")
			return false
		}

		/* create the master map of teleport locations */
		teleportGroups = HashMap()

		individuals.forEachIndexed { i, uuid -> teleportGroups[uuid] = tempTeleportLocations[i] }
		TeamData.teams.forEachIndexed { i, team ->
			team.members.forEach { uuid ->
				teleportGroups[uuid] = tempTeleportLocations[i + individuals.size]
			}
		}

		timer = -11
		timerGoing = true
		preGameConfig.lock = true

		messageStream(false, "Starting UHC")

		return true
	}

	fun destroyGame() {
		game = null
		preGameConfig = GameConfig()

		PlayerData.prune()
		Bukkit.getOnlinePlayers().forEach { player -> Lobby.onSpawnLobby(player) }

		WorldManager.destroyGameWorlds()
	}

	fun containSpecs() {
		val radius = game?.initialRadius ?: return

		val gameWorld = WorldManager.getGameWorld()
		val netherWorld = WorldManager.getNetherWorld()

		Bukkit.getOnlinePlayers().filter { it.world === gameWorld || it.world === netherWorld }.forEach { player ->
			if (player.gameMode == GameMode.SPECTATOR) {
				val locX = player.location.blockX.toDouble()
				val locZ = player.location.blockZ.toDouble()

				val x = when {
					locX > radius -> radius.toDouble()
					locX < -radius -> -radius.toDouble()
					else -> locX
				}

				val z = when {
					locZ > radius -> radius.toDouble()
					locZ < -radius -> -radius.toDouble()
					else -> locZ
				}

				if (x != locX || z != locZ) player.teleport(player.location.set(x + 0.5, player.location.y, z + 0.5))
			}
		}
	}
}

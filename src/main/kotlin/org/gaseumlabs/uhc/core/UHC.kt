package org.gaseumlabs.uhc.core

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.scoreboard.*
import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.chc.CHC
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.core.phase.phases.*
import org.gaseumlabs.uhc.customSpawning.CustomSpawning
import org.gaseumlabs.uhc.customSpawning.CustomSpawningType
import org.gaseumlabs.uhc.database.DataManager
import org.gaseumlabs.uhc.discord.MixerBot
import org.gaseumlabs.uhc.event.Portal
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.team.*
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.*
import org.gaseumlabs.uhc.util.Util.void
import org.gaseumlabs.uhc.world.WorldManager
import java.time.Duration
import java.util.*
import kotlin.math.*

object UHC {
	val colorCube: ColorCube = ColorCube()
	val timer = GameTimer()

	private var preGameConfig: GameConfig = GameConfig()
	var preGameTeams: Teams<PreTeam> = Teams({ action ->
		Teams.updateNames(action.uuids, action.team)
	}, { team ->
		colorCube.removeTeam(team.colors)
	})

	var game: Game? = null
	var chc: CHC<*>? = null
	var preset: GamePreset = GamePreset.defaultGamePreset()
	var chcListener: Listener? = null
	var heightmap: Heightmap? = null

	var teleportGroups = HashMap<UUID, Location>()
	var worldRadius: Int = 0

	var dataManager: DataManager = DataManager.createDataManager(UHCPlugin.configFile, UHCPlugin.uhcDbFile)
	var bot: MixerBot? = null

	lateinit var heartsObjective: Objective

	/* 3 biomes times 6 chunks per biome times 16 blocks per chunk */
	val areaPerPlayer = (3 * 6 * 16) * (3 * 6 * 16) / 2
	fun area(radius: Float) = ((radius * 2) + 1).pow(2)
	fun radius(area: Float) = (sqrt(area) - 1) / 2

	fun getConfig(): GameConfig {
		return game?.config ?: preGameConfig
	}

	fun getTeams(): Teams<AbstractTeam> {
		return (game?.teams ?: preGameTeams) as Teams<AbstractTeam>
	}

	/* game flow modifiers */

	private fun countdownColor(number: Int): TextColor {
		return Util.interpColor(1.0f - (number / 10.0f), TextColor.color(0xebd80c), TextColor.color(0xeb0c0c))
	}

	fun start() {
		/* register hearts objective */
		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		val objective = scoreboard.getObjective("hp")
			?: scoreboard.registerNewObjective("hp", "health", Component.text("hp"), RenderType.HEARTS)

		objective.renderType = RenderType.HEARTS
		objective.displayName(Component.text("hp"))
		objective.displaySlot = DisplaySlot.PLAYER_LIST

		heartsObjective = objective

		/* lobby spawn */
		Bukkit.getServer().onlinePlayers.forEach { player -> Lobby.onSpawnLobby(player) }

		/* begin global ticking task */
		/* holds a centralized list of all general continuous tasks throughout the game */
		var currentTick = 0

		SchedulerUtil.everyTick {
			val currentGame = game

			if (currentGame != null) {
				if (currentGame.phase !is Postgame) {
					CustomSpawning.spawnTick(CustomSpawningType.HOSTILE, currentTick, currentGame)
					CustomSpawning.spawnTick(CustomSpawningType.PASSIVE, currentTick, currentGame)
					currentGame.globalResources.tick(currentGame, currentTick)

					OfflineZombie.zombieBorderTick(currentTick, currentGame)
					currentGame.trader.traderTick(currentTick)
				}

				if (currentGame.phase.tick(currentTick)) {
					currentGame.nextPhase()
				}
			}

			if (currentTick % 20 == 0) {
				if (currentGame != null && currentGame.phase !is Postgame) {
					currentGame.updateMobCaps(currentGame.world)
					currentGame.updateMobCaps(currentGame.otherWorld)
					containSpecs()

					timer.tick()

				} else if (currentGame == null && timer.onMode(GameTimer.Mode.GAMING)) {
					if (timer.get() < 0) {
						val countdownTitle = Title.title(
							Component.text("${-timer.get()}", countdownColor(-timer.get()), TextDecoration.BOLD),
							Component.text("Game starts in"),
							Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(2))
						)

						preGameTeams.teams().forEach {
							it.members.forEach { uuid ->
								Bukkit.getPlayer(uuid)?.showTitle(countdownTitle)
							}
						}
					} else {
						gameStartTick()
					}

					timer.tick()
				}
			} else if (currentTick % 1200 == 0) {
				if (game == null) dataManager.linkData.massPlayersLink()
			}

			Lobby.lobbyTipsTick(currentTick)
			ArenaManager.perTick(currentTick)
			Bukkit.getOnlinePlayers().forEach(UHCBar::updateBar)
			Portal.portalTick()

			++currentTick
		}
	}

	/**
	 * @param messageStream send status messages and error messages to the caller,
	 * true indicates an error
	 */
	fun startGame(messageStream: (Boolean, String) -> Unit) {
		fun badExit(message: String) {
			timer.reset()
			messageStream(true, message)
		}

		if (timer.mode !== GameTimer.Mode.NONE) return badExit("Game has already started")
		timer.launch()

		/* whoever is on a team pregame will be who participates, count players for world size */
		val teams = preGameTeams.teams()
		val numPlayers = teams.fold(0) { i, team -> i + team.members.size }
		if (numPlayers == 0) return badExit("No one is playing")

		chc = preGameConfig.chcType?.create?.let { it() }
		preset = chc?.gamePreset() ?: GamePreset.defaultGamePreset()

		val worldSizePlayers = numPlayers.coerceAtLeast(2)
		worldRadius = radius(worldSizePlayers * preset.scale * areaPerPlayer).toInt()
		messageStream(false, "Creating game worlds for the size of $worldSizePlayers players")

		/* initiate chc before worlds initialize */
		chcListener = chc?.eventListener()
		chcListener?.let { Bukkit.getPluginManager().registerEvents(it, UHCPlugin.plugin) }

		/* create worlds */
		WorldManager.refreshGameWorlds()
		heightmap = Heightmap(preset.battlegroundRadius, 48)
		heightmap!!.generate(WorldManager.gameWorld!!)

		val (world, otherWorld) = WorldManager.getGameWorldsBy(preset.defaultWorldEnvironment)

		/* set border in each game dimension */
		arrayOf(world, otherWorld).forEach {
			it.worldBorder.setCenter(0.5, 0.5)
			it.worldBorder.size = worldRadius * 2 + 1.0
		}

		messageStream(false, "Finding starting locations")

		PlayerSpreader.spreadPlayers(world, worldRadius, teams).thenAccept { teleports ->
			/* create the master map of teleport locations */
			teleportGroups = HashMap()

			for (i in teams.indices) {
				val blockList = teleports[i]
				teams[i].members.forEachIndexed { j, uuid ->
					teleportGroups[uuid] = blockList[j].getRelative(0, 1, 0).location.add(0.5, 0.0, 0.5)
				}
			}

			timer.start()
			preGameConfig.lock = true

			messageStream(false, "Starting UHC")
		}.exceptionally { ex ->
			messageStream(true, "Could not start | ${ex.message}").void()
		}
	}

	fun gameStartTick() {
		/* add people to team vcs */
		/* make teams finalized */
		val gameTeams: Teams<Team> = Teams({ action ->
			Teams.updateNames(action.uuids, action.team)

			val bot = UHC.bot ?: return@Teams

			if (getConfig().usingBot) when (action) {
				is Teams.ClearAction -> bot.clearTeamVCs()
				is Teams.AddAction -> bot.addToTeamChannel(action.id, action.uuids)
				is Teams.RemoveAction -> bot.removeFromTeamChannel(action.id, action.size, action.uuids)
			}
		}, { team ->
			colorCube.removeTeam(team.colors)
		})
		preGameTeams.transfer(gameTeams, PreTeam::toTeam)

		val (world, otherWorld) = if (preset.defaultWorldEnvironment === World.Environment.NORMAL)
			WorldManager.gameWorld to WorldManager.netherWorld
		else
			WorldManager.netherWorld to WorldManager.gameWorld

		/* GAME OBJECT */
		val newGame = Game(
			preGameConfig,
			preset,
			gameTeams,
			heightmap ?: throw Error("No heightmap?"),
			chc,
			world ?: throw Error("No world?"),
			otherWorld ?: throw Error("No otherWorld?"),
			worldRadius
		)

		/* teleport and set playerData to current */
		teleportGroups.forEach { (uuid, location) ->
			newGame.startPlayer(uuid, location)
		}

		newGame.setPhase(PhaseType.GRACE)

		arrayOf(world, otherWorld).forEach { it.time = 0 }

		game = newGame
		preGameTeams.clear()
	}

	fun destroyGame() {
		val runningGame = game
		if (runningGame != null) {
			runningGame.teams.clearTeams()
			chc?.onDestroy(runningGame)
		}

		chcListener?.let { HandlerList.unregisterAll(it) }
		preGameConfig = GameConfig()
		timer.reset()

		game = null

		PlayerData.prune()
		WorldManager.destroyGameWorlds()
	}

	fun containSpecs() {
		val currentGame = game ?: return
		val radius = worldRadius

		Bukkit.getOnlinePlayers()
			.filter { it.world === currentGame.world || it.world === currentGame.otherWorld }
			.forEach { player ->
				if (player.gameMode === GameMode.SPECTATOR) {
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

					if (x != locX || z != locZ) player.teleport(
						player.location.set(
							x + 0.5,
							player.location.y,
							z + 0.5
						)
					)
				}
			}
	}
}

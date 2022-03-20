package org.gaseumlabs.uhc.core

import org.gaseumlabs.uhc.core.phase.phases.*
import org.gaseumlabs.uhc.customSpawning.CustomSpawning
import org.gaseumlabs.uhc.customSpawning.CustomSpawningType
import org.gaseumlabs.uhc.database.DataManager
import org.gaseumlabs.uhc.discord.MixerBot
import org.gaseumlabs.uhc.event.Portal
import org.gaseumlabs.uhc.event.Trader
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.team.*
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.*
import org.gaseumlabs.uhc.util.Util.void
import org.gaseumlabs.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import org.bukkit.scoreboard.*
import java.time.Duration
import java.util.*
import kotlin.math.*

object UHC {
	val colorCube: ColorCube = ColorCube(4)

	private var preGameConfig: GameConfig = GameConfig()
	var preGameTeams: Teams<PreTeam> = Teams({ action ->
		Teams.updateNames(action.uuids, action.team)
	}, { team ->
		colorCube.removeTeam(team.colors)
	})

	var game: Game? = null
	var timer = 0
	var countdownTimerGoing = false

	var teleportGroups = HashMap<UUID, Location>()
	var worldRadius: Int = 375

	var dataManager: DataManager = DataManager.offlineDataManager()
	var bot: MixerBot? = null

	lateinit var heartsObjective: Objective

	/* 3 biomes times 6 chunks per biome times 16 blocks per chunk */
	val areaPerTeam = (3 * 6 * 16) * (3 * 6 * 16)
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

	fun startLobby() {
		/* register hearts objective */
		val scoreboard = Bukkit.getServer().scoreboardManager.mainScoreboard

		val objective = scoreboard.getObjective("hp")
			?: scoreboard.registerNewObjective("hp", "health", Component.text("hp"), RenderType.HEARTS)

		objective.renderType = RenderType.HEARTS
		objective.displayName(Component.text("hp"))
		objective.displaySlot = DisplaySlot.PLAYER_LIST

		heartsObjective = objective

		/* lobby spawn */
		Lobby.loadSpawn(WorldManager.lobbyWorld)
		Lobby.loadRadius(WorldManager.lobbyWorld)
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
					CustomSpawning.spawnTick(CustomSpawningType.BLAZE, currentTick, currentGame)

					currentGame.sugarCaneRegen.tick()
					currentGame.leatherRegen.tick()

					currentGame.veinScheduler.tick(currentTick)

					//currentGame.melonRegen.tick()
				}

				Portal.portalTick(currentGame)
				PlayerData.zombieBorderTick(currentTick, currentGame)
				ledgerTrailTick(currentGame, currentTick)

				if (currentTick % 20 == 0) {
					currentGame.updateMobCaps(currentGame.world)
					currentGame.updateMobCaps(currentGame.otherWorld)
					containSpecs()
				}

				val halfWay = (currentGame.config.graceTime.get() + currentGame.config.shrinkTime.get()) * 20 / 2

				if (timer == halfWay) {
					Trader.deployTraders(currentGame)
				}

				if (switchResult) currentGame.nextPhase()
				if (currentGame.phase !is Postgame) ++timer

			} else if (currentTick % 20 == 0 && countdownTimerGoing) {
				++timer

				if (timer < 0) {
					val countdownTitle = Title.title(
						Component.text("${-timer}", countdownColor(-timer), TextDecoration.BOLD),
						Component.text("Game starts in"),
						Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(2))
					)

					preGameTeams.teams().forEach {
						it.members.forEach { uuid ->
							Bukkit.getPlayer(uuid)?.showTitle(countdownTitle)
						}
					}

				} else if (timer == 0) {
					countdownTimerGoing = false

					val (gameWorld, netherWorld) = preGameConfig.getWorlds()
					if (gameWorld == null || netherWorld == null) return@everyTick

					/* add people to team vcs */
					/* make teams finalized */
					val teams: Teams<Team> = Teams({ action ->
						Teams.updateNames(action.uuids, action.team)

						val bot = UHC.bot ?: return@Teams

						if (getConfig().usingBot.get()) when (action) {
							is Teams.ClearAction -> bot.clearTeamVCs()
							is Teams.AddAction -> bot.addToTeamChannel(action.id, action.uuids)
							is Teams.RemoveAction -> bot.removeFromTeamChannel(action.id, action.size, action.uuids)
						}
					}, { team ->
						colorCube.removeTeam(team.colors)
					})

					preGameTeams.transfer(teams, PreTeam::toTeam)

					/* GAME OBJECT */
					val newGame = Game(
						preGameConfig,
						teams,
						worldRadius,
						gameWorld,
						netherWorld
					)

					/* set border in each game dimension */
					listOf(gameWorld, netherWorld).forEach { world ->
						world.worldBorder.setCenter(0.5, 0.5)
						world.worldBorder.size = worldRadius * 2 + 1.0

						world.time = 0
						world.isThundering = false
						world.setStorm(false)
					}

					/* teleport and set playerData to current */
					teleportGroups.forEach { (uuid, location) ->
						newGame.startPlayer(uuid, location)
					}

					game = newGame
				}
			}

			Lobby.lobbyTipsTick(currentTick)
			ArenaManager.perTick(currentTick)

			Bukkit.getOnlinePlayers().forEach { player ->
				UHCBar.updateBar(player)
			}

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
				if (block != null) game.ledger.tracker.addPlayerPosition(uuid, block)
			}
		}
	}

	/**
	 * @param messageStream send status messages and error messages to the caller,
	 * true indicates an error
	 */
	fun startGame(messageStream: (Boolean, String) -> Unit) {
		if (game != null) {
			return messageStream(true, "Game has already started")
		}

		val teams = preGameTeams.teams()

		val numTeams = teams.size
		if (numTeams == 0) {
			return messageStream(true, "No one is playing")
		}

		messageStream(false, "Creating game worlds for $numTeams team${if (numTeams == 1) "" else "s"}")

		worldRadius = radius(numTeams * preGameConfig.scale.get() * areaPerTeam).toInt()

		/* create worlds */
		WorldManager.refreshGameWorlds()

		/* get where players are teleporting */
		val (defaultWorld, otherWorld) = preGameConfig.getWorlds()
		if (defaultWorld == null || otherWorld == null) {
			return messageStream(true, "Worlds did not initialize")
		}

		messageStream(false, "Finding starting locations")

		PlayerSpreader.spreadPlayers(defaultWorld, worldRadius, teams).thenAccept { teleports ->
			/* create the master map of teleport locations */
			teleportGroups = HashMap()

			for (i in teams.indices) {
				val blockList = teleports[i]
				teams[i].members.forEachIndexed { j, uuid ->
					teleportGroups[uuid] = blockList[j].getRelative(0, 1, 0).location.add(0.5, 0.0, 0.5)
				}
			}

			timer = -11
			countdownTimerGoing = true
			preGameConfig.lock = true

			messageStream(false, "Starting UHC")

		}.exceptionally { ex ->
			messageStream(true, "Could not start | ${ex.message}").void()
		}
	}

	fun destroyGame() {
		game?.teams?.clearTeams()
		game?.quirks?.forEach { quirk -> quirk?.onDestroy() }

		game = null
		preGameConfig = GameConfig()

		PlayerData.prune()
		Bukkit.getOnlinePlayers().forEach { player -> Lobby.onSpawnLobby(player) }

		WorldManager.destroyGameWorlds()
	}

	fun containSpecs() {
		val currentGame = game ?: return
		val radius = currentGame.initialRadius

		Bukkit.getOnlinePlayers()
			.filter { it.world === currentGame.world || it.world === currentGame.otherWorld }
			.forEach { player ->
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

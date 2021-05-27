package com.codeland.uhc.core

import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.event.Portal
import com.codeland.uhc.gui.GuiManager
import com.codeland.uhc.gui.SetupGui
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.phase.phases.waiting.WaitingDefault
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.SchedulerUtil
import com.codeland.uhc.world.WorldManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.*
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

object UHC {
	var ledger = Ledger()

	var mobCapCoefficient = 1.0

	val defaultPreset = Preset.MEDIUM

	val defaultVariants = arrayOf(
		PhaseVariant.WAITING_DEFAULT,
		PhaseVariant.GRACE_DEFAULT,
		PhaseVariant.SHRINK_DEFAULT,
		PhaseVariant.ENDGAME_NATURAL_TERRAIN,
		PhaseVariant.POSTGAME_DEFAULT
	)

	private var phaseTimes = arrayOf(
		0,
		defaultPreset.graceTime,
		defaultPreset.shrinkTime,
		0,
		0
	)

	var startRadius = defaultPreset.startRadius
	private set
	var endRadius = defaultPreset.endRadius
	private set
	var elapsedTime = 0

	var currentPhase = null as Phase?

	var teleportGroups: ArrayList<ArrayList<UUID>>? = null
	var teleportLocations: ArrayList<Location>? = null

	/* properties */

	val phaseVariants = Array(PhaseType.values().size) { index ->
		UHCProperty(defaultVariants[index])
	}

	val quirks = Array(QuirkType.values().size) { index ->
		QuirkType.values()[index].createQuirk()
	}

	val preset = UHCProperty<Preset?>(defaultPreset)
	val defaultWorldEnvironment = UHCProperty(World.Environment.NORMAL)
	val naturalRegeneration = UHCProperty(false)
	val killReward = UHCProperty(KillReward.REGENERATION)
	val usingBot = UHCProperty(GameRunner.bot != null)

	val properties = arrayOf(
		preset, defaultWorldEnvironment,
		naturalRegeneration, killReward, usingBot
	)

	val setupGui = GuiManager.register(SetupGui())

	/* property updaters */

	fun updateUsingBot(using: Boolean) {
		val bot = GameRunner.bot

		usingBot.set(if (bot == null) {
			false
		} else {
			if (!using) bot.clearTeamVCs()
			using
		})
	}

	fun updatePreset(preset: Preset) {
		updatePreset(preset, preset.startRadius, preset.endRadius, preset.graceTime, preset.shrinkTime)
	}

	fun updatePreset(startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int) {
		updatePreset(null, startRadius, endRadius, graceTime, shrinkTime)
	}

	private fun updatePreset(preset: Preset?, startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int) {
		this.preset.set(preset)

		this.startRadius = startRadius
		this.endRadius = endRadius

		phaseTimes = arrayOf(0, graceTime, shrinkTime, 0, 0)
	}

	fun updateVariant(phaseVariant: PhaseVariant) {
		val index = phaseVariant.type.ordinal

		phaseVariants[index].set(phaseVariant)
	}

	fun updateQuirk(type: QuirkType, enabled: Boolean) {
		quirks[type.ordinal].enabled.set(enabled)

		if (enabled) type.incompatibilities.forEach { getQuirk(it).enabled.set(false) }
	}

	fun updateTime(phaseType: PhaseType, time: Int) {
		this.preset.set(null)
		phaseTimes[phaseType.ordinal] = time
	}

	fun updateStartRadius(startRadius: Int) {
		this.preset.set(null)
		this.startRadius = startRadius
	}

	fun updateEndRadius(endRadius: Int) {
		this.preset.set(null)
		this.endRadius = endRadius
	}

	fun updateDefaultWorldEnvironment(environment: World.Environment) {
		if (isGameGoing()) return

		defaultWorldEnvironment.set(environment)
	}

	/* state getters */

	fun getVariant(phaseType: PhaseType): PhaseVariant {
		return phaseVariants[phaseType.ordinal].get()
	}

	fun getTime(phaseType: PhaseType): Int {
		return phaseTimes[phaseType.ordinal]
	}

	fun getQuirk(quirkType: QuirkType): Quirk {
		return quirks[quirkType.ordinal]
	}

	fun isEnabled(quirkType: QuirkType): Boolean {
		return quirks[quirkType.ordinal].enabled.get()
	}

	fun isPhase(compare: PhaseType): Boolean {
		return currentPhase?.phaseType === compare
	}

	fun isVariant(compare: PhaseVariant): Boolean {
		return currentPhase?.phaseVariant === compare
	}

	fun isGameGoing(): Boolean {
		return currentPhase?.phaseType?.gameGoing ?: false
	}

	fun getDefaultWorld(): World {
		return if (defaultWorldEnvironment.get() === World.Environment.NORMAL)
			WorldManager.getGameWorldGame()
		else
			WorldManager.getNetherWorldGame()
	}

	/* game flow modifiers */

	/**
	 * should be called when the world is loaded
	 *
	 * starts the waiting phase
	 */
	fun startWaiting() {
		startPhase(PhaseType.WAITING)

		/* begin global ticking task */
		/* holds a centralized list of all general continuous tasks throughout the game */
		var currentTick = 0

		SchedulerUtil.everyTick {
			if (isGameGoing() && !isPhase(PhaseType.ENDGAME)) CustomSpawning.spawnTick(currentTick)

			if (isGameGoing()) PlayerData.zombieBorderTick(currentTick)

			if (isGameGoing()) ledgerTrailTick(currentTick)

			Lobby.lobbyTipsTick(currentTick)

			PvpGameManager.perTick(currentTick)

			Portal.portalTick()

			/* highly composite number */
			currentTick = (currentTick + 1) % 294053760
		}
	}

	fun ledgerTrailTick(currentTick: Int) {
		if (currentTick % 40 != 0) return

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			val player = Bukkit.getPlayer(uuid)

			if (playerData.participating && (player == null || player.gameMode !== GameMode.SPECTATOR)) {
				val block = GameRunner.getPlayerLocation(uuid)?.block

				if (block != null) ledger.addPlayerPosition(uuid, block)
			}
		}
	}

	/**
	 * @param messageStream send status messages and error messages to the caller,
	 * true indicates an error
	 */
	fun canStartUHC(messageStream: (Boolean, String) -> Unit): Pair<ArrayList<ArrayList<UUID>>, ArrayList<Location>> {
		fun errorValue() = Pair(ArrayList<ArrayList<UUID>>(), ArrayList<Location>())

		if (WorldManager.getGameWorld() == null || WorldManager.getNetherWorld() == null) {
			messageStream(true, "Game world has not been initialized, use /uhca worldRefresh")
			return errorValue()
		}

		if (isGameGoing()) {
			messageStream(true, "Game has already started")
			return errorValue()
		}

		/* compile a list of all individuals that will play */
		val individuals = PlayerData.playerDataList
			.filter { (uuid, playerData) -> playerData.staged && !TeamData.isOnTeam(uuid) }
			.map { (uuid, _) -> arrayListOf(uuid) }

		val numGroups = TeamData.teams.size + individuals.size

		if (numGroups == 0) {
			messageStream(true, "No one is playing")
			return errorValue()
		}

		/* get where players are teleporting */
		val world = getDefaultWorld()

		val tempTeleportLocations = GraceDefault.spreadPlayers(
			world,
			numGroups,
			startRadius - 5.0,
			if (world.environment == World.Environment.NETHER) GraceDefault.Companion::findYMid else GraceDefault.Companion::findYTop
		)

		if (tempTeleportLocations.isEmpty()) {
			messageStream(true, "Not enough valid starting locations found")
			return errorValue()
		}

		val tempTeleportGroups = TeamData.teams.map { it.members } as ArrayList<ArrayList<UUID>>
		tempTeleportGroups.addAll(individuals)

		messageStream(false, "Starting UHC")

		return Pair(tempTeleportGroups, tempTeleportLocations)
	}

	/**
	 * actually sets in motion the start of uhc
	 */
	fun startUHC(tempTeleportGroups: ArrayList<ArrayList<UUID>>, tempTeleportLocations: ArrayList<Location>) {
		/* compile teams and individuals into who will teleport to which location */
		teleportGroups = tempTeleportGroups
		teleportLocations = tempTeleportLocations

		/* switch to grace in 4 seconds */
		val waiting = currentPhase as WaitingDefault
		waiting.updateLength(4)
	}

	/**
	 * called any time during the uhc to end it
	 *
	 * cleans up and summarizes the game
	 *
	 * sets the current phase to endgame
	 */
	fun endUHC(winners: List<UUID>) {
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

				var playerString = winners.joinToString(" ") { Bukkit.getOfflinePlayer(it).name ?: "NULL" }
				winners.forEach { ledger.addEntry(Bukkit.getOfflinePlayer(it).name ?: "NULL", elapsedTime, "winning", true) }

				bottomMessage = winningTeam.apply(playerString)
			}

			Title.title(topMessage, bottomMessage, Title.Times.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2)))

		/* no one won the game */
		} else {
			Title.title(Component.text("No one wins?", NamedTextColor.GOLD, TextDecoration.BOLD), Component.empty(), Title.Times.of(Duration.ZERO, Duration.ofSeconds(10), Duration.ofSeconds(2)))
		}

		ledger.createFile()

		Bukkit.getServer().onlinePlayers.forEach { player -> player.showTitle(title) }

		/* remove all teams */
		TeamData.destroyTeam(null, usingBot.get(), true) {}

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
		startPhase(PhaseType.POSTGAME)
	}

	/* starting phases */

	fun startNextPhase() {
		val oldPhase = currentPhase ?: return

		var nextIndex = (oldPhase.phaseType.ordinal + 1) % PhaseType.values().size

		startPhase(nextIndex)
	}

	fun startPhase(phaseType: PhaseType, onInject: (Phase) -> Unit = {}) {
		startPhase(phaseType.ordinal, onInject)
	}

	fun startPhase(phaseIndex: Int, onInject: (Phase) -> Unit = {}) {
		currentPhase?.onEnd()

		val variant = phaseVariants[phaseIndex].get()

		currentPhase = variant.start(phaseTimes[phaseIndex], onInject)

		quirks.filter { it.enabled.get() }.forEach { it.onPhaseSwitch(variant) }
	}

	fun spectatorSpawnLocation(): Location {
		for ((uuid, playerData) in PlayerData.playerDataList) {
			if (playerData.alive && playerData.participating) {
				return GameRunner.getPlayerLocation(uuid)?.clone()?.add(0.0, 2.0, 0.0)
					?: Location(getDefaultWorld(), 0.5, 100.0, 0.5)
			}
		}

		return Location(getDefaultWorld(), 0.5, 100.0, 0.5)
	}

	fun containSpecs() {
		Bukkit.getOnlinePlayers().forEach { player ->
			if (player.gameMode == GameMode.SPECTATOR) {
				val locX = player.location.blockX.toDouble()
				val locZ = player.location.blockZ.toDouble()

				val x = when {
					locX > startRadius -> startRadius.toDouble()
					locX < -startRadius -> -startRadius.toDouble()
					else -> locX
				}

				val z = when {
					locZ > startRadius -> startRadius.toDouble()
					locZ < -startRadius -> -startRadius.toDouble()
					else -> locZ
				}

				if (x != locX || z != locZ) player.teleport(player.location.set(x + 0.5, player.location.y, z + 0.5))
			}
		}
	}

	fun updateMobCaps() {
		val world = getDefaultWorld()

		val borderRadius = world.worldBorder.size / 2

		var spawnModifier = borderRadius / 128.0
		if (spawnModifier > 1.0) spawnModifier = 1.0

		world.     monsterSpawnLimit = (70 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world.      animalSpawnLimit = (10 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world.     ambientSpawnLimit = (15 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world. waterAnimalSpawnLimit = ( 5 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
		world.waterAmbientSpawnLimit = (20 * mobCapCoefficient * spawnModifier).roundToInt().coerceAtLeast(1)
	}
}

package com.codeland.uhc.core

import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phase.*
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.phase.phases.postgame.PostgameDefault
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.TeamData
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.CommandSender
import java.lang.Math.pow
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

class UHC(val defaultPreset: Preset, val defaultVariants: Array<PhaseVariant>) {
	var gameMaster = null as CommandSender?
	var ledger = Ledger()

	var mobCapCoefficient = 1.0
	var killReward = KillReward.REGENERATION

	private var phaseVariants = Array(PhaseType.values().size) { index ->
		defaultVariants[index]
	}

	var quirks = Array(QuirkType.values().size) { index ->
		QuirkType.values()[index].createQuirk(this)
	}

	/* set by init */
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

	/* null if it is a custom preset */
	var preset: Preset? = defaultPreset

	var currentPhase = null as Phase?

	var defaultEnvironment = World.Environment.NORMAL

	var appleFix = true
	var mushroomBlockNerf = true

	var usingBot = GameRunner.bot != null
	private set

	fun updateUsingBot(using: Boolean) {
		val bot = GameRunner.bot

		usingBot = if (bot == null) {
			false

		} else {
			bot.clearTeamVCs()
			using
		}
	}

	val gui = Gui(this)

	val playerDataList = HashMap<UUID, PlayerData>()

	/* access operations for player data list */
	fun isAlive(uuid: UUID): Boolean {
		return getPlayerData(uuid).alive
	}

	fun isParticipating(uuid: UUID): Boolean {
		return getPlayerData(uuid).participating
	}

	fun isOptingOut(uuid: UUID): Boolean {
		return getPlayerData(uuid).optingOut
	}

	fun setAlive(uuid: UUID, alive: Boolean) {
		getPlayerData(uuid).alive = alive
	}

	fun setParticipating(uuid: UUID, participating: Boolean) {
		getPlayerData(uuid).participating = participating
	}

	fun setOptOut(uuid: UUID, optOut: Boolean) {
		getPlayerData(uuid).optingOut = optOut
	}

	fun getPlayerData(uuid: UUID): PlayerData {
		val playerData = playerDataList[uuid]

		return if (playerData == null) {
			val ret = PlayerData(false, false, false)
			playerDataList[uuid] = ret
			ret
		} else {
			playerData
		}
	}

	fun spectatorSpawnLocation(): Location {
		for ((uuid, playerData) in playerDataList) {
			if (playerData.alive) {
				return GameRunner.getPlayerLocation(uuid)?.clone()?.add(0.0, 2.0, 0.0)
					?: Location(Bukkit.getWorlds()[0], 0.5, 100.0, 0.5)
			}
		}

		return Location(Bukkit.getWorlds()[0], 0.5, 100.0, 0.5)
	}

	/* state setters */

	fun updatePreset(preset: Preset) {
		updatePreset(preset, preset.startRadius, preset.endRadius, preset.graceTime, preset.shrinkTime)
	}

	fun updatePreset(startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int) {
		updatePreset(null, startRadius, endRadius, graceTime, shrinkTime)
	}

	private fun updatePreset(preset: Preset?, startRadius: Int, endRadius: Int, graceTime: Int, shrinkTime: Int) {
		this.preset = preset

		this.startRadius = startRadius
		this.endRadius = endRadius

		phaseTimes = arrayOf(
			0,
			graceTime,
			shrinkTime,
			0,
			0
		)
	}

	fun updateTime(phaseType: PhaseType, time: Int) {
		this.preset = null
		phaseTimes[phaseType.ordinal] = time
	}

	fun updateStartRadius(startRadius: Int) {
		this.preset = null
		this.startRadius = startRadius
	}

	fun updateEndRadius(endRadius: Int) {
		this.preset = null
		this.endRadius = endRadius
	}

	fun updateVariant(phaseVariant: PhaseVariant) {
		val index = phaseVariant.type.ordinal

		phaseVariants[index] = phaseVariant
	}

	fun updateQuirk(type: QuirkType, enabled: Boolean) {
		quirks[type.ordinal].enabled = enabled

		if (enabled) type.incompatibilities.forEach { other ->
			var otherQuirk = GameRunner.uhc.getQuirk(other)

			if (otherQuirk.enabled) {
				otherQuirk.enabled = false
				gui.quirkToggles[otherQuirk.type.ordinal].updateDisplay()
			}
		}
	}

	/**
	 * call after object is fully initialized
	 */
	fun updateDisplays() {
		quirks.forEach { quirk -> updateQuirk(quirk.type, quirk.enabled) }
	}

	/* state getters */

	fun getVariant(phaseType: PhaseType): PhaseVariant {
		return phaseVariants[phaseType.ordinal]
	}

	fun getTime(phaseType: PhaseType): Int {
		return phaseTimes[phaseType.ordinal]
	}

	fun getQuirk(quirkType: QuirkType): Quirk {
		return quirks[quirkType.ordinal]
	}

	fun isEnabled(quirkType: QuirkType): Boolean {
		return quirks[quirkType.ordinal].enabled
	}

	fun isPhase(compare: PhaseType): Boolean {
		return currentPhase?.phaseType == compare
	}

	fun isVariant(compare: PhaseVariant): Boolean {
		return currentPhase?.phaseVariant == compare
	}

	fun isGameGoing(): Boolean {
		return currentPhase?.phaseType?.gameGoing ?: false
	}

	fun allCurrentPlayers(action: (uuid: UUID) -> Unit) {
		playerDataList.forEach { (uuid, data) ->
			if (data.alive && data.participating) action(uuid)
		}
	}

	/* game flow modifiers */

	/**
	 * should be called when the world is loaded
	 *
	 * starts the waiting phase
	 */
	fun startWaiting() {
		startPhase(PhaseType.WAITING)
	}

	/**
	 * starts the grace period and ends waiting phase
	 *
	 * @return a string if the game couldn't start
	 */
	fun startUHC(commandSender : CommandSender): String? {
		if (isGameGoing()) return "Game has already started!"

		val numTeams = TeamData.teams.size
		val individuals = ArrayList<UUID>()

		playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating && !TeamData.isOnTeam(uuid)) individuals.add(uuid)
		}

		if (numTeams + individuals.size == 0) return "No one is playing!"

		val teleportLocations = GraceDefault.spreadPlayers(
			Util.worldFromEnvironment(defaultEnvironment), numTeams + individuals.size, startRadius - 5.0,
			if (defaultEnvironment == World.Environment.NETHER) GraceDefault.Companion::findYMid else GraceDefault.Companion::findYTop
		)

		if (teleportLocations.isNotEmpty()) {
			/* compile teams and individuals into who will teleport to which location */
			val teleportGroups = Array(numTeams + individuals.size) { i ->
				if (i < numTeams) {
					val team = TeamData.teams[i]

					Array(team.members.size) { j ->
						team.members[j]
					}
				} else {
					arrayOf(individuals[i - numTeams])
				}
			}

			gameMaster = commandSender

			PlayerData.startZombieBorderTask()

			startPhase(PhaseType.GRACE) { phase ->
				(phase as GraceDefault).teleportGroups = teleportGroups
				phase.teleportLocations = teleportLocations
			}

		} else {
			return "Not enough valid spaces to teleport in this world!"
		}

		return null
	}

	/**
	 * called any time during the uhc to end it
	 *
	 * starts the postgame phase
	 */
	fun endUHC(winners: ArrayList<UUID>) {
		PlayerData.endZombieBorderTask()

		startPhase(PhaseType.POSTGAME) { phase ->
			(phase as PostgameDefault).winners = winners
		}
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

		currentPhase = phaseVariants[phaseIndex].start(this, phaseTimes[phaseIndex], onInject)

		quirks.forEach { quirk ->
			if (quirk.enabled) quirk.onPhaseSwitch(phaseVariants[phaseIndex])
		}
	}

	fun updateMobCaps() {
		val world = Util.worldFromEnvironment(defaultEnvironment)

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

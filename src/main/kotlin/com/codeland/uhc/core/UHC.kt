package com.codeland.uhc.core

import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phase.*
import com.codeland.uhc.phase.Phase
import com.codeland.uhc.phase.phases.grace.GraceDefault
import com.codeland.uhc.phase.phases.postgame.PostgameDefault
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.team.Team
import com.codeland.uhc.team.TeamData
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class UHC(val defaultPreset: Preset, val defaultVariants: Array<PhaseVariant>) {
	var gameMaster = null as CommandSender?
	var ledger = Ledger()

	var mobCapCoefficient = 1.0
	var killReward = KillReward.REGENERATION

	private var phaseVariants = Array(PhaseType.values().size) { index ->
		defaultVariants[index]
	}

	private var quirks = Array(QuirkType.values().size) { index ->
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

	var appleFix = true
	var mushroomBlockNerf = true

	var usingBot = GameRunner.bot != null
	private set

	fun updateUsingBot(using: Boolean) {
		val bot = GameRunner.bot

		usingBot = if (bot == null) {
			false

		} else {
			if (!using) TeamData.teams.forEach { team ->
				bot.destroyTeam(team) {}
			}

			using
		}
	}

	val gui = Gui(this)

	/* state setters */

	fun updatePreset(preset: Preset) {
		updatePreset(preset, preset.startRadius, preset.endRadius, preset.graceTime, preset.shrinkTime)
	}

	fun updatePreset(startRadius: Double, endRadius: Double, graceTime: Int, shrinkTime: Int) {
		updatePreset(null, startRadius, endRadius, graceTime, shrinkTime)
	}

	private fun updatePreset(preset: Preset?, startRadius: Double, endRadius: Double, graceTime: Int, shrinkTime: Int) {
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

	fun updateStartRadius(startRadius: Double) {
		this.preset = null
		this.startRadius = startRadius
	}

	fun updateEndRadius(endRadius: Double) {
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
		if (isGameGoing())
			return "Game has already started!"

		val numTeams = TeamData.teams.size
		if (numTeams == 0) return "No one is playing!"

		gameMaster = commandSender

		val teleportLocations = GraceDefault.spreadPlayers(
			Bukkit.getWorlds()[0], numTeams, startRadius - 5
		)

		if (teleportLocations.isNotEmpty()) {
			startPhase(PhaseType.GRACE) { phase ->
				(phase as GraceDefault).teleportLocations = teleportLocations
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
	fun endUHC(winner: Team?) {
		startPhase(PhaseType.POSTGAME) { phase ->
			(phase as PostgameDefault).winningTeam = winner
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
		val world = Bukkit.getWorlds()[0]

		/* mobCap = constant × chunks ÷ 289                           */
		/* https://minecraft.gamepedia.com/Spawn#Java_Edition_mob_cap */

		/*
		var total = 0.0
		var inBorder = 0.0

		for (chunk in world.loadedChunks) {
			++total

			val width = min(world.worldBorder.size, chunk.x * 16.0 + 16.0) - max(-world.worldBorder.size, chunk.x * 16.0)
			val height = min(world.worldBorder.size, chunk.z * 16.0 + 16.0) - max(-world.worldBorder.size, chunk.z * 16.0)

			if (width < 0 || height < 0) continue

			inBorder += width * height / 256.0
		}

		val coeff = inBorder / total

		world.    monsterSpawnLimit = (70 * coeff * mobCapCoefficient).toInt() + 1
		world.     animalSpawnLimit = (10 * coeff * mobCapCoefficient).toInt() + 1
		world.    ambientSpawnLimit = (15 * coeff * mobCapCoefficient).toInt() + 1
		world.waterAnimalSpawnLimit = ( 5 * coeff * mobCapCoefficient).toInt() + 1
		 */

		val radius = world.worldBorder.size / 2
		var inverseAlong = 1 - (((radius - startRadius) / (endRadius - startRadius)))

		/* range for mobcaps is from [0.25 - 1] */
		inverseAlong *= 0.75
		inverseAlong += 0.25

		world.     monsterSpawnLimit = (70 * inverseAlong * mobCapCoefficient).toInt() + 1
		world.      animalSpawnLimit = (10 * inverseAlong * mobCapCoefficient).toInt() + 1
		world.     ambientSpawnLimit = (15 * inverseAlong * mobCapCoefficient).toInt() + 1
		world. waterAnimalSpawnLimit = ( 5 * inverseAlong * mobCapCoefficient).toInt() + 1
		world.waterAmbientSpawnLimit = (20 * inverseAlong * mobCapCoefficient).toInt() + 1
	}
}

package com.codeland.uhc.core

import CarePackages
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phaseType.*
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.grace.GraceDefault
import com.codeland.uhc.phases.postgame.PostgameDefault
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.scoreboard.Team
import kotlin.math.max
import kotlin.math.min

class UHC(deafultPreset: Preset, defaultVariants: Array<PhaseVariant>) {
	var gameMaster = null as CommandSender?
	var ledger = Ledger()

	/* time is measured in seconds here. */
	var netherToZero = true
	var mobCapCoefficient = 1.0
	var killReward = KillReward.NONE

	private var phaseVariants = Array(PhaseType.values().size) { index ->
		defaultVariants[index]
	}

	private var quirks = Array(QuirkType.values().size) { index ->
		QuirkType.values()[index].createQuirk()
	}

	/* set by init */
	var phaseTimes = emptyArray<Int>()
	var startRadius = 0.0
	var endRadius = 0.0
	var elapsedTime = 0

	var currentPhase = null as Phase?

	var carePackages = CarePackages()

	init {
		updatePreset(deafultPreset)

		phaseVariants.forEach { variant ->
			updateVariant(variant)
		}
	}

	/* state setters */

	fun updatePreset(preset: Preset) {
		startRadius = preset.startRadius
		endRadius = preset.endRadius

		phaseTimes = arrayOf(
			0,
			preset.graceTime,
			preset.shrinkTime,
			preset.finalTime,
			preset.glowTime,
			0,
			0
		)

		Gui.updatePreset(preset)
	}

	fun updateVariant(phaseVariant: PhaseVariant) {
		phaseVariants[phaseVariant.type.ordinal] = phaseVariant

		Gui.updatePhaseVariant(phaseVariant)
	}

	fun updateQuirk(type: QuirkType, enabled: Boolean) {
		quirks[type.ordinal].enabled = enabled

		Gui.updateQuirk(type)

		type.incompatibilities.forEach { other ->
			var otherQuirk = GameRunner.uhc.getQuirk(other)

			if (otherQuirk.enabled) {
				otherQuirk.enabled = false
				Gui.updateQuirk(other)
			}
		}
	}

	/**
	 * call after object is fully initialized
	 */
	fun updateDisplays() {
		quirks.forEach { quirk -> updateQuirk(quirk.type, quirk.enabled) }

		carePackages.onEnable()
		Gui.updateCarePackages(carePackages)
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
		return currentPhase?.phaseType != PhaseType.WAITING && currentPhase?.phaseType != PhaseType.POSTGAME
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

		val numTeams = Bukkit.getScoreboardManager().mainScoreboard.teams.size
		if (numTeams == 0)
			return "No one is playing!"

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
		/* mobCap = constant × chunks ÷ 289                           */
		/* https://minecraft.gamepedia.com/Spawn#Java_Edition_mob_cap */
		for (world in Bukkit.getServer().worlds) {
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
		}
	}
}

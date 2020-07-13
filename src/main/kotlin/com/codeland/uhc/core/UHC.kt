package com.codeland.uhc.core

import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phaseType.*
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.phases.postgame.PostgameDefault
import com.codeland.uhc.quirk.Quirk
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.scoreboard.Team
import kotlin.math.max
import kotlin.math.min

class UHC(var preset: Preset) {
	/* time is measured in seconds here. */
	var netherToZero = true
	var mobCapCoefficient = 1.0
	var killReward = KillReward.NONE

	var gameMaster = null as CommandSender?

	var phaseVariants = Array<PhaseVariant>(7) {PhaseVariant.WAITING_DEFAULT}
	var phaseTimes = emptyArray<Long>()

	init {
		Quirk.values().forEach { quirk ->
			quirk.updateEnabled(false)
		}

		Quirk.APPLE_FIX.updateEnabled(true)

		setVariant(PhaseVariant.WAITING_DEFAULT)
		setVariant(PhaseVariant.GRACE_DEFAULT)
		setVariant(PhaseVariant.SHRINK_DEFAULT)
		setVariant(PhaseVariant.FINAL_DEFAULT)
		setVariant(PhaseVariant.GLOWING_DEFAULT)
		setVariant(PhaseVariant.ENDGAME_NONE)
		setVariant(PhaseVariant.POSTGAME_DEFAULT)
	}

	var currentPhase = null as Phase?
	var currentPhaseIndex = 0

	fun updatePreset(preset: Preset) {
		this.preset = preset

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

	init {
		updatePreset(preset)
	}

	fun startWaiting() {
		startPhase(PhaseType.WAITING)
	}

	/**
	 * @return a string if the game couldn't start
	 */
	fun startUHC(commandSender : CommandSender): String? {
		if (!isPhase(PhaseType.WAITING))
			return "Game has already started!"

		if (Bukkit.getScoreboardManager().mainScoreboard.teams.size == 0)
			return "No one is playing!"

		gameMaster = commandSender

		startPhase(PhaseType.GRACE)

		return null
	}

	fun endUHC(winner: Team?) {
		startPhase(PhaseType.POSTGAME) { phase ->
			phase as PostgameDefault

			phase.winningTeam = winner
		}
	}

	public fun setVariant(phaseVariant: PhaseVariant) {
		phaseVariants[phaseVariant.type.ordinal] = phaseVariant

		Gui.updatePhaseVariant(phaseVariant)
	}

	fun getVariant(phaseType: PhaseType): PhaseVariant {
		return phaseVariants[phaseType.ordinal]
	}

	fun getTime(phaseType: PhaseType): Long {
		return phaseTimes[phaseType.ordinal]
	}

	fun startNextPhase() {
		++currentPhaseIndex

		if (currentPhaseIndex < PhaseType.values().size)
			startPhase(currentPhaseIndex)
	}

	fun startPhase(phaseType: PhaseType, onInject: (Phase) -> Unit = {}) {
		startPhase(phaseType.ordinal, onInject)
	}

	fun startPhase(phaseIndex: Int, onInject: (Phase) -> Unit = {}) {
		currentPhase?.onEnd()

		currentPhaseIndex = phaseIndex

		currentPhase = phaseVariants[phaseIndex].start(this, phaseTimes[phaseIndex], onInject)
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
				if (width < 0 || height < 0) {
					continue
				}
				inBorder += width * height / 256.0
			}
			val coeff = inBorder / total

			world.monsterSpawnLimit = (70 * coeff * mobCapCoefficient).toInt() + 1
			world.animalSpawnLimit = (10 * coeff * mobCapCoefficient).toInt() + 1
			world.ambientSpawnLimit = (15 * coeff * mobCapCoefficient).toInt() + 1
			world.waterAnimalSpawnLimit = (5 * coeff * mobCapCoefficient).toInt() + 1
		}
	}

	fun isPhase(compare: PhaseType): Boolean {
		return currentPhase?.phaseType == compare
	}
}

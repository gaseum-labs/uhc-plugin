package com.codeland.uhc.core

import com.codeland.uhc.gui.Gui
import com.codeland.uhc.phaseType.*
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.quirk.Quirk
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import kotlin.math.max
import kotlin.math.min

class UHC(startRadius: Double, endRadius: Double, graceTime: Long, shrinkTime: Long, finalTime: Long, glowTime: Long) {

	/* time is measured in seconds here. */

	var startRadius = startRadius
	var endRadius = endRadius

	var netherToZero = true
	var mobCapCoefficient = 1.0
	var killReward = KillReward.NONE

	var gameMaster = null as CommandSender?

	var phaseVariants = arrayOf<PhaseVariant>(
		PhaseVariant.WAITING_DEFAULT,
		PhaseVariant.GRACE_DEFAULT,
		PhaseVariant.SHRINK_DEFAULT,
		PhaseVariant.FINAL_DEFAULT,
		PhaseVariant.GLOWING_DEFAULT,
		PhaseVariant.ENDGAME_NONE,
		PhaseVariant.POSTGAME_DEFAULT
	)

	var phaseTimes = arrayOf<Long>(
		0,
		graceTime,
		shrinkTime,
		finalTime,
		glowTime,
		0,
		0
	)

	init {
		Quirk.PESTS.enabled = false
		Quirk.ABUNDANCE.enabled = false
		Quirk.HALF_ZATOICHI.enabled = false
		Quirk.UNSHELTERED.enabled = false
	}

	var currentPhase = null as Phase?
	var currentPhaseIndex = 0

	fun startWaiting() {
		startPhase(PhaseType.WAITING)
	}

	fun startUHC(commandSender : CommandSender) {
		gameMaster = commandSender

		startPhase(PhaseType.GRACE)
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

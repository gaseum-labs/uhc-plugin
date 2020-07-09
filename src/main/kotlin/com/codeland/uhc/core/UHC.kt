package com.codeland.uhc.core

import com.codeland.uhc.phaseType.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import kotlin.math.max
import kotlin.math.min

class UHC(startRadius: Double, endRadius: Double, graceTime: Long, shrinkTime: Long, finalTime: Long, glowTime: Long) {

	/* time is measured in seconds here. */

	var startRadius = startRadius
	var endRadius = endRadius

	init {
		PhaseType.GRACE.time = graceTime
		PhaseType.SHRINK.time = shrinkTime
		PhaseType.FINAL.time = finalTime
		PhaseType.GLOWING.time = glowTime
	}

	var netherToZero = true
	var mobCapCoefficient = 1.0
	var killReward = KillReward.NONE

	var gameMaster = null as CommandSender?

	var currentPhase = PhaseFactory.WAITING_DEFAULT.target
	var currentPhaseIndex = 0

	fun startWaiting() {
		startPhase(PhaseType.WAITING)
	}

	fun startUHC(commandSender : CommandSender) {
		gameMaster = commandSender

		startPhase(PhaseType.GRACE)
	}

	fun startNextPhase() {
		++currentPhaseIndex

		if (currentPhaseIndex < PhaseType.values().size)
			startPhase(currentPhaseIndex)
	}

	fun startPhase(phaseType: PhaseType) {
		startPhase(phaseType.ordinal)
	}

	fun startPhase(phaseIndex: Int) {
		currentPhaseIndex = phaseIndex

		currentPhase = PhaseType.getFactory(phaseIndex).start(this)
	}

	fun updateMobCaps() {
		// mobCap = constant × chunks ÷ 289
		// https://minecraft.gamepedia.com/Spawn#Java_Edition_mob_cap
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
		return currentPhase.phaseType == compare
	}
}

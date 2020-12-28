package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.GameRule

class Christmas(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	fun setSnowing() {
		Bukkit.getWorlds().forEach { world ->
			world.isThundering = false
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
			world.setStorm(true)
		}
	}

	fun revokeSnowing() {
		Bukkit.getWorlds().forEach { world ->
			world.isThundering = false
			world.setGameRule(GameRule.DO_WEATHER_CYCLE, true)
			world.setStorm(false)
		}
	}

	override fun onEnable() {
		if (uhc.currentPhase?.phaseType == PhaseType.GRACE) setSnowing()
	}

	override fun onDisable() {
		revokeSnowing()
	}

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) setSnowing()
	}
}
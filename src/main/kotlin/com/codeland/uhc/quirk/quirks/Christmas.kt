package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Christmas(type: QuirkType) : Quirk(type) {
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
		if (UHC.currentPhase?.phaseType == PhaseType.GRACE) setSnowing()
	}

	override fun onDisable() {
		revokeSnowing()
	}

	override val representation: ItemStack
		get() = ItemStack(Material.SNOWBALL)

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) setSnowing()
	}
}

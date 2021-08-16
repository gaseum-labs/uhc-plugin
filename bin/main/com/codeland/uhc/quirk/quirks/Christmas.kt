package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.core.phase.PhaseType
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Material

class Christmas(type: QuirkType, game: Game) : Quirk(type, game) {
	init {
		setSnowing()
	}

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

	override fun customDestroy() {
		revokeSnowing()
	}
}

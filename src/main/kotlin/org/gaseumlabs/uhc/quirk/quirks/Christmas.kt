package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.GameRule

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

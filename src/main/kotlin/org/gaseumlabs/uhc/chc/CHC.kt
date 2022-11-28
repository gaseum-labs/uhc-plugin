package org.gaseumlabs.uhc.chc

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.phase.Phase
import org.bukkit.event.Listener
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.GamePreset
import java.util.*

abstract class CHC<DataType> {
	fun onDestroy(game: Game) {
		customDestroy(game)
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) onEndPlayer(game, uuid)
			playerData.deleteQuirkData()
		}
	}

	fun onStartGame(game: Game) {
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) onStartPlayer(game, uuid)
		}
	}

	abstract fun defaultData(): DataType

	open fun gamePreset(): GamePreset? = null
	open fun onPhaseSwitch(game: Game, phase: Phase) {}
	open fun onStartPlayer(game: Game, uuid: UUID) {}
	open fun onEndPlayer(game: Game, uuid: UUID) {}
	open fun eventListener(): Listener? = null
	open fun customDestroy(game: Game) {}
}

abstract class NoDataCHC: CHC<Nothing?>() {
	final override fun defaultData() = null
}

package com.codeland.uhc.quirk

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.phase.Phase
import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.dropFix.DropFix
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class Quirk(val type: QuirkType, val game: Game) {
	/* when this quirk is created, start for all players already in the game */
	init {
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) {
				PlayerData.getQuirkDataHolder(playerData, type, game).applied = true
				onStartPlayer(uuid)
			}
		}
	}

	/* when this quirk is destroyed, end for all players still in the game */
	fun onDestroy() {
		customDestroy()

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) {
				val quirkDataHolder = PlayerData.getQuirkDataHolder(playerData, type, game)

				if (quirkDataHolder.applied) {
					onEndPlayer(uuid)
					quirkDataHolder.applied = false
				}
			}
		}
	}

	open fun customDestroy() {}

	open fun onStartPlayer(uuid: UUID) {}
	open fun onEndPlayer(uuid: UUID) {}

	open fun defaultData(): Any = 0
	open fun onPhaseSwitch(phase: Phase) {}

	protected open fun customDrops(): Array<DropFix>? = null
	protected open fun customSpawnInfos(): Array<SpawnInfo>? = null

	val customDrops = customDrops()
	val spawnInfos = customSpawnInfos()

	/* event wrappers (makes them compatible with uhc event flow) */
	/* more will be added */

	/**
	 * returns true if it replaces drops entirely and other
	 * quirks / dropfix should not be applied
	 */
	open fun modifyEntityDrops(entity: Entity, killer: Player?, drops: MutableList<ItemStack>) = false
}

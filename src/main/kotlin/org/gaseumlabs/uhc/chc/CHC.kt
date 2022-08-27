package org.gaseumlabs.uhc.chc

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.phase.Phase
import org.gaseumlabs.uhc.customSpawning.SpawnEntry
import org.gaseumlabs.uhc.dropFix.DropFix
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class CHC<DataType>(val type: CHCType, val game: Game) {
	/* when this quirk is created, start for all players already in the game */
	init {
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) {
				onStartPlayer(uuid)
				playerData.getQuirkDataHolder(this).applied = true
			}
		}
	}

	/* when this quirk is destroyed, end for all players still in the game */
	fun onDestroy() {
		customDestroy()
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) {
				val quirkDataHolder = playerData.getQuirkDataHolder(this)
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
	open fun eventListener(): Listener? = null

	abstract fun defaultData(): DataType
	open fun onPhaseSwitch(phase: Phase) {}

	protected open fun customDrops(): Array<DropFix>? = null
	protected open fun customSpawnInfos(): Array<SpawnEntry>? = null

	val customDrops = customDrops()
	val spawnInfos = customSpawnInfos()

	/**
	 * @return true if it replaces drops entirely and dropfix should not be applied
	 */
	open fun modifyEntityDrops(entity: Entity, killer: Player?, drops: MutableList<ItemStack>) = false
}

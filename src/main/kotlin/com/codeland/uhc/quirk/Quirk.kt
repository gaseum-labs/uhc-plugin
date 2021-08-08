package com.codeland.uhc.quirk

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.dropFix.DropFix
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.phase.Phase
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class Quirk(val type: QuirkType, val game: Game) {
	/* when this quirk is created, start for all players already in the game */
	init {
		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) {
				PlayerData.getQuirkDataHolder(playerData, type).applied = true
				onStartPlayer(uuid)
			}
		}
	}

	/* when this quirk is destroyed, end for all players still in the game */
	fun onDestroy() {
		customDestroy()

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (playerData.participating) {
				val quirkDataHolder = PlayerData.getQuirkDataHolder(playerData, type)

				if (quirkDataHolder.applied) {
					onEndPlayer(uuid)
					quirkDataHolder.applied = false
				}
			}
		}
	}

	//TEMPORARILY DISABLED
	/*
	private val properties = ArrayList<UHCProperty<*>>()

	val gui: GuiPage = GuiManager.register(GuiPage(5, Component.text(type.prettyName)))

	init {
		val backgroundItem = ItemUtil.namedItem(Material.BLACK_STAINED_GLASS_PANE, "${ChatColor.RESET}${ChatColor.BLACK}_")
		val internal = gui.inventory
		for (i in 0 until internal.size - 1) {
			internal.setItem(i, backgroundItem)
		}

		gui.addItem(object : GuiItem(gui.inventory.size - 1) {
			override fun onClick(player: Player, shift: Boolean) {
				if (shift)
					this@Quirk.gui.close(player)
				else
					UHC.setupGui.open(player)
			}

			override fun getStack(): ItemStack {
				return ItemCreator.fromType(Material.PRISMARINE_SHARD)
					.name(Component.text("Back", NamedTextColor.BLUE))
					.create()
			}
		})

		customDrops?.sortBy { dropFix -> dropFix.entityType }
	}

	protected fun <T> addProperty(property: UHCProperty<T>): UHCProperty<T> {
		properties.add(property)
		return property
	}

	fun resetProperties() = properties.forEach { it.reset() }
    */

	abstract fun representation(): ItemCreator

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

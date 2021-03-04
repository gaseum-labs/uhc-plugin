package com.codeland.uhc.quirk

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.dropFix.DropFix
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.util.ItemUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList

abstract class Quirk(val uhc: UHC, val type: QuirkType) {
	/* default value will be set upon init */
	var enabled = type.defaultEnabled
	set(value) {
		/* enable / disable functions come first */
		if (value) {
			/* start all players if the game is already going */
			if (uhc.isGameGoing()) {
				PlayerData.playerDataList.forEach { (uuid, playerData) ->
					if (playerData.participating) {
						/* mark that they have been applied */
						PlayerData.getQuirkDataHolder(playerData, type).applied = true
						onStart(uuid)
					}
				}
			}

			onEnable()

		} else {
			/* revoke applied status from all players who were applied by this quirk */
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				val quirkDataHolder = PlayerData.getQuirkDataHolder(playerData, type)
				if (quirkDataHolder.applied) {
					onEnd(uuid)
					quirkDataHolder.applied = false
				}
			}

			onDisable()
		}

		field = value
	}

	private val properties = ArrayList<BoolProperty>()

	val inventory: GuiInventory = GuiInventory(4, type.prettyName)

	val drops = customDrops()
	val spawnInfos = customSpawnInfos()

	init {
		val backgroundItem = ItemUtil.namedItem(Material.BLACK_STAINED_GLASS_PANE, "${ChatColor.RESET}${ChatColor.BLACK}_")
		val internal = inventory.inventory
		for (i in 0 until internal.size - 1) {
			internal.setItem(i, backgroundItem)
		}

		inventory.addItem(object : GuiItem(uhc, inventory.inventory.size - 1, false) {
			override fun onClick(player: Player, shift: Boolean) {
				if (shift)
					inventory.close(player)
				else
					uhc.gui.inventory.open(player)
			}
			override fun getStack(): ItemStack {
				return setName(ItemStack(Material.PRISMARINE_SHARD), "${ChatColor.BLUE}Back")
			}
		})
	}

	protected fun addProperty(property: BoolProperty): BoolProperty {
		properties.add(property)
		return property
	}

	fun resetProperties() {
		properties.forEach { property ->
			property.value = property.defaultValue
		}
	}

	abstract fun onEnable()
	abstract fun onDisable()

	open fun onStart(uuid: UUID) {}
	open fun onEnd(uuid: UUID) {}

	open fun defaultData(): Any = 0
	open fun onPhaseSwitch(phase: PhaseVariant) {}
	open fun customDrops(): Array<DropFix>? = null
	open fun customSpawnInfos(): Array<SpawnInfo>? = null
}
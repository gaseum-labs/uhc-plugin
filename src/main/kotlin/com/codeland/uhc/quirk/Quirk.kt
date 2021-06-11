package com.codeland.uhc.quirk

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.dropFix.DropFix
import com.codeland.uhc.gui.GuiPage
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiManager
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.util.ItemUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList

abstract class Quirk(val type: QuirkType) {
	/* default value will be set upon init */
	val enabled: UHCProperty<Boolean> = UHCProperty(false) { set ->
		if (set) onEnable() else onDisable()

		PlayerData.playerDataList.forEach { (uuid, playerData) ->
			if (set) {
				if (playerData.participating) {
					PlayerData.getQuirkDataHolder(playerData, type).applied = true
					onStart(uuid)
				}
			} else {
				val quirkDataHolder = PlayerData.getQuirkDataHolder(playerData, type)

				if (quirkDataHolder.applied) {
					onEnd(uuid)
					quirkDataHolder.applied = false
				}
			}
		}

		if (set) type.incompatibilities.forEach {
			val other = UHC.getQuirk(it)
			if (other.enabled.get()) other.enabled.set(false)
		}

		set
	}

	fun toggleEnabled() = enabled.set(!enabled.get())

	private val properties = ArrayList<UHCProperty<*>>()

	val gui: GuiPage = GuiManager.register(GuiPage(5, Component.text(type.prettyName)))

	val customDrops = customDrops()
	val spawnInfos = customSpawnInfos()

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
				return name(ItemStack(Material.PRISMARINE_SHARD), Component.text("Back", NamedTextColor.BLUE))
			}
		})

		customDrops?.sortBy { dropFix -> dropFix.entityType }
	}

	protected fun <T> addProperty(property: UHCProperty<T>): UHCProperty<T> {
		properties.add(property)
		return property
	}

	fun resetProperties() = properties.forEach { it.reset() }

	abstract fun onEnable()
	abstract fun onDisable()

	abstract val representation: ItemStack

	open fun onStart(uuid: UUID) {}
	open fun onEnd(uuid: UUID) {}

	open fun defaultData(): Any = 0
	open fun onPhaseSwitch(phase: PhaseVariant) {}
	open fun customDrops(): Array<DropFix>? = null
	open fun customSpawnInfos(): Array<SpawnInfo>? = null

	/* event wrappers (makes them compatible with uhc event flow) */
	/* more will be added */

	/**
	 * returns true if it replaces drops entirely and other
	 * quirks / dropfix should not be applied
	 */
	open fun modifyEntityDrops(entity: Entity, killer: Player?, drops: MutableList<ItemStack>) = false
}

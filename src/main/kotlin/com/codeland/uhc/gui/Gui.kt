package com.codeland.uhc.gui

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.guiItem.CarePackageCycler
import com.codeland.uhc.gui.guiItem.PresetCycler
import com.codeland.uhc.gui.guiItem.QuirkToggle
import com.codeland.uhc.gui.guiItem.VariantCycler
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Gui(val uhc: UHC) {
	companion object {
		const val INVENTORY_WIDTH = 9
		const val INVENTORY_SIZE = INVENTORY_WIDTH * 4
	}

	var inventory = Bukkit.createInventory(null, INVENTORY_SIZE, "UHC Setup")
	var guiItems = arrayOfNulls<GuiItem>(INVENTORY_SIZE)

	val quirkToggles: Array<QuirkToggle>
	val variantCylers: Array<VariantCycler>

	val presetCycler: PresetCycler
	val carePackageCycler: CarePackageCycler
	val cancelButton: GuiItem

	init {
		quirkToggles = Array(QuirkType.values().size) { i ->
			addItem(QuirkToggle(this, uhc, i, QuirkType.values()[i]))
		}

		variantCylers = Array(PhaseType.values().size) { i ->
			addItem(VariantCycler(this, uhc, i + (INVENTORY_WIDTH * 2), PhaseType.values()[i]))
		}

		presetCycler = addItem(PresetCycler(this, uhc, INVENTORY_WIDTH * 3))
		carePackageCycler = addItem(CarePackageCycler(this, uhc, INVENTORY_WIDTH * 3 + 1))

		val thisGui = this
		cancelButton = addItem(object : GuiItem(thisGui, uhc, INVENTORY_SIZE - 1, false) {
			override fun onClick(player: Player) = close(player)
			override fun getStack() = ItemStack(Material.BARRIER)
		})
	}

	fun open(player: Player) {
		player.openInventory(inventory)
	}

	fun close(player: Player) {
		player.closeInventory()
	}

	/* positioning and updating */

	private fun coordinateToIndex(x: Int, y: Int): Int {
		return y * INVENTORY_WIDTH + x
	}

	private fun IndexToCoordinate(index: Int): Pair<Int, Int> {
		return Pair(index % INVENTORY_WIDTH, index / INVENTORY_WIDTH)
	}

	private fun <ItemType : GuiItem> addItem(guiItem: ItemType): ItemType {
		val index = guiItem.index

		inventory.setItem(index, guiItem.getStack())
		guiItems[index] = guiItem

		val guiStack = inventory.getItem(index) ?: return guiItem
		guiItem.guiStack = guiStack

		return guiItem
	}

	private fun removeItem(x: Int, y: Int) {
		val index = coordinateToIndex(x, y)
		removeItem(index)
	}

	private fun removeItem(index: Int) {
		inventory.setItem(index, null)
		guiItems[index] = null
	}
}
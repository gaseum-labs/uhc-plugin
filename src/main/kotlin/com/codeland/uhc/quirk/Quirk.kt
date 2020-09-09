package com.codeland.uhc.quirk

import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiInventory
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.util.ItemUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class Quirk(val uhc: UHC, val type: QuirkType) {
	/* default value will be set upon init */
	var enabled = type.defaultEnabled
	set(value) {
		/* enable / disable functions come first */
		if (value) {
			onEnable()
		} else {
			onDisable()
		}

		field = value
	}

	val inventory: GuiInventory = GuiInventory(4, type.prettyName)

	init {
		val backgroundItem = ItemUtil.namedItem(Material.BLACK_STAINED_GLASS_PANE, "${ChatColor.RESET}${ChatColor.BLACK}_")
		val internal = inventory.inventory
		for (i in 0 until internal.size - 1) {
			internal.setItem(i, backgroundItem)
		}

		inventory.addItem(object : GuiItem(inventory, uhc, inventory.inventory.size - 1, false) {
			override fun onClick(player: Player, shift: Boolean) {
				if (shift)
					inventory.close(player)
				else
					uhc.gui.inventory.open(player)
			}
			override fun getStack(): ItemStack {
				return setName(ItemStack(Material.PRISMARINE_SHARD), "${ChatColor.RESET}${ChatColor.BLUE}Back")
			}
		})
	}

	abstract fun onEnable()
	abstract fun onDisable()

	open fun onPhaseSwitch(phase: PhaseVariant) {}
}
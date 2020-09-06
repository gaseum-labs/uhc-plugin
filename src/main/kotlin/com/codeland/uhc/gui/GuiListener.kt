package com.codeland.uhc.gui

import com.codeland.uhc.core.UHC
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class GuiListener(val gui: Gui, val uhc: UHC) : Listener {
	@EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
	    /* only work on this inventory */
	    if (event.inventory !== gui.inventory)
	        return

	    event.isCancelled = true

	    /* make sure it is in top inventory not player's */
	    val slot = event.rawSlot
		if (slot >= Gui.INVENTORY_SIZE || slot < 0)
            return

        val guiItem = gui.guiItems[slot] ?: return

        /* only ops may modify */
        val player = event.whoClicked
        if (guiItem.opOnly && !player.isOp) return

        /* do guiItem action */
        guiItem.onClick(player as Player)

		/* then change its item to reflect the action */
		guiItem.updateDisplay()
    }
}

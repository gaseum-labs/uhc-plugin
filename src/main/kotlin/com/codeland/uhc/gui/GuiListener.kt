package com.codeland.uhc.gui;

import com.codeland.uhc.gui.Gui.inventory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class GuiListener : Listener {
	@EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
	    /* only work on this inventory */
	    if (event.inventory !== inventory)
	        return

	    event.isCancelled = true

	    /* make sure it is in top inventory not player's */
	    val slot = event.rawSlot
		if (slot >= Gui.INVENTORY_SIZE || slot < 0)
            return

        val guiItem = Gui.guiItems[slot]
            ?: return

        /* only ops may modify */
        val player = event.whoClicked
        if (!player.isOp)
            return

        /* do guiItem action */
        guiItem.onClick(guiItem, player as Player)
    }
}

package com.codeland.uhc.gui

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.Quirk
import org.bukkit.inventory.ItemStack

class QuirkToggle(stack: ItemStack, quirk: Quirk) : GuiItem(stack, { gui, guiItem, player ->
    if (quirk.enabled) {
        quirk.enabled = false
        guiItem.setDisplayDisabled(quirk.name)
    } else {
        quirk.enabled = true
        guiItem.setDisplayEnabled(quirk.name)
    }

    gui.inventory.setItem(guiItem.index, guiItem.stack)
}) {

    var quirk = quirk

    init {
        setDisplayDisabled(quirk.name)
    }
}
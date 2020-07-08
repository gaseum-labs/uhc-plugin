package com.codeland.uhc.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GuiItem(stack: ItemStack, onClick: (Player) -> Unit) {
    var stack = stack

    var onClick = onClick

    var index = 0
    var x = 0
    var y = 0
}
package com.codeland.uhc.gui

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class GuiItem(var opOnly: Boolean, var stack: ItemStack, var onClick: (GuiItem, Player) -> Unit) {
    var index = 0

    /**
     * called when the guiItem is added
     *
     * the stack you pass in is not the same stack in the inventory
     * this makes the internal stack the one from the inventory
     */
    fun updateStack(newStack: ItemStack) {
        stack = newStack

        val meta = stack.itemMeta
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE)
        meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED)
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR)
        meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS)
        stack.itemMeta = meta
    }

    fun changeStackType(material: Material) {
        stack.type = material
    }

    fun setName(name: String) {
        val meta = stack.itemMeta
        meta.setDisplayName(name)
        stack.itemMeta = meta
    }

    fun setLore(lore: List<String>) {
        val meta = stack.itemMeta
        meta.lore = lore
        stack.itemMeta = meta
    }
}
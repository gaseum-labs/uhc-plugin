package com.codeland.uhc.gui.item

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object AntiSoftlock {
	val MATERIAL = Material.DRIED_KELP

	fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.setDisplayName("${ChatColor.RESET}${ChatColor.BLUE}Anti Softlock")
		meta.lore = listOf("Right click to respawn")

		stack.itemMeta = meta
		return stack
	}

	fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	fun hasItem(inventory: Inventory): Boolean {
		return inventory.contents.any { stack ->
			if (stack == null) return@any false

			isItem(stack)
		}
	}
}

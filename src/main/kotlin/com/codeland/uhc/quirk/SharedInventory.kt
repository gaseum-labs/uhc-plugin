package com.codeland.uhc.quirk;

import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class SharedInventory(type: QuirkType) : Quirk(type) {
	companion object {
		lateinit var inventory: Inventory
	}

	override fun onEnable() {
		inventory = Bukkit.createInventory(null, InventoryType.PLAYER)
		inventory.clear()
	}

	override fun onDisable() {

	}
}

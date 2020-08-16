package com.codeland.uhc.quirk;

import com.codeland.uhc.core.GameRunner
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SharedInventory(type: QuirkType) : Quirk(type) {
	companion object {
		var contents: Array<out ItemStack?> = emptyArray()
	}

	override fun onEnable() {
		GameRunner.plugin.server.scheduler.scheduleSyncRepeatingTask(GameRunner.plugin, {
			for (player in Bukkit.getServer().onlinePlayers) {
				val playersContents = player.inventory.contents
				if (!playersContents.contentEquals(contents)) {
					contents = playersContents.copyOf()
					for (other in Bukkit.getServer().onlinePlayers) {
						if (other != player) other.inventory.contents = playersContents
					}
					// this sucks because it'll override any other inventory changes
					// todo: fix
					break
				}
			}
		}, 1, 1)
	}

	fun contentsCopy(arr: Array<ItemStack?>): Array<ItemStack?> {
		val newArr = arr.copyOf()
		for (i in newArr.indices) {
			newArr[i] = if (arr[i] != null) arr[i] else null
		}
		return newArr
	}

	override fun onDisable() {

	}
}

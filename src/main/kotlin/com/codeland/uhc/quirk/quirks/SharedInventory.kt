package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SharedInventory(type: QuirkType) : Quirk(type) {
	companion object {
		lateinit var contents: Array<out ItemStack?>
		var taskId: Int = 0
	}

	override fun onEnable() {
		contents = Bukkit.getOnlinePlayers().first().inventory.contents

		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			Bukkit.getOnlinePlayers().any { player ->
				val playersContents = player.inventory.contents

				if (!contentsSimilar(contents, playersContents)) {
					contents = contentsCopy(playersContents)

					Bukkit.getOnlinePlayers().forEach { otherPlayer ->
						if (otherPlayer != player) otherPlayer.inventory.contents = contents as Array<out ItemStack>
					}

					true
				}

				false
			}
		}, 1, 1)
	}

	private fun contentsSimilar(contents1: Array<out ItemStack?>, contents2: Array<out ItemStack?>): Boolean {
		for (i in contents1.indices) if (contents1[i] != contents2[i]) return false

		return true
	}

	private fun contentsCopy(contents: Array<out ItemStack?>): Array<out ItemStack?> {
		return Array(contents.size) { i -> contents[i]?.clone() }
	}

	override fun onDisable() {
		Bukkit.getScheduler().cancelTask(taskId)
	}

	override val representation: ItemStack
		get() = ItemStack(Material.KNOWLEDGE_BOOK)
}
package com.codeland.uhc.gui

import CarePackages
import com.codeland.uhc.core.GameRunner
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class CarePackageCycler : GuiItem(true, ItemStack(Material.CHEST),
	{ guiItem, player ->
		guiItem as CarePackageCycler

		val carePackages = GameRunner.uhc.carePackages

		when (asIndex(carePackages)) {
			0 -> { carePackages.enabled = true; carePackages.setFastMode(true) }
			1 -> { carePackages.enabled = false; }
			2 -> { carePackages.enabled = true; carePackages.setFastMode(false) }
		}
	}
) {
	/**
	 * what state the carepackages is in
	 * state 0: on and slow
	 * state 1: on and fast
	 * state 2: off
	 */
	companion object {
		fun asIndex(carePackages: CarePackages): Int {
			return if (GameRunner.uhc.carePackages.enabled) {
				if (!GameRunner.uhc.carePackages.fastMode) 0 else 1
			} else 2
		}
	}

	private fun makeItem(material: Material, name: String) {
		changeStackType(material)

		setName("${ChatColor.RESET}${ChatColor.WHITE}Care packages ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${name}")
		setLore(listOf("Fight over periodic falling chests containing good loot"))
	}

	fun updateDisplay(carePackages: CarePackages) {
		when(asIndex(carePackages)) {
			0 -> makeItem(Material.CHEST, "On")
			1 -> makeItem(Material.ENDER_CHEST, "Chaotic")
			2 -> makeItem(Material.OAK_PLANKS, "Off")
		}
	}
}

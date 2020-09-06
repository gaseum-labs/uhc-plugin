package com.codeland.uhc.gui.guiItem

import CarePackages
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.Gui
import com.codeland.uhc.gui.GuiItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class CarePackageCycler(gui: Gui, uhc: UHC, index: Int) : GuiItem(gui, uhc, index, true) {
	override fun onClick(player: Player) {
		val carePackages = uhc.carePackages

		when (asIndex(carePackages)) {
			0 -> { carePackages.enabled = true; carePackages.setFastMode(true) }
			1 -> { carePackages.enabled = false; }
			2 -> { carePackages.enabled = true; carePackages.setFastMode(false) }
		}
	}

	override fun getStack(): ItemStack {
		return when(asIndex(uhc.carePackages)) {
			0 -> makeItem(Material.CHEST, "On")
			1 -> makeItem(Material.ENDER_CHEST, "Chaotic")
			2 -> makeItem(Material.OAK_PLANKS, "Off")
			else -> ItemStack(Material.POTION)
		}
	}

	/**
	 * what state the carepackages is in
	 * state 0: on and slow
	 * state 1: on and fast
	 * state 2: off
	 */
	companion object {
		private fun asIndex(carePackages: CarePackages): Int {
			return if (carePackages.enabled) {
				if (!carePackages.fastMode) 0 else 1
			} else 2
		}

		private fun makeItem(material: Material, name: String): ItemStack {
			val stack = ItemStack(material)

			setName(stack, "${ChatColor.RESET}${ChatColor.WHITE}Care packages ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}${name}")
			setLore(stack, listOf("Fight over periodic falling chests containing good loot"))

			return stack
		}
	}
}

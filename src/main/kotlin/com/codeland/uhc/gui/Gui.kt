package com.codeland.uhc.gui

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.guiItem.*
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Gui(val uhc: UHC) {
	val listener = GuiListener()

	var inventory = GuiInventory(4, "UHC Setup")

	val quirkToggles: Array<QuirkToggle>
	val variantCylers: Array<VariantCycler>

	val presetCycler: PresetCycler
	val carePackageCycler: CarePackageCycler
	val botToggle: BotToggle

	val resetButton: GuiItem
	val cancelButton: GuiItem

	init {
		quirkToggles = Array(QuirkType.values().size) { i ->
			val item = inventory.addItem(QuirkToggle(inventory, uhc, i, QuirkType.values()[i]))

			listener.registerInventory(uhc.getQuirk(QuirkType.values()[i]).inventory)

			item
		}

		variantCylers = Array(PhaseType.values().size) { i ->
			inventory.addItem(VariantCycler(inventory, uhc, i + (GuiInventory.WIDTH * 2), PhaseType.values()[i]))
		}

		presetCycler = inventory.addItem(PresetCycler(inventory, uhc, GuiInventory.WIDTH * 3))
		carePackageCycler = inventory.addItem(CarePackageCycler(inventory, uhc, GuiInventory.WIDTH * 3 + 1))
		botToggle = inventory.addItem(BotToggle(inventory, uhc, GuiInventory.WIDTH * 3 + 2))

		resetButton = inventory.addItem(object : GuiItem(inventory, uhc, inventory.inventory.size - 2, true) {
			override fun onClick(player: Player, shift: Boolean) {
				uhc.updatePreset(uhc.defaultPreset)
				uhc.defaultVariants.forEach { variant ->
					uhc.updateVariant(variant)
				}
				QuirkType.values().forEach { quirkType ->
					uhc.updateQuirk(quirkType, quirkType.defaultEnabled)
				}
				uhc.updateCarePackages(enabled = true, fast = false)
				uhc.updateUsingBot(true)
			}
			override fun getStack(): ItemStack {
				val stack = ItemStack(Material.MUSIC_DISC_WAIT)
				setName(stack, "${ChatColor.RESET}${ChatColor.AQUA}Reset")
				return stack
			}
		})

		cancelButton = inventory.addItem(object : GuiItem(inventory, uhc, inventory.inventory.size - 1, false) {
			override fun onClick(player: Player, shift: Boolean) = inventory.close(player)
			override fun getStack(): ItemStack {
				val stack = ItemStack(Material.BARRIER)
				setName(stack, "${ChatColor.RESET}${ChatColor.RED}Close")
				return stack
			}
		})

		Bukkit.getPluginManager().registerEvents(listener, UHCPlugin.plugin)
		listener.registerInventory(inventory)
	}
}

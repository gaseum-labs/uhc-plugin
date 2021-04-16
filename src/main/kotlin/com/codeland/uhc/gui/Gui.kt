package com.codeland.uhc.gui

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.KillReward
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.guiItem.*
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Gui(val uhc: UHC) {
	val listener = GuiListener()

	var inventory = GuiInventory(5, "UHC Setup")

	val quirkToggles: Array<QuirkToggle> = Array(QuirkType.values().size) { i ->
		val item = inventory.addItem(QuirkToggle(uhc, i, QuirkType.values()[i]))

		listener.registerInventory(uhc.getQuirk(QuirkType.values()[i]).inventory)

		item
	}

	val variantCylers: Array<VariantCycler> = Array(PhaseType.values().size) { i ->
		inventory.addItem(VariantCycler(uhc, i + (GuiInventory.WIDTH * 3), PhaseType.values()[i]))
	}

	val presetCycler: PresetCycler = inventory.addItem(PresetCycler(uhc, GuiInventory.WIDTH * 4))
	val killRewardCycler: KillRewardCycler = inventory.addItem(KillRewardCycler(uhc, GuiInventory.WIDTH * 4 + 1))
	val botToggle: BotToggle = inventory.addItem(BotToggle(uhc, GuiInventory.WIDTH * 4 + 2))
	val defaultEnvironmentCycler: DefaultEnvironmentCycler = inventory.addItem(DefaultEnvironmentCycler(uhc, GuiInventory.WIDTH * 4 + 3))
	val naturalRegenerationToggle: NaturalRegenerationToggle = inventory.addItem(NaturalRegenerationToggle(uhc, GuiInventory.WIDTH * 4 + 4))

	private val resetButton: GuiItem = inventory.addItem(object : GuiItem(uhc, inventory.inventory.size - 2, true) {
		override fun onClick(player: Player, shift: Boolean) {
			uhc.updatePreset(uhc.defaultPreset)
			presetCycler.updateDisplay()

			uhc.killReward = KillReward.REGENERATION
			killRewardCycler.updateDisplay()

			uhc.defaultVariants.forEach { variant ->
				uhc.updateVariant(variant)
				variantCylers[variant.type.ordinal].updateDisplay()
			}

			QuirkType.values().forEach { quirkType ->
				uhc.updateQuirk(quirkType, false)
				quirkToggles[quirkType.ordinal].updateDisplay()

				val quirk = uhc.getQuirk(quirkType)
				quirk.resetProperties()
				quirk.inventory.guiItems.forEach { guiItem ->
					guiItem?.updateDisplay()
				}
			}

			uhc.updateUsingBot(true)
			botToggle.updateDisplay()

			uhc.defaultWorldIndex = 0
			defaultEnvironmentCycler.updateDisplay()

			uhc.naturalRegeneration = false
			naturalRegenerationToggle.updateDisplay()
		}
		override fun getStack(): ItemStack {
			val stack = ItemStack(Material.MUSIC_DISC_WAIT)
			setName(stack, "${ChatColor.AQUA}Reset")
			return stack
		}
	})

	private val cancelButton: GuiItem = inventory.addItem(object : GuiItem(uhc, inventory.inventory.size - 1, false) {
		override fun onClick(player: Player, shift: Boolean) = inventory.close(player)
		override fun getStack(): ItemStack {
			val stack = ItemStack(Material.BARRIER)
			setName(stack, "${ChatColor.RED}Close")
			return stack
		}
	})

	init {
		Bukkit.getPluginManager().registerEvents(listener, UHCPlugin.plugin)
		listener.registerInventory(inventory)
	}
}

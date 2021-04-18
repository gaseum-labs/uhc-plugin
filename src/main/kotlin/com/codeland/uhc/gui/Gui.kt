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

class Gui {
	val listener = GuiListener()

	var inventory = GuiInventory(5, "UHC Setup")

	val quirkToggles: Array<QuirkToggle> = Array(QuirkType.values().size) { i ->
		val item = inventory.addItem(QuirkToggle(i, QuirkType.values()[i]))

		listener.registerInventory(UHC.getQuirk(QuirkType.values()[i]).inventory)

		item
	}

	val variantCylers: Array<VariantCycler> = Array(PhaseType.values().size) { i ->
		inventory.addItem(VariantCycler(i + (GuiInventory.WIDTH * 3), PhaseType.values()[i]))
	}

	val presetCycler: PresetCycler = inventory.addItem(PresetCycler(GuiInventory.WIDTH * 4))
	val killRewardCycler: KillRewardCycler = inventory.addItem(KillRewardCycler(GuiInventory.WIDTH * 4 + 1))
	val botToggle: BotToggle = inventory.addItem(BotToggle(GuiInventory.WIDTH * 4 + 2))
	val defaultEnvironmentCycler: DefaultEnvironmentCycler = inventory.addItem(DefaultEnvironmentCycler(GuiInventory.WIDTH * 4 + 3))
	val naturalRegenerationToggle: NaturalRegenerationToggle = inventory.addItem(NaturalRegenerationToggle(GuiInventory.WIDTH * 4 + 4))

	private val resetButton: GuiItem = inventory.addItem(object : GuiItem(inventory.inventory.size - 2, true) {
		override fun onClick(player: Player, shift: Boolean) {
			UHC.updatePreset(UHC.defaultPreset)
			presetCycler.updateDisplay()

			UHC.killReward = KillReward.REGENERATION
			killRewardCycler.updateDisplay()

			UHC.defaultVariants.forEach { variant ->
				UHC.updateVariant(variant)
				variantCylers[variant.type.ordinal].updateDisplay()
			}

			QuirkType.values().forEach { quirkType ->
				UHC.updateQuirk(quirkType, false)
				quirkToggles[quirkType.ordinal].updateDisplay()

				val quirk = UHC.getQuirk(quirkType)
				quirk.resetProperties()
				quirk.inventory.guiItems.forEach { guiItem ->
					guiItem?.updateDisplay()
				}
			}

			UHC.updateUsingBot(true)
			botToggle.updateDisplay()

			UHC.defaultWorldIndex = 0
			defaultEnvironmentCycler.updateDisplay()

			UHC.naturalRegeneration = false
			naturalRegenerationToggle.updateDisplay()
		}
		override fun getStack(): ItemStack {
			val stack = ItemStack(Material.MUSIC_DISC_WAIT)
			setName(stack, "${ChatColor.AQUA}Reset")
			return stack
		}
	})

	private val cancelButton: GuiItem = inventory.addItem(object : GuiItem(inventory.inventory.size - 1, false) {
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

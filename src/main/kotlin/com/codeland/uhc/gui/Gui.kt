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

	var inventory = GuiInventory(4, "UHC Setup")

	val quirkToggles: Array<QuirkToggle>
	val variantCylers: Array<VariantCycler>

	val presetCycler: PresetCycler
	val killRewardCycler: KillRewardCycler
	val appleFixToggle: AppleFixToggle
	val mushroomBlockNerfToggle: MushroomBlockNerfToggle
	val botToggle: BotToggle
	val defaultEnvironmentCycler: DefaultEnvironmentCycler
	val resetButton: GuiItem
	val cancelButton: GuiItem

	init {
		quirkToggles = Array(QuirkType.values().size) { i ->
			val item = inventory.addItem(QuirkToggle(uhc, i, QuirkType.values()[i]))

			listener.registerInventory(uhc.getQuirk(QuirkType.values()[i]).inventory)

			item
		}

		variantCylers = Array(PhaseType.values().size) { i ->
			inventory.addItem(VariantCycler(uhc, i + (GuiInventory.WIDTH * 2), PhaseType.values()[i]))
		}

		presetCycler = inventory.addItem(PresetCycler(uhc, GuiInventory.WIDTH * 3))
		killRewardCycler = inventory.addItem(KillRewardCycler(uhc, GuiInventory.WIDTH * 3 + 1))
		appleFixToggle = inventory.addItem(AppleFixToggle(uhc, GuiInventory.WIDTH * 3 + 2))
		mushroomBlockNerfToggle = inventory.addItem(MushroomBlockNerfToggle(uhc, GuiInventory.WIDTH * 3 + 3))
		botToggle = inventory.addItem(BotToggle(uhc, GuiInventory.WIDTH * 3 + 4))
		defaultEnvironmentCycler = inventory.addItem(DefaultEnvironmentCycler(uhc, GuiInventory.WIDTH * 3 + 5))

		resetButton = inventory.addItem(object : GuiItem(uhc, inventory.inventory.size - 2, true) {
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
					uhc.updateQuirk(quirkType, quirkType.defaultEnabled)
					quirkToggles[quirkType.ordinal].updateDisplay()

					val quirk = uhc.getQuirk(quirkType)
					quirk.resetProperties()
					quirk.inventory.guiItems.forEach { guiItem ->
						guiItem?.updateDisplay()
					}
				}

				uhc.updateUsingBot(true)
				botToggle.updateDisplay()

				uhc.appleFix = true
				appleFixToggle.updateDisplay()

				uhc.mushroomBlockNerf = true
				mushroomBlockNerfToggle.updateDisplay()

				uhc.defaultEnvironment = World.Environment.NORMAL
				defaultEnvironmentCycler.updateDisplay()
			}
			override fun getStack(): ItemStack {
				val stack = ItemStack(Material.MUSIC_DISC_WAIT)
				setName(stack, "${ChatColor.AQUA}Reset")
				return stack
			}
		})

		cancelButton = inventory.addItem(object : GuiItem(uhc, inventory.inventory.size - 1, false) {
			override fun onClick(player: Player, shift: Boolean) = inventory.close(player)
			override fun getStack(): ItemStack {
				val stack = ItemStack(Material.BARRIER)
				setName(stack, "${ChatColor.RED}Close")
				return stack
			}
		})

		Bukkit.getPluginManager().registerEvents(listener, UHCPlugin.plugin)
		listener.registerInventory(inventory)
	}
}

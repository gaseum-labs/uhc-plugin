package com.codeland.uhc.gui

import com.codeland.uhc.util.ItemUtil
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

abstract class GuiItem(val index: Int) {
	protected lateinit var gui: GuiPage

	fun giveGui(gui: GuiPage) {
		this.gui = gui
	}

	abstract fun getStack(): ItemStack
	abstract fun onClick(player: Player, shift: Boolean)

	fun updateDisplay() {
		gui.inventory.setItem(index, getStack())
	}

	companion object {
		fun name(stack: ItemStack, component: Component): ItemStack {
			val meta = stack.itemMeta
			meta.displayName(component)
			stack.itemMeta = meta

			return stack
		}

		fun lore(stack: ItemStack, lore: List<Component>): ItemStack {
			val meta = stack.itemMeta
			meta.lore(lore)
			stack.itemMeta = meta

			return stack
		}

		fun enchant(stack: ItemStack): ItemStack {
			val meta = stack.itemMeta
			meta.addEnchant(ItemUtil.FakeEnchantment(), 0, true)
			stack.itemMeta = meta

			return stack
		}

		fun stateName(base: String, state: String): Component {
			return Component.text("$base ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}$state")
		}

		fun enabledName(base: String, enabled: Boolean): Component {
			return Component.text("$base ${ChatColor.GRAY}- ${if (enabled) ChatColor.GREEN else ChatColor.RED}${ChatColor.BOLD}${if (enabled) "Enabled" else "Disabled"}")
		}
	}
}

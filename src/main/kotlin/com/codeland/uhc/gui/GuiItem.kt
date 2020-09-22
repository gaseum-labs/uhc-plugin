package com.codeland.uhc.gui

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.event.Chat
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.NotNull

abstract class GuiItem(val uhc: UHC, val index: Int, val opOnly: Boolean) {
	lateinit var guiInventory: GuiInventory
	lateinit var guiStack: ItemStack

	abstract fun onClick(player: Player, shift: Boolean)
	abstract fun getStack(): ItemStack

	fun updateDisplay() {
		guiInventory.inventory.setItem(index, getStack())
		guiStack = guiInventory.inventory.getItem(index) ?: ItemStack(Material.POTION)
	}

	companion object {
		fun setName(stack: ItemStack, name: String): ItemStack {
			val meta = stack.itemMeta
			meta.setDisplayName("${ChatColor.RESET}$name")
			stack.itemMeta = meta

			return stack
		}

		fun setLore(stack: ItemStack, lore: List<String>): ItemStack {
			val meta = stack.itemMeta
			meta.lore = lore
			stack.itemMeta = meta

			return stack
		}

		class FakeEnchantment : Enchantment(NamespacedKey(UHCPlugin.plugin, "fakeEnchantment")) {
			override fun getName() = ""
			override fun getMaxLevel() = 0
			override fun getStartLevel() = 0
			override fun getItemTarget() = EnchantmentTarget.ARMOR
			override fun isTreasure() = false
			override fun isCursed() = false
			override fun conflictsWith(other: Enchantment) = false
			override fun canEnchantItem(item: ItemStack) = true
		}

		fun setEnchanted(stack: ItemStack): ItemStack {
			val meta = stack.itemMeta
			meta.addEnchant(FakeEnchantment(), 0, true)
			stack.itemMeta = meta

			return stack
		}

		fun stateName(base: String, state: String): String {
			return "$base ${ChatColor.GRAY}- ${ChatColor.GOLD}${ChatColor.BOLD}$state"
		}

		fun enabledName(base: String, enabled: Boolean): String {
			return "$base ${ChatColor.GRAY}- ${if (enabled) ChatColor.GREEN else ChatColor.RED}${ChatColor.BOLD}${if (enabled) "Enabled" else "Disabled"}"
		}
	}
}
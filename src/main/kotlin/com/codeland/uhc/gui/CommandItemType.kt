package com.codeland.uhc.gui

import com.codeland.uhc.UHCPlugin
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

enum class CommandItemType(
	val material: Material,
	val displayName: Component,
	val command: String
) {
	/* lobby */
	GUI_OPENER(
		Material.MUSIC_DISC_WAIT,
		Component.text("${ChatColor.AQUA}Open UHC Settings"),
		"uhc gui"
	),
	PVP_OPENER(
		Material.IRON_SWORD,
		Component.text("${ChatColor.RED}Open PVP Menu"),
		"uhc pvp"
	),
	SPECTATE(
		Material.HEART_OF_THE_SEA,
		Component.text("${ChatColor.BLUE}Spectate Game"),
		"uhc spectate"
	),
	/* parkour */
	LOBBY_RETURN(
		Material.MAGMA_CREAM,
		Component.text("${ChatColor.WHITE}Return to Lobby"),
		"uhc lobby"
	),
	PARKOUR_TEST(
		Material.RABBIT_FOOT,
		Component.text("${ChatColor.LIGHT_PURPLE}Test Parkour"),
		"uhc parkour test"
	),
	PARKOUR_CHECKPOINT(
		Material.GOLD_NUGGET,
		Component.text("${ChatColor.GOLD}Checkpoint"),
		"uhc parkour checkpoint"
	),
	PARKOUR_RESET(
		Material.GOLD_INGOT,
		Component.text("${ChatColor.GOLD}Reset"),
		"uhc parkour reset"
	);

	fun isItem(stack: ItemStack): Boolean {
		if (stack.type !== material) return false

		val index = (stack.itemMeta as PersistentDataHolder).persistentDataContainer.get(key, PersistentDataType.INTEGER)

		return index != null && index == ordinal
	}

	fun giveItem(inventory: Inventory, slot: Int? = null) {
		if (!hasItem(inventory)) {
			if (slot != null) {
				inventory.setItem(slot, createItem())
			} else {
				inventory.addItem(createItem())
			}
		}
	}

	fun execute(player: Player) {
		player.performCommand(command)
	}

	private fun hasItem(inventory: Inventory): Boolean {
		return inventory.contents.any { stack -> stack != null && isItem(stack) }
	}

	private fun createItem(): ItemStack {
		val stack = ItemStack(material)

		val itemMeta = stack.itemMeta

		itemMeta.displayName(displayName)
		itemMeta.lore(listOf(Component.text("Shortcut: ${ChatColor.BOLD}/${command}")))

		(itemMeta as PersistentDataHolder).persistentDataContainer.set(key, PersistentDataType.INTEGER, ordinal)

		stack.itemMeta = itemMeta

		return stack
	}

	companion object {
		private val key = NamespacedKey(UHCPlugin.plugin, "commandItem")
	}
}
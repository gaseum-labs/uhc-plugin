package com.codeland.uhc.gui

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType

enum class CommandItemType(
	val material: Material,
	val displayName: Component,
	val command: String,
) {
	/* lobby */
	GUI_OPENER(
		Material.MUSIC_DISC_WAIT,
		Component.text("${ChatColor.AQUA}Open UHC Settings"),
		"uhc gui"
	),
	PVP_OPENER(
		Material.IRON_SWORD,
		Util.gradientString("Open PVP Menu", TextColor.color(0xe01047), TextColor.color(0xe08910)),
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

		val index =
			(stack.itemMeta as PersistentDataHolder).persistentDataContainer.get(key, PersistentDataType.INTEGER)

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
		return inventory.contents!!.any { stack -> stack != null && isItem(stack) }
	}

	private fun createItem(): ItemStack {
		return ItemCreator.fromType(material)
			.name(displayName)
			.lore("Shortcut: ${ChatColor.BOLD}/${command}")
			.setData(key, ordinal)
			.create()
	}

	companion object {
		private val key = NamespacedKey(UHCPlugin.plugin, "commandItem")
	}
}
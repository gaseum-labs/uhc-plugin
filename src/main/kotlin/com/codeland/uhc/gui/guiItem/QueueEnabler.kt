package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.lobbyPvp.PvpGameManager
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QueueEnabler(index: Int, queueProperty: UHCProperty<Boolean>) : GuiItemProperty<Boolean>(index, queueProperty) {
	override fun onClick(player: Player, shift: Boolean) {
		/* don't let players leave during the countdown */
		if (PvpGameManager.playersGame(player.uniqueId) != null) return

		if (property.get()) {
			player.sendMessage("${ChatColor.GOLD}Left PVP Queue")
			property.set(false)

		} else {
			player.sendMessage("${ChatColor.RED}Entered PVP Queue")
			property.set(true)
		}
	}

	override fun getStackProperty(value: Boolean): ItemStack {
		val stack = name(ItemStack(Material.IRON_SWORD), if (value) "${ChatColor.GREEN}In Queue" else "${ChatColor.RED}Not in Queue")
		lore(stack, if (value) listOf(Component.text("Click to leave queue")) else listOf(Component.text("Click to join queue")))
		return if (value) enchant(stack) else stack
	}
}

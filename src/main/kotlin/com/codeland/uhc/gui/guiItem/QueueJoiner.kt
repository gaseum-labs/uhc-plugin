package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.lobbyPvp.PvpGameManager
import com.codeland.uhc.lobbyPvp.PvpQueue
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QueueJoiner(index: Int, val type: Int, queueProperty: UHCProperty<Int>) : GuiItemProperty<Int>(index, queueProperty) {
	val name = PvpGameManager.typeName(type)
	val material = if (type == PvpGameManager.TYPE_1V1) Material.IRON_SWORD else Material.IRON_AXE
	val disabledMaterial = if (type == PvpGameManager.TYPE_1V1) Material.STONE_SWORD else Material.STONE_AXE

	init { PvpQueue.enabled.watch(::updateDisplay) }

	override fun onClick(player: Player, shift: Boolean) {
		/* don't let players leave during the countdown */
		if (PvpGameManager.playersGame(player.uniqueId) != null) return
		/* if pvp queue is disabled */
		if (!PvpQueue.enabled.get()) return

		if (property.get() == type) {
			player.sendMessage("${ChatColor.GOLD}Left $name PVP Queue")
			property.set(0)

		} else {
			player.sendMessage("${ChatColor.RED}Entered $name PVP Queue")
			property.set(type)
		}
	}

	override fun getStackProperty(value: Int): ItemStack {
		return if (PvpQueue.enabled.get()) {
			val inQueue = value == type

			val stack = name(ItemStack(material), if (inQueue) "${ChatColor.GREEN}In $name Queue" else "${ChatColor.RED}Not in $name Queue")
			lore(stack, if (inQueue) listOf(Component.text("Click to leave $name queue")) else listOf(Component.text("Click to join $name queue")))
			if (inQueue) enchant(stack) else stack

		} else {
			val stack = name(ItemStack(disabledMaterial), "${ChatColor.GRAY}Queue Disabled")
			lore(stack, listOf(Component.text("Ask an admin to enable queue")))
			stack
		}
	}
}

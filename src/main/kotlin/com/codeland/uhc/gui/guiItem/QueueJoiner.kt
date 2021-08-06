package com.codeland.uhc.gui.guiItem

import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.lobbyPvp.PvpQueue
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class QueueJoiner(index: Int, val type: Int, queueProperty: UHCProperty<Int>) : GuiItemProperty<Int>(index, queueProperty) {
	val name = PvpArena.typeName(type)
	val material = if (type == PvpArena.TYPE_1V1) Material.IRON_SWORD else Material.IRON_AXE
	val disabledMaterial = if (type == PvpArena.TYPE_1V1) Material.STONE_SWORD else Material.STONE_AXE

	init { PvpQueue.enabled.watch(::updateDisplay) }

	override fun onClick(player: Player, shift: Boolean) {
		/* don't let players leave during the countdown */
		if (ArenaManager.playersArena(player.uniqueId) != null) return
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

			ItemCreator.fromType(material)
				.name(
					if (inQueue) "${ChatColor.GREEN}In $name Queue"
					else "${ChatColor.RED}Not in $name Queue"
				).lore(
					if (inQueue) "Click to leave $name queue"
					else "Click to join $name queue"
				).enchant(inQueue)

		} else {
			ItemCreator.fromType(disabledMaterial)
				.name("${ChatColor.GRAY}Queue Disabled")
				.lore("Ask an admin to enable queue")

		}.create()
	}
}

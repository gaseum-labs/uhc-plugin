package org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.UHCProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena

class QueueJoiner(index: Int, val type: Int, queueProperty: UHCProperty<Int>) :
	GuiItemProperty<Int>(index, queueProperty) {
	val name = PvpQueue.queueName(type)
	val material = when (type) {
		PvpQueue.TYPE_1V1 -> IRON_SWORD
		PvpQueue.TYPE_2V2 -> IRON_AXE
		PvpQueue.TYPE_GAP -> GOLDEN_APPLE
		else -> STONE
	}
	val disabledMaterial = when (type) {
		PvpQueue.TYPE_1V1 -> STONE_SWORD
		PvpQueue.TYPE_2V2 -> STONE_AXE
		PvpQueue.TYPE_GAP -> APPLE
		else -> BEDROCK
	}

	init {
		PvpQueue.enabled.watch(::updateDisplay)
	}

	override fun onClick(player: Player, shift: Boolean) {
		/* don't let players leave during the countdown */
		if (ArenaManager.playersArena(player.uniqueId) != null) return
		/* if pvp queue is disabled */
		if (!PvpQueue.enabled.get()) return

		if (type == PvpQueue.TYPE_GAP && GapSlapArena.submittedPlatforms.isEmpty()) {
			Commands.errorMessage(player, "No platforms submitted!")
			return
		}

		if (property.get() == type) {
			Action.sendGameMessage(player, "Left $name PVP Queue")
			property.set(0)

		} else {
			Action.sendGameMessage(player, "Entered $name PVP Queue")
			property.set(type)
		}
	}

	override fun getStackProperty(value: Int): ItemStack {
		return if (PvpQueue.enabled.get()) {
			val inQueue = value == type

			ItemCreator.fromType(material)
				.name(
					if (inQueue) Component.text("In $name Queue", GREEN)
					else Component.text("Not in $name Queue", RED)
				).lore(
					if (inQueue) Component.text("Click to leave $name queue")
					else Component.text("Click to join $name queue")
				).enchant(inQueue)

		} else {
			ItemCreator.fromType(disabledMaterial)
				.name(Component.text("Queue Disabled", GRAY))
				.lore(Component.text("Ask an admin to enable queue"))

		}.create()
	}
}

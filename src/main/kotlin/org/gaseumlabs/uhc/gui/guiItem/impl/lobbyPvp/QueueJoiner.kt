package org.gaseumlabs.uhc.gui.guiItem.impl.lobbyPvp

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Material.*
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.gui.guiItem.GuiItemProperty
import org.gaseumlabs.uhc.lobbyPvp.ArenaManager
import org.gaseumlabs.uhc.lobbyPvp.PvpQueue
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.UHCProperty
import kotlin.reflect.KMutableProperty0

class QueueJoiner(index: Int, val type: Int, val playerData: PlayerData) : GuiItemProperty<Int>(index) {
	val name = PvpQueue.queueName(type)
	private val material = when (type) {
		PvpQueue.TYPE_1V1 -> IRON_SWORD
		PvpQueue.TYPE_2V2 -> IRON_AXE
		PvpQueue.TYPE_GAP -> GOLDEN_APPLE
		else -> STONE
	}
	private val disabledMaterial = when (type) {
		PvpQueue.TYPE_1V1 -> STONE_SWORD
		PvpQueue.TYPE_2V2 -> STONE_AXE
		PvpQueue.TYPE_GAP -> APPLE
		else -> BEDROCK
	}

	override fun property() = playerData::inLobbyPvpQueue

	override fun onClickProperty(player: Player, shift: Boolean, property: UHCProperty<Int>) {
		/* don't let players leave during the countdown */
		if (ArenaManager.playersArena(player.uniqueId) != null) return
		/* if pvp queue is disabled */
		if (!PvpQueue.enabled) return

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

	override fun renderProperty(value: Int) =
		if (PvpQueue.enabled) {
			val inQueue = value == type

			ItemCreator.display(material)
				.name(
					if (inQueue) Component.text("In $name Queue", GREEN)
					else Component.text("Not in $name Queue", RED)
				).lore(
					if (inQueue) Component.text("Click to leave $name queue")
					else Component.text("Click to join $name queue")
				).enchant(inQueue)

		} else {
			ItemCreator.display(disabledMaterial)
				.name(Component.text("Queue Disabled", GRAY))
				.lore(Component.text("Ask an admin to enable queue"))

		}
}

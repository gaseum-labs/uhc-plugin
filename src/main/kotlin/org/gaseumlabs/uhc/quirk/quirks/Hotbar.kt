package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.util.Action
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration.BOLD
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

class Hotbar(type: QuirkType, game: Game) : Quirk(type, game) {
	override fun customDestroy() {}

	override fun onStartPlayer(uuid: UUID) {
		val creator = ItemCreator.fromType(Material.BLACK_STAINED_GLASS_PANE)
			.name(Component.text("Unusable Slot", NamedTextColor.DARK_PURPLE, BOLD))

		Action.playerAction(uuid) { player ->
			for (slot in 9 until 36)
				player.inventory.setItem(slot, creator.create())
		}
	}

	override fun onEndPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player ->
			for (slot in 9 until 36)
				player.inventory.setItem(slot, null)
		}
	}

	fun filterDrops(drops: MutableList<ItemStack>) {
		drops.removeAll { itemStack ->
			itemStack.type == Material.BLACK_STAINED_GLASS_PANE && itemStack.itemMeta.hasDisplayName()
		}
	}
}

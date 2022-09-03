package org.gaseumlabs.uhc.chc.chcs

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.FireworkMeta
import org.gaseumlabs.uhc.chc.NoDataCHC
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.Action
import java.util.*

class FloatingIslands : NoDataCHC() {
	private val numRockets = 2

	override fun onStartPlayer(game: Game, uuid: UUID) {
		Action.playerAction(uuid) { player -> giveItems(player, numRockets) }
	}

	override fun onEndPlayer(game: Game, uuid: UUID) {
		Action.playerAction(uuid) { player -> revokeItems(player) }
	}

	companion object {
		fun giveItems(player: Player, numRockets: Int) {
			player.inventory.addItem(
				ItemCreator.display(Material.ELYTRA)
					.lore(Component.text("Given from Floating Islands CHC"))
					.name(Component.text("UHC Elytra", GOLD))
					.create()
			)

			player.inventory.addItem(
				ItemCreator.display(Material.FIREWORK_ROCKET)
					.customMeta<FireworkMeta> { it.power = 2 }
					.lore(Component.text("Given from Floating Islands CHC"))
					.name(Component.text("UHC Rocket", GOLD))
					.amount(numRockets)
					.create()
			)
		}

		fun revokeItems(player: Player) {
			player.inventory.contents.forEach { stack ->
				if (stack != null && (stack.type == Material.ELYTRA || stack.type == Material.FIREWORK_ROCKET) && stack.itemMeta.hasLore()) {
					stack.amount = 0
				}
			}
		}
	}
}
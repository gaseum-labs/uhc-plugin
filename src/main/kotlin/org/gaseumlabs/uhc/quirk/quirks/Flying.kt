package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.util.Action
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.FireworkMeta
import java.util.*

class Flying(type: QuirkType, game: Game) : Quirk(type, game) {
	private val numRockets = 2

	override fun customDestroy() {}

	override fun onStartPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player -> giveItems(player, numRockets) }
	}

	override fun onEndPlayer(uuid: UUID) {
		Action.playerAction(uuid) { player -> revokeItems(player) }
	}

	companion object {
		fun giveItems(player: Player, numRockets: Int) {
			player.inventory.addItem(
				ItemCreator.fromType(Material.ELYTRA)
					.lore(Component.text("Given from Flying Quirk"))
					.name(Component.text("UHC Elytra", GOLD))
					.create()
			)

			player.inventory.addItem(
				ItemCreator.fromType(Material.FIREWORK_ROCKET)
					.customMeta<FireworkMeta> { it.power = 2 }
					.lore(Component.text("Given from Flying Quirk"))
					.name(Component.text("UHC Rocket", GOLD))
					.amount(numRockets)
					.create()
			)
		}

		fun revokeItems(player: Player) {
			player.inventory.contents!!.forEach { stack ->
				if (stack != null && (stack.type == Material.ELYTRA || stack.type == Material.FIREWORK_ROCKET) && stack.itemMeta.hasLore()) {
					stack.amount = 0
				}
			}
		}
	}
}
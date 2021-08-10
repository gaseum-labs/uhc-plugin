package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.Game
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.util.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import java.util.*

class Flying(type: QuirkType, game: Game) : Quirk(type, game) {
	private val numRockets = 2

	override fun customDestroy() {}

	override fun onStartPlayer(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> giveItems(player, numRockets) }
	}

	override fun onEndPlayer(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> revokeItems(player) }
	}

	companion object {
		fun giveItems(player: Player, numRockets: Int) {
			player.inventory.addItem(
				ItemCreator.fromType(Material.ELYTRA)
					.lore("Given from Flying Quirk")
					.name("${ChatColor.GOLD}UHC Elytra")
					.create()
			)

			player.inventory.addItem(
				ItemCreator.fromType(Material.FIREWORK_ROCKET)
				.customMeta { meta -> (meta as FireworkMeta).power = 2 }
				.lore("Given from Flying Quirk")
				.name("${ChatColor.GOLD}UHC Rocket")
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
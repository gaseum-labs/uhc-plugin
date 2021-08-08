package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import java.util.*

class Flying(type: QuirkType) : Quirk(type) {
	val numRockets = addProperty(UHCProperty(DEFAULT_ROCKETS))

	override fun onEnable() {}

	override fun customDestroy() {}

	override fun onStartPlayer(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> giveItems(player, numRockets.get()) }
	}

	override fun onEndPlayer(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> revokeItems(player) }
	}

	override val representation = ItemCreator.fromType(Material.FIREWORK_ROCKET)

	init {
		val rocketItem = object : GuiItemProperty <Int> (13, numRockets) {
			override fun onClick(player: Player, shift: Boolean) {
				if (shift) numRockets.set((numRockets.get() - 1).coerceAtLeast(0))
				else numRockets.set((numRockets.get() + 1).coerceAtMost(MAX_ROCKETS))
			}

			override fun getStackProperty(value: Int): ItemStack {
				return ItemCreator.fromType(if (value == 0) Material.GUNPOWDER else Material.FIREWORK_ROCKET)
					.lore("click to add", "shift click to subtract")
					.name(ItemCreator.stateName("Num rockets", "$value"))
					.amount(value.coerceAtLeast(1))
					.create()
			}
		}

		gui.addItem(rocketItem)
	}

	companion object {
		const val MIN_ROCKETS = 0
		const val DEFAULT_ROCKETS = 2
		const val MAX_ROCKETS = 64

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
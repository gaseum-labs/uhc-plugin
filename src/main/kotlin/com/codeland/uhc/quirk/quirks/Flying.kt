package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHCProperty
import com.codeland.uhc.gui.GuiItemProperty
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

	override fun onDisable() {}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> giveItems(player, numRockets.get()) }
	}

	override fun onEnd(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> revokeItems(player) }
	}

	override val representation: ItemStack
		get() = ItemStack(Material.FIREWORK_ROCKET)

	init {
		val rocketItem = object : GuiItemProperty <Int> (13, numRockets) {
			override fun onClick(player: Player, shift: Boolean) {
				if (shift)
					numRockets.set((numRockets.get() - 1).coerceAtLeast(0))
				else
					numRockets.set((numRockets.get() + 1).coerceAtMost(MAX_ROCKETS))
			}

			override fun getStackProperty(value: Int): ItemStack {
				return lore(
					name(
						ItemStack(if (value == 0) Material.GUNPOWDER else Material.FIREWORK_ROCKET, value.coerceAtLeast(1)),
						stateName("Num rockets", "$value")
					),
					listOf(Component.text("click to add"), Component.text("shift click to subtract"))
				)
			}
		}

		gui.addItem(rocketItem)
	}

	companion object {
		const val MIN_ROCKETS = 0
		const val DEFAULT_ROCKETS = 2
		const val MAX_ROCKETS = 64

		fun giveItems(player: Player, numRockets: Int) {
			val elytra = ItemStack(Material.ELYTRA)
			val meta = elytra.itemMeta

			meta.setDisplayName("${ChatColor.GOLD}UHC Elytra")
			meta.lore = listOf("Given from Flying Quirk")

			elytra.itemMeta = meta
			player.inventory.addItem(elytra)

			val rockets = ItemStack(Material.FIREWORK_ROCKET, numRockets)
			val rocketMeta = rockets.itemMeta as FireworkMeta

			rocketMeta.setDisplayName("${ChatColor.GOLD}UHC Rocket")
			rocketMeta.lore = listOf("Given from Flying Quirk")
			rocketMeta.power = 2

			rockets.itemMeta = rocketMeta

			player.inventory.addItem(ItemStack(rockets))
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
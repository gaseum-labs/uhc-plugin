package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.ItemUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import java.util.*

class Flying(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	var numRockets: Int = DEFAULT_ROCKETS

	override fun onEnable() {
		if (uhc.isGameGoing()) {
			GameRunner.uhc.allCurrentPlayers { uuid ->
				GameRunner.playerAction(uuid) { player -> giveItems(player, numRockets) }
			}
		}
	}

	override fun onDisable() {
		GameRunner.uhc.allCurrentPlayers { uuid ->
			GameRunner.playerAction(uuid) { player -> revokeItems(player) }
		}
	}

	override fun onStart(uuid: UUID) {
		GameRunner.playerAction(uuid) { player -> giveItems(player, numRockets) }
	}

	init {
		val rocketItem = object : GuiItem(uhc, 13, true) {
			override fun onClick(player: Player, shift: Boolean) {
				if (shift) {
					--numRockets
					if (numRockets < MIN_ROCKETS) numRockets = MIN_ROCKETS
				} else {
					++numRockets
					if (numRockets > MAX_ROCKETS) numRockets = MAX_ROCKETS
				}
			}

			override fun getStack(): ItemStack {
				return setLore(setName(ItemStack(if (numRockets == 0) Material.GUNPOWDER else Material.FIREWORK_ROCKET, numRockets.coerceAtLeast(1)), stateName("Num rockets", "$numRockets")), listOf("click to add", "shift click to subtract"))
			}
		}

		inventory.addItem(rocketItem)
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
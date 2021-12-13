package com.codeland.uhc.event

import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Material.POTION
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.SmithItemEvent
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.Damageable
import java.util.*

class Axe : Listener {
	companion object {
		private fun stats(stack: ItemStack, damage: Double, speed: Double): ItemStack {
			val meta = stack.itemMeta
			meta.addAttributeModifier(
				Attribute.GENERIC_ATTACK_DAMAGE,
				AttributeModifier(
					UUID.randomUUID(),
					"Damage",
					damage - 1.0,
					AttributeModifier.Operation.ADD_NUMBER,
					EquipmentSlot.HAND
				)
			)
			meta.addAttributeModifier(
				Attribute.GENERIC_ATTACK_SPEED,
				AttributeModifier(
					UUID.randomUUID(),
					"AttackSpeed",
					speed - 4.0,
					AttributeModifier.Operation.ADD_NUMBER,
					EquipmentSlot.HAND
				)
			)
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
			meta.lore(
				listOf(
					Component.text("${ChatColor.RED}Damage: ${ChatColor.BOLD}$damage"),
					Component.text("${ChatColor.AQUA}Speed: ${ChatColor.BOLD}$speed")
				)
			)
			stack.itemMeta = meta

			return stack
		}

		fun netheriteAxe(): ItemStack {
			return stats(ItemStack(Material.NETHERITE_AXE), 9.0, 1.0)
		}

		fun diamondAxe(): ItemStack {
			return stats(ItemStack(Material.DIAMOND_AXE), 8.5, 1.0)
		}

		fun ironAxe(): ItemStack {
			return stats(ItemStack(Material.IRON_AXE), 8.0, 0.9)
		}

		fun stoneAxe(): ItemStack {
			return stats(ItemStack(Material.STONE_AXE), 8.0, 0.8)
		}
	}

	@EventHandler
	fun onCraft(event: CraftItemEvent) {
		val type = event.currentItem?.type ?: return

		event.currentItem = when (type) {
			Material.STONE_AXE -> stoneAxe()
			Material.IRON_AXE -> ironAxe()
			Material.DIAMOND_AXE -> diamondAxe()
			else -> event.currentItem
		}
	}

	@EventHandler
	fun onSmith(event: SmithItemEvent) {
		val type = event.currentItem?.type ?: return

		event.currentItem = when (type) {
			Material.NETHERITE_AXE -> netheriteAxe()
			else -> event.currentItem
		}
	}
}
package com.codeland.uhc.core

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

object AxeFix {
	private fun stats(stack: ItemStack, damage: Double, speed: Double): ItemStack {
		val meta = stack.itemMeta
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier(UUID.randomUUID(), "Damage", damage - 1.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND))
		meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier(UUID.randomUUID(), "AttackSpeed", speed - 4.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND))
		stack.itemMeta = meta

		return stack
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
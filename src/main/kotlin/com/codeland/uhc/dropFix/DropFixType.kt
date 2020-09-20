package com.codeland.uhc.dropFix

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.*
import org.bukkit.inventory.ItemStack

enum class DropFixType(val dropFix: DropFix) {
	BLAZE(DropFix(EntityType.BLAZE, arrayOf(
		arrayOf(ItemStack(Material.BLAZE_ROD))
	), arrayOf(
		ItemStack(Material.BLAZE_POWDER)
	))),
	SPIDER(DropFix(EntityType.SPIDER, arrayOf(
		arrayOf(ItemStack(Material.STRING)),
		arrayOf(ItemStack(Material.STRING)),
		arrayOf(ItemStack(Material.STRING), ItemStack(Material.SPIDER_EYE))
	), arrayOf(
		ItemStack(Material.AIR)
	))),
	SKELETON(DropFix(EntityType.SKELETON, arrayOf(
		arrayOf(ItemStack(Material.BONE), ItemStack(Material.ARROW))
	), arrayOf(
		ItemStack(Material.BONE)
	))),
	COW(DropFix(EntityType.COW, arrayOf(
		arrayOf(ItemStack(Material.LEATHER), ItemStack(Material.BEEF, 1)),
		arrayOf(ItemStack(Material.LEATHER), ItemStack(Material.BEEF, 2)),
		arrayOf(ItemStack(Material.LEATHER), ItemStack(Material.BEEF, 3))
	), arrayOf(
		ItemStack(Material.BEEF, 1)
	))),
	CHICKEN(DropFix(EntityType.CHICKEN, arrayOf(
		arrayOf(ItemStack(Material.CHICKEN)),
		arrayOf(ItemStack(Material.CHICKEN)),
		arrayOf(ItemStack(Material.CHICKEN)),
		arrayOf(ItemStack(Material.CHICKEN)),
		arrayOf(ItemStack(Material.FEATHER, 1), ItemStack(Material.CHICKEN)),
		arrayOf(ItemStack(Material.FEATHER, 1), ItemStack(Material.CHICKEN)),
		arrayOf(ItemStack(Material.FEATHER, 1), ItemStack(Material.CHICKEN)),
		arrayOf(ItemStack(Material.FEATHER, 1), ItemStack(Material.CHICKEN))
	), arrayOf(
		ItemStack(Material.CHICKEN)
	))),
	ENDERMAN(DropFix(EntityType.ENDERMAN, arrayOf(
		arrayOf(ItemStack(Material.AIR)),
		arrayOf(ItemStack(Material.AIR)),
		arrayOf(ItemStack(Material.AIR)),
		arrayOf(ItemStack(Material.AIR)),
		arrayOf(ItemStack(Material.ENDER_PEARL)),
		arrayOf(ItemStack(Material.ENDER_PEARL)),
		arrayOf(ItemStack(Material.ENDER_PEARL)),
		arrayOf(ItemStack(Material.ENDER_PEARL))
	), arrayOf(
		ItemStack(Material.AIR)
	))),
	GHAST(DropFix(EntityType.GHAST, arrayOf(
		arrayOf(ItemStack(Material.GUNPOWDER), ItemStack(Material.GHAST_TEAR))
	), arrayOf(
		ItemStack(Material.GUNPOWDER)
	)))
}

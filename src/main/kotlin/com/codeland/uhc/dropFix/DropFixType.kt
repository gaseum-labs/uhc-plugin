package com.codeland.uhc.dropFix

import com.codeland.uhc.dropFix.DropEntry.Companion.loot
import com.codeland.uhc.dropFix.DropEntry.Companion.lootItem
import com.codeland.uhc.dropFix.DropEntry.Companion.lootMulti
import com.codeland.uhc.dropFix.DropEntry.Companion.onFire
import com.codeland.uhc.dropFix.DropEntry.Companion.saddle
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.EntityType

enum class DropFixType(val dropFix: DropFix) {
	BLAZE(DropFix(EntityType.BLAZE, arrayOf(
		arrayOf(loot(BLAZE_ROD, ::lootItem))
	), arrayOf(
		DropEntry.item(BLAZE_POWDER)
	))),

	SPIDER(DropFix(EntityType.SPIDER, arrayOf(
		arrayOf(loot(STRING, ::lootItem), loot(SPIDER_EYE, lootMulti(-1))),
		arrayOf(loot(STRING, ::lootItem), loot(SPIDER_EYE, lootMulti( 0))),
		arrayOf(loot(STRING, ::lootItem), loot(SPIDER_EYE, lootMulti( 1)))
	), arrayOf(
		DropEntry.nothing()
	))),

	SKELETON(DropFix(EntityType.SKELETON, arrayOf(
		arrayOf(loot(BONE, ::lootItem), loot(ARROW, ::lootItem)),
		arrayOf(loot(BONE, ::lootItem), loot(ARROW, ::lootItem)),
		arrayOf(loot(BONE, ::lootItem), loot(ARROW, ::lootItem))
	), arrayOf(
		DropEntry.item(BONE)
	))),

	COW(DropFix(EntityType.COW, arrayOf(
		arrayOf(loot(LEATHER, ::lootItem), DropEntry.lootEntity(onFire(BEEF, COOKED_BEEF), lootMulti(1))),
		arrayOf(loot(LEATHER, ::lootItem), DropEntry.lootEntity(onFire(BEEF, COOKED_BEEF), lootMulti(2))),
		arrayOf(loot(LEATHER, ::lootItem), DropEntry.lootEntity(onFire(BEEF, COOKED_BEEF), lootMulti(3)))
	), arrayOf(
		DropEntry.item(BEEF)
	))),

	HORSE(DropFix(EntityType.HORSE, arrayOf(
		arrayOf(DropEntry.entity(::saddle), loot(LEATHER, ::lootItem))
	), arrayOf(
		DropEntry.nothing()
	))),

	LLAMA(DropFix(EntityType.LLAMA, arrayOf(
		arrayOf(loot(LEATHER, ::lootItem))
	), arrayOf(
		DropEntry.nothing()
	))),

	CHICKEN(DropFix(EntityType.CHICKEN, arrayOf(
		arrayOf(DropEntry.lootEntity(onFire(Material.CHICKEN, COOKED_CHICKEN), ::lootItem), loot(FEATHER, lootMulti(-2))),
		arrayOf(DropEntry.lootEntity(onFire(Material.CHICKEN, COOKED_CHICKEN), ::lootItem), loot(FEATHER, lootMulti(-1))),
		arrayOf(DropEntry.lootEntity(onFire(Material.CHICKEN, COOKED_CHICKEN), ::lootItem), loot(FEATHER, lootMulti( 0))),
		arrayOf(DropEntry.lootEntity(onFire(Material.CHICKEN, COOKED_CHICKEN), ::lootItem), loot(FEATHER, ::lootItem)),
		arrayOf(DropEntry.lootEntity(onFire(Material.CHICKEN, COOKED_CHICKEN), ::lootItem), loot(FEATHER, ::lootItem)),
		arrayOf(DropEntry.lootEntity(onFire(Material.CHICKEN, COOKED_CHICKEN), ::lootItem), loot(FEATHER, ::lootItem))
	), arrayOf(
		DropEntry.item(Material.CHICKEN)
	))),

	ENDERMAN(DropFix(EntityType.ENDERMAN, arrayOf(
		arrayOf(loot(ENDER_PEARL, lootMulti(-2))),
		arrayOf(loot(ENDER_PEARL, lootMulti(-1))),
		arrayOf(loot(ENDER_PEARL, lootMulti(0))),
		arrayOf(loot(ENDER_PEARL, lootMulti(1))),
		arrayOf(loot(ENDER_PEARL, lootMulti(1))),
		arrayOf(loot(ENDER_PEARL, lootMulti(1)))
	), arrayOf(
		DropEntry.nothing()
	))),

	GHAST(DropFix(EntityType.GHAST, arrayOf(
		arrayOf(loot(GUNPOWDER, ::lootItem), loot(GHAST_TEAR, ::lootItem))
	), arrayOf(
		DropEntry.item(GUNPOWDER)
	))),

	STRIDER(DropFix(EntityType.STRIDER, arrayOf(
		arrayOf(DropEntry.entity(::saddle), loot(STRING, lootMulti(1))),
		arrayOf(DropEntry.entity(::saddle), loot(STRING, lootMulti(2))),
		arrayOf(DropEntry.entity(::saddle), loot(STRING, lootMulti(2))),
		arrayOf(DropEntry.entity(::saddle), loot(STRING, lootMulti(3))),
		arrayOf(DropEntry.entity(::saddle), loot(STRING, lootMulti(3))),
		arrayOf(DropEntry.entity(::saddle), loot(STRING, lootMulti(4)))
	), arrayOf(
		DropEntry.item(STRING)
	)));
}

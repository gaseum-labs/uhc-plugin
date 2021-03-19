package com.codeland.uhc.dropFix

import com.codeland.uhc.dropFix.DropEntry.Companion.endermanHolding
import com.codeland.uhc.dropFix.DropEntry.Companion.entity
import com.codeland.uhc.dropFix.DropEntry.Companion.hasTrident
import com.codeland.uhc.dropFix.DropEntry.Companion.isSize
import com.codeland.uhc.dropFix.DropEntry.Companion.item
import com.codeland.uhc.dropFix.DropEntry.Companion.loot
import com.codeland.uhc.dropFix.DropEntry.Companion.lootEntity
import com.codeland.uhc.dropFix.DropEntry.Companion.lootItem
import com.codeland.uhc.dropFix.DropEntry.Companion.lootMulti
import com.codeland.uhc.dropFix.DropEntry.Companion.mobArmor
import com.codeland.uhc.dropFix.DropEntry.Companion.mobInventory
import com.codeland.uhc.dropFix.DropEntry.Companion.noBaby
import com.codeland.uhc.dropFix.DropEntry.Companion.nothing
import com.codeland.uhc.dropFix.DropEntry.Companion.onFire
import com.codeland.uhc.dropFix.DropEntry.Companion.potion
import com.codeland.uhc.dropFix.DropEntry.Companion.saddle
import com.codeland.uhc.dropFix.DropEntry.Companion.slownessArrow
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionType

enum class DropFixType(val dropFix: DropFix) {
	BLAZE(DropFix(EntityType.BLAZE, arrayOf(
		arrayOf(loot(BLAZE_ROD, ::lootItem))
	), arrayOf(
		item(BLAZE_POWDER)
	))),

	SPIDER(DropFix(EntityType.SPIDER, arrayOf(
		arrayOf(loot(STRING, ::lootItem), loot(SPIDER_EYE, lootMulti(-1))),
		arrayOf(loot(STRING, ::lootItem), loot(SPIDER_EYE, lootMulti( 0))),
		arrayOf(loot(STRING, ::lootItem), loot(SPIDER_EYE, lootMulti( 1)))
	), arrayOf(
		nothing()
	))),

	SKELETON(DropFix(EntityType.SKELETON, arrayOf(
		arrayOf(loot(BONE, ::lootItem), loot(ARROW, ::lootItem), mobArmor())
	), arrayOf(
		item(BONE)
	))),

	COW(DropFix(EntityType.COW, arrayOf(
		arrayOf(lootEntity(noBaby { LEATHER }, ::lootItem), lootEntity(noBaby(onFire(BEEF, COOKED_BEEF)), lootMulti(1))),
		arrayOf(lootEntity(noBaby { LEATHER }, ::lootItem), lootEntity(noBaby(onFire(BEEF, COOKED_BEEF)), lootMulti(2))),
		arrayOf(lootEntity(noBaby { LEATHER }, ::lootItem), lootEntity(noBaby(onFire(BEEF, COOKED_BEEF)), lootMulti(3)))
	), arrayOf(
		item(BEEF)
	))),

	HORSE(DropFix(EntityType.HORSE, arrayOf(
		arrayOf(DropEntry.horseInventory(), lootEntity(noBaby { LEATHER }, ::lootItem))
	), arrayOf(
		nothing()
	))),

	LLAMA(DropFix(EntityType.LLAMA, arrayOf(
		arrayOf(lootEntity(noBaby { LEATHER }, ::lootItem))
	), arrayOf(
		nothing()
	))),

	MULE(DropFix(EntityType.MULE, arrayOf(
		arrayOf(DropEntry.horseInventory(), lootEntity(noBaby { LEATHER }, ::lootItem))
	), arrayOf(
		nothing()
	))),

	DONKEY(DropFix(EntityType.DONKEY, arrayOf(
		arrayOf(DropEntry.horseInventory(), lootEntity(noBaby { LEATHER }, ::lootItem))
	), arrayOf(
		nothing()
	))),

	CHICKEN(DropFix(EntityType.CHICKEN, arrayOf(
		arrayOf(lootEntity(noBaby(onFire(Material.CHICKEN, COOKED_CHICKEN)), ::lootItem), lootEntity(noBaby { FEATHER }, lootMulti(0))),
		arrayOf(lootEntity(noBaby(onFire(Material.CHICKEN, COOKED_CHICKEN)), ::lootItem), lootEntity(noBaby { FEATHER }, lootMulti(1))),
	), arrayOf(
		item(Material.CHICKEN)
	))),

	ENDERMAN(DropFix(EntityType.ENDERMAN, arrayOf(
		arrayOf(loot(ENDER_PEARL, ::lootItem), endermanHolding())
	), arrayOf(
		nothing()
	))),

	MAGMA_CUBE(DropFix(EntityType.MAGMA_CUBE, arrayOf(
		arrayOf(nothing()),
		arrayOf(lootEntity(isSize(MAGMA_CREAM, 1), ::lootItem))
	), arrayOf(
		nothing()
	))),

	GHAST(DropFix(EntityType.GHAST, arrayOf(
		arrayOf(loot(GUNPOWDER, ::lootItem), loot(GHAST_TEAR, ::lootItem))
	), arrayOf(
		item(GUNPOWDER)
	))),

	STRIDER(DropFix(EntityType.STRIDER, arrayOf(
		arrayOf(entity(::saddle), loot(STRING, lootMulti(3))),
	), arrayOf(
		item(STRING)
	))),

	CREEPER(DropFix(EntityType.CREEPER, arrayOf(
		arrayOf(loot(GUNPOWDER, ::lootItem))
	), arrayOf(
		nothing()
	))),

	DROWNED(DropFix(EntityType.DROWNED, arrayOf(
		arrayOf(mobInventory(), loot(ROTTEN_FLESH, ::lootItem), loot(GOLD_INGOT, lootMulti(-1))),
		arrayOf(mobInventory(), loot(ROTTEN_FLESH, ::lootItem), loot(GOLD_INGOT, lootMulti( 0))),
		arrayOf(mobInventory(), loot(ROTTEN_FLESH, ::lootItem), loot(GOLD_INGOT, lootMulti( 1)))
	), arrayOf(
		item(ROTTEN_FLESH)
	))),

	WITCH(DropFix(EntityType.WITCH, arrayOf(
		arrayOf(item(GLASS_BOTTLE), item(GLOWSTONE_DUST), item(GUNPOWDER), item(REDSTONE), item(SPIDER_EYE), item(SUGAR), item(STICK), potion(PotionType.INSTANT_HEAL)),
		arrayOf(item(GLASS_BOTTLE), item(GLOWSTONE_DUST), item(GUNPOWDER), item(REDSTONE), item(SPIDER_EYE), item(SUGAR), item(STICK), potion(PotionType.FIRE_RESISTANCE)),
		arrayOf(item(GLASS_BOTTLE), item(GLOWSTONE_DUST), item(GUNPOWDER), item(REDSTONE), item(SPIDER_EYE), item(SUGAR), item(STICK), potion(PotionType.SPEED))
	), arrayOf(
		item(STICK)
	))),

	ZOMBIE(DropFix(EntityType.ZOMBIE, arrayOf(
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory(), item(IRON_INGOT)),
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory(), item(CARROT)),
	), arrayOf(
		item(ROTTEN_FLESH)
	))),

	STRAY(DropFix(EntityType.STRAY, arrayOf(
		arrayOf(loot(BONE, ::lootItem), slownessArrow(), mobArmor())
	), arrayOf(
		item(BONE)
	))),

	HUSK(DropFix(EntityType.HUSK, arrayOf(
		arrayOf(loot(ROTTEN_FLESH, ::lootItem), mobInventory())
	), arrayOf(
		item(ROTTEN_FLESH)
	))),

	PIGLIN(DropFix(EntityType.PIGLIN, arrayOf(
		arrayOf(mobArmor())
	), arrayOf(
		nothing()
	)));
}

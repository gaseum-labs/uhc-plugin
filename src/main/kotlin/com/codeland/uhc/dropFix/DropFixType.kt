package com.codeland.uhc.dropFix

import com.codeland.uhc.dropFix.DropEntry.Companion.endermanHolding
import com.codeland.uhc.dropFix.DropEntry.Companion.isSize
import com.codeland.uhc.dropFix.DropEntry.Companion.item
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
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionType

enum class DropFixType(val dropFix: DropFix) {
	BLAZE(DropFix(EntityType.BLAZE, arrayOf(
		arrayOf(item(BLAZE_ROD, ::lootItem))
	), arrayOf(
		item(BLAZE_POWDER)
	))),

	SPIDER(DropFix(EntityType.SPIDER, arrayOf(
		arrayOf(item(STRING, ::lootItem), item(SPIDER_EYE, lootMulti(-1))),
		arrayOf(item(STRING, ::lootItem), item(SPIDER_EYE, lootMulti( 0))),
		arrayOf(item(STRING, ::lootItem), item(SPIDER_EYE, lootMulti( 1)))
	))),

	SKELETON(DropFix(EntityType.SKELETON, arrayOf(
		arrayOf(item(BONE, ::lootItem), item(ARROW, ::lootItem), mobArmor())
	), arrayOf(
		item(BONE)
	))),

	COW(DropFix(EntityType.COW, arrayOf(
		arrayOf(item(noBaby { LEATHER }, ::lootItem), item(noBaby(onFire(BEEF, COOKED_BEEF)), lootMulti(2)))
	), arrayOf(
		item(BEEF)
	))),

	HORSE(DropFix(EntityType.HORSE, arrayOf(
		arrayOf(DropEntry.horseInventory(), item(noBaby { LEATHER }, ::lootItem))
	))),

	LLAMA(DropFix(EntityType.LLAMA, arrayOf(
		arrayOf(item(noBaby { LEATHER }, ::lootItem))
	))),

	MULE(DropFix(EntityType.MULE, arrayOf(
		arrayOf(DropEntry.horseInventory(), item(noBaby { LEATHER }, ::lootItem))
	))),

	DONKEY(DropFix(EntityType.DONKEY, arrayOf(
		arrayOf(DropEntry.horseInventory(), item(noBaby { LEATHER }, ::lootItem))
	))),

	CHICKEN(DropFix(EntityType.CHICKEN, arrayOf(
		arrayOf(item(noBaby(onFire(Material.CHICKEN, COOKED_CHICKEN)), ::lootItem), item(noBaby { FEATHER }, lootMulti(0))),
		arrayOf(item(noBaby(onFire(Material.CHICKEN, COOKED_CHICKEN)), ::lootItem), item(noBaby { FEATHER }, lootMulti(1))),
	), arrayOf(
		item(Material.CHICKEN)
	))),

	ENDERMAN(DropFix(EntityType.ENDERMAN, arrayOf(
		arrayOf(item(ENDER_PEARL, ::lootItem), endermanHolding())
	))),

	MAGMA_CUBE(DropFix(EntityType.MAGMA_CUBE, arrayOf(
		arrayOf(nothing()),
		arrayOf(item(isSize(MAGMA_CREAM, 1), ::lootItem))
	))),

	GHAST(DropFix(EntityType.GHAST, arrayOf(
		arrayOf(item(GUNPOWDER, ::lootItem), item(GHAST_TEAR, ::lootItem))
	), arrayOf(
		item(GUNPOWDER)
	))),

	STRIDER(DropFix(EntityType.STRIDER, arrayOf(
		arrayOf(item(noBaby { saddle(it) }), item(STRING, lootMulti(3))),
	), arrayOf(
		item(STRING)
	))),

	CREEPER(DropFix(EntityType.CREEPER, arrayOf(
		arrayOf(item(GUNPOWDER, ::lootItem))
	))),

	DROWNED(DropFix(EntityType.DROWNED, arrayOf(
		arrayOf(mobInventory(), item(ROTTEN_FLESH, ::lootItem), item(GOLD_INGOT, lootMulti(-1))),
		arrayOf(mobInventory(), item(ROTTEN_FLESH, ::lootItem), item(GOLD_INGOT, lootMulti( 0))),
		arrayOf(mobInventory(), item(ROTTEN_FLESH, ::lootItem), item(GOLD_INGOT, lootMulti( 1)))
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
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory(), item(IRON_INGOT)),
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory(), item(CARROT)),
	), arrayOf(
		item(ROTTEN_FLESH)
	))),

	STRAY(DropFix(EntityType.STRAY, arrayOf(
		arrayOf(item(BONE, ::lootItem), slownessArrow(), mobArmor())
	), arrayOf(
		item(BONE)
	))),

	HUSK(DropFix(EntityType.HUSK, arrayOf(
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory())
	), arrayOf(
		item(ROTTEN_FLESH)
	))),

	PIGLIN(DropFix(EntityType.PIGLIN, arrayOf(
		arrayOf(mobArmor())
	)));

	companion object {
		val list = values()
		init { list.sortBy { dropFixType -> dropFixType.dropFix.entityType } }
	}
}

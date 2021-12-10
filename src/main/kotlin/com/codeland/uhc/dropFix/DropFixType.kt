package com.codeland.uhc.dropFix

import com.codeland.uhc.dropFix.DropEntry.Companion.endermanHolding
import com.codeland.uhc.dropFix.DropEntry.Companion.isSize
import com.codeland.uhc.dropFix.DropEntry.Companion.item
import com.codeland.uhc.dropFix.DropEntry.Companion.lootItem
import com.codeland.uhc.dropFix.DropEntry.Companion.lootMulti
import com.codeland.uhc.dropFix.DropEntry.Companion.mobArmor
import com.codeland.uhc.dropFix.DropEntry.Companion.mobInventory
import com.codeland.uhc.dropFix.DropEntry.Companion.noBaby
import com.codeland.uhc.dropFix.DropEntry.Companion.onFire
import com.codeland.uhc.dropFix.DropEntry.Companion.potion
import com.codeland.uhc.dropFix.DropEntry.Companion.saddle
import com.codeland.uhc.dropFix.DropEntry.Companion.slownessArrow
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.EntityType
import org.bukkit.potion.PotionType

private fun zombieTemplate(entityType: EntityType): DropFix {
	return DropFix(
		entityType,
		arrayOf(
			arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory()),
		),
		arrayOf(
			Pair(8, item(IRON_INGOT)),
			Pair(8, item(CARROT)),
			Pair(8, item(POTATO)),
		)
	)
}

private fun horseTemplate(entityType: EntityType): DropFix {
	return DropFix(
		entityType,
		arrayOf(
			arrayOf(DropEntry.horseInventory(), item(noBaby { LEATHER }, ::lootItem))
		)
	)
}

enum class DropFixType(val dropFix: DropFix) {
	BLAZE(DropFix(EntityType.BLAZE, arrayOf(
		arrayOf(item(BLAZE_ROD, ::lootItem))
	))),

	SPIDER(DropFix(
		EntityType.SPIDER,
		arrayOf(
			arrayOf(item(STRING, ::lootItem)),
		),
		arrayOf(
			Pair(3, item(SPIDER_EYE))
		)
	)),

	SKELETON(DropFix(EntityType.SKELETON, arrayOf(
		arrayOf(item(BONE, ::lootItem), item(ARROW, ::lootItem), mobArmor())
	))),

	COW(DropFix(EntityType.COW, arrayOf(
		arrayOf(item(noBaby { LEATHER }, ::lootItem), item(noBaby(onFire(BEEF, COOKED_BEEF)), lootMulti(2)))
	))),

	HORSE(horseTemplate(EntityType.HORSE)),
	MULE(horseTemplate(EntityType.MULE)),
	DONKEY(horseTemplate(EntityType.DONKEY)),

	LLAMA(DropFix(EntityType.LLAMA, arrayOf(
		arrayOf(item(noBaby { LEATHER }, ::lootItem))
	))),

	CHICKEN(DropFix(
		EntityType.CHICKEN,
		arrayOf(
			arrayOf(item(noBaby(onFire(Material.CHICKEN, COOKED_CHICKEN)), ::lootItem)),
		),
		arrayOf(
			Pair(2, item(noBaby { FEATHER }, ::lootItem))
		)
	)),

	ENDERMAN(DropFix(EntityType.ENDERMAN, arrayOf(
		arrayOf(item(ENDER_PEARL, ::lootItem), endermanHolding())
	))),

	MAGMA_CUBE(DropFix(
		EntityType.MAGMA_CUBE,
		emptyArray(),
		arrayOf(
			Pair(2, item(isSize(MAGMA_CREAM, 1), ::lootItem))
		)
	)),

	GHAST(DropFix(EntityType.GHAST, arrayOf(
		arrayOf(item(GUNPOWDER, ::lootItem), item(GHAST_TEAR, ::lootItem))
	))),

	STRIDER(DropFix(EntityType.STRIDER, arrayOf(
		arrayOf(item(noBaby { saddle(it) }), item(STRING, lootMulti(3))),
	))),

	CREEPER(DropFix(EntityType.CREEPER, arrayOf(
		arrayOf(item(GUNPOWDER, ::lootItem))
	))),

	DROWNED(DropFix(EntityType.DROWNED, arrayOf(
		arrayOf(mobInventory(), item(ROTTEN_FLESH, ::lootItem), item(GOLD_INGOT)),
	))),

	WITCH(DropFix(EntityType.WITCH, arrayOf(
		arrayOf(item(GLASS_BOTTLE),
			item(GLOWSTONE_DUST),
			item(GUNPOWDER),
			item(REDSTONE),
			item(SPIDER_EYE),
			item(SUGAR),
			item(STICK),
			potion(PotionType.INSTANT_HEAL)),
		arrayOf(item(GLASS_BOTTLE),
			item(GLOWSTONE_DUST),
			item(GUNPOWDER),
			item(REDSTONE),
			item(SPIDER_EYE),
			item(SUGAR),
			item(STICK),
			potion(PotionType.FIRE_RESISTANCE)),
		arrayOf(item(GLASS_BOTTLE),
			item(GLOWSTONE_DUST),
			item(GUNPOWDER),
			item(REDSTONE),
			item(SPIDER_EYE),
			item(SUGAR),
			item(STICK),
			potion(PotionType.SPEED))
	))),

	ZOMBIE(zombieTemplate(EntityType.ZOMBIE)),
	ZOMBIE_VILLAGER(zombieTemplate(EntityType.ZOMBIE_VILLAGER)),
	HUSK(zombieTemplate(EntityType.HUSK)),

	STRAY(DropFix(EntityType.STRAY, arrayOf(
		arrayOf(item(BONE, ::lootItem), slownessArrow(), mobArmor())
	))),

	PIGLIN(DropFix(EntityType.PIGLIN, arrayOf(
		arrayOf(mobArmor())
	))),

	RABBIT(DropFix(EntityType.RABBIT, arrayOf(
		arrayOf(item(RABBIT_HIDE, lootMulti(2)), item(onFire(Material.RABBIT, COOKED_RABBIT), ::lootItem)),
	), arrayOf(
		Pair(3, item(RABBIT_FOOT, ::lootItem))
	))),

	PARROT(DropFix(EntityType.PARROT, arrayOf(
		arrayOf(item(FEATHER, ::lootItem))
	))),

	HOGLIN(DropFix(EntityType.HOGLIN, arrayOf(
		arrayOf(item(LEATHER, lootMulti(2)), item(noBaby(onFire(PORKCHOP, COOKED_PORKCHOP)), lootMulti(3)))
	)));

	companion object {
		val list = values()

		init {
			list.sortBy { dropFixType -> dropFixType.dropFix.entityType }
		}
	}
}

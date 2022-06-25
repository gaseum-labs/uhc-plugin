package org.gaseumlabs.uhc.dropFix

import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.LOOT_UP_TO_CYCLE
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.endermanHolding
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.isSize
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.item
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.lootItem
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.lootMulti
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.mobArmor
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.mobInventory
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.noBaby
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.onFire
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.saddle
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.slownessArrow
import org.gaseumlabs.uhc.dropFix.DropEntry.Companion.lootUpTo
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.EntityType.PHANTOM
import org.bukkit.entity.EntityType.SLIME
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhc.gui.ItemCreator
import org.bukkit.inventory.meta.PotionMeta
import org.gaseumlabs.uhc.util.ItemUtil

private fun zombieTemplate(entityType: EntityType): DropFix {
	return DropFix(
		entityType, 1,
		arrayOf(item(ROTTEN_FLESH, ::lootItem), mobInventory()),
		arrayOf(
			RareEntry(8, item(IRON_INGOT)),
			RareEntry(14, item(CARROT)),
			RareEntry(14, item(POTATO)),
		)
	)
}

private fun horseTemplate(entityType: EntityType): DropFix {
	return DropFix(
		entityType, 1,
		arrayOf(DropEntry.horseInventory(), item(noBaby { LEATHER }, ::lootItem))
	)
}

enum class DropFixType(val dropFix: DropFix) {
	BLAZE(DropFix(EntityType.BLAZE, LOOT_UP_TO_CYCLE, arrayOf(
		item(BLAZE_ROD, ::lootUpTo)
	))),

	SPIDER(DropFix(
		EntityType.SPIDER, 1,
		arrayOf(item(STRING, ::lootItem)),
		arrayOf(RareEntry(3, item(SPIDER_EYE)))
	)),

	SKELETON(DropFix(EntityType.SKELETON, 1,
		arrayOf(item(BONE, ::lootItem), item(ARROW, ::lootItem), mobArmor()),
		arrayOf(RareEntry(12, DropEntry { _, _, _ ->
			arrayOf(ItemUtil.randomDamagedItem(BOW))
		}))
	)),

	COW(DropFix(EntityType.COW, 1,
		arrayOf(item(noBaby { LEATHER }, ::lootItem), item(noBaby(onFire(BEEF, COOKED_BEEF)), lootMulti(2)))
	)),

	HORSE(horseTemplate(EntityType.HORSE)),
	MULE(horseTemplate(EntityType.MULE)),
	DONKEY(horseTemplate(EntityType.DONKEY)),

	LLAMA(DropFix(EntityType.LLAMA, 1,
		arrayOf(item(noBaby { LEATHER }, ::lootItem))
	)),

	CHICKEN(DropFix(
		EntityType.CHICKEN, 1,
		arrayOf(item(noBaby(onFire(Material.CHICKEN, COOKED_CHICKEN)), ::lootItem)),
		arrayOf(RareEntry(2, item(noBaby { FEATHER }, ::lootItem)))
	)),

	ENDERMAN(DropFix(EntityType.ENDERMAN, LOOT_UP_TO_CYCLE,
		arrayOf(item(ENDER_PEARL, ::lootUpTo), endermanHolding())
	)),

	MAGMA_CUBE(DropFix(
		EntityType.MAGMA_CUBE, 0,
		emptyArray(),
		arrayOf(RareEntry(2, item(isSize(MAGMA_CREAM, 1), ::lootItem)))
	)),

	GHAST(DropFix(EntityType.GHAST, 1,
		arrayOf(item(GUNPOWDER, ::lootItem), item(GHAST_TEAR, ::lootItem))
	)),

	STRIDER(DropFix(
		EntityType.STRIDER, 1,
		arrayOf(item(noBaby { saddle(it) }), item(STRING, lootMulti(3))),
	)),

	CREEPER(DropFix(EntityType.CREEPER, 1,
		arrayOf(item(GUNPOWDER, ::lootItem))
	)),

	DROWNED(DropFix(
		EntityType.DROWNED, 1,
		arrayOf(mobInventory(), item(ROTTEN_FLESH, ::lootItem), item(GOLD_INGOT)),
	)),

	WITCH(DropFix(
		EntityType.WITCH, 3,
		arrayOf(
			item(GLASS_BOTTLE),
			item(GLOWSTONE_DUST),
			item(GUNPOWDER),
			item(REDSTONE),
			item(SPIDER_EYE),
			item(SUGAR),
			item(STICK),
			DropEntry { _, _, cycle ->
				arrayOf(ItemCreator.fromType(POTION, false)
					.customMeta<PotionMeta> {
						it.basePotionData = org.bukkit.potion.PotionData(arrayOf(
							PotionType.INSTANT_HEAL,
							PotionType.FIRE_RESISTANCE,
							PotionType.SPEED
						)[cycle % 3], false, false)
					}
					.create()
				)
			}
		),
	)),

	ZOMBIE(zombieTemplate(EntityType.ZOMBIE)),
	ZOMBIE_VILLAGER(zombieTemplate(EntityType.ZOMBIE_VILLAGER)),
	HUSK(zombieTemplate(EntityType.HUSK)),

	STRAY(DropFix(EntityType.STRAY, 1,
		arrayOf(item(BONE, ::lootItem), slownessArrow(), mobArmor())
	)),

	PIGLIN(DropFix(EntityType.PIGLIN, 1,
		arrayOf(mobArmor())
	)),

	RABBIT(DropFix(EntityType.RABBIT, 1,
		arrayOf(item(RABBIT_HIDE, lootMulti(2)), item(onFire(Material.RABBIT, COOKED_RABBIT), ::lootItem)),
		arrayOf(RareEntry(3, item(RABBIT_FOOT, ::lootItem)))
	)),

	PARROT(DropFix(EntityType.PARROT, 1,
		arrayOf(item(FEATHER, ::lootItem))
	)),

	HOGLIN(DropFix(EntityType.HOGLIN, 1,
		arrayOf(item(LEATHER, lootMulti(2)), item(noBaby(onFire(PORKCHOP, COOKED_PORKCHOP)), lootMulti(3)))
	)),

	SLIME(DropFix(EntityType.SLIME, 1, arrayOf(
		item(isSize(SLIME_BALL, 1), ::lootItem)
	)));

	companion object {
		val list = values()

		init {
			list.sortBy { dropFixType -> dropFixType.dropFix.entityType }
		}
	}
}

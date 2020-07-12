package com.codeland.uhc.quirk

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

object ModifiedDrops {
	private val spawnEggs = arrayOf(
			Material.CREEPER_SPAWN_EGG,
			Material.PHANTOM_SPAWN_EGG,
			Material.ZOMBIE_SPAWN_EGG,
			Material.ZOMBIE_VILLAGER_SPAWN_EGG,
			Material.HUSK_SPAWN_EGG,
			Material.SKELETON_SPAWN_EGG,
			Material.STRAY_SPAWN_EGG,
			Material.SPIDER_SPAWN_EGG,
			Material.CAVE_SPIDER_SPAWN_EGG,
			Material.DROWNED_SPAWN_EGG,
			Material.HOGLIN_SPAWN_EGG,
			Material.PIGLIN_SPAWN_EGG,
			Material.GHAST_SPAWN_EGG,
			Material.ZOMBIFIED_PIGLIN_SPAWN_EGG,
			Material.WITHER_SKELETON_SPAWN_EGG,
			Material.BLAZE_SPAWN_EGG,
			Material.ENDERMAN_SPAWN_EGG,
			Material.ENDERMITE_SPAWN_EGG,
			Material.SILVERFISH_SPAWN_EGG,
			Material.SLIME_SPAWN_EGG,
			Material.MAGMA_CUBE_SPAWN_EGG,
			Material.WITCH_SPAWN_EGG,
			Material.ZOGLIN_SPAWN_EGG
	)

	private val enemyEntities = arrayOf(
			EntityType.CREEPER,
			EntityType.PHANTOM,
			EntityType.ZOMBIE,
			EntityType.ZOMBIE_VILLAGER,
			EntityType.HUSK,
			EntityType.SKELETON,
			EntityType.STRAY,
			EntityType.SPIDER,
			EntityType.CAVE_SPIDER,
			EntityType.DROWNED,
			EntityType.HOGLIN,
			EntityType.PIGLIN,
			EntityType.GHAST,
			EntityType.ZOMBIFIED_PIGLIN,
			EntityType.WITHER_SKELETON,
			EntityType.BLAZE,
			EntityType.ENDERMAN,
			EntityType.ENDERMITE,
			EntityType.SILVERFISH,
			EntityType.SLIME,
			EntityType.MAGMA_CUBE,
			EntityType.WITCH,
			EntityType.ZOGLIN
	)

	fun getSpawnEgg(entity : EntityType) : Material? {
		if (!enemyEntities.contains(entity)) {
			return null
		}
		return spawnEggs[enemyEntities.indexOf(entity)]
	}

	fun isSpawnEgg(material: Material?) : Boolean {
		return spawnEggs.contains(material)
	}

	fun getEntityType(material: Material?) : EntityType? {
		if (!isSpawnEgg(material)) {
			return null
		}
		return enemyEntities[spawnEggs.indexOf(material)]
	}

	fun randomEnchantedBook(): ItemStack {
		val ret = ItemStack(Material.BOOK)

		val meta = ret.itemMeta

		val enchant = Enchantment.values()[(Math.random() * Enchantment.values().size).toInt()]
		meta.addEnchant(enchant, (Math.random() * (enchant.maxLevel + 1)).toInt(), true)

		ret.itemMeta = meta

		return ret
	}
}
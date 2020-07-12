package com.codeland.uhc.quirk

import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

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

	fun randomDamagedItem(type: Material): ItemStack {
		val ret = ItemStack(type)
		val damageable = ret.itemMeta as Damageable
		damageable.damage = (type.maxDurability * Math.random()).toInt()
		ret.itemMeta = damageable as ItemMeta

		return ret
	}

	fun namedItem(type: Material, name: String): ItemStack {
		val ret = ItemStack(Material.CARROT)
		val meta = ret.itemMeta
		meta.setDisplayName(name)
		ret.itemMeta = meta

		return ret
	}

	fun addRandomEnchants(itemStack: ItemStack, enchantList: Array<Enchantment>, probability: Float): ItemStack {
		var enchantIndex = (Math.random() * enchantList.size * (1 / probability)).toInt()

		if (enchantIndex < enchantList.size) {
			val enchantment = enchantList[enchantIndex]

			val meta = itemStack.itemMeta
			meta.addEnchant(enchantment, randRange(1, enchantment.maxLevel), true)
			itemStack.itemMeta = meta
		}

		return itemStack
	}

	fun randRange(low: Int, high: Int): Int {
		return ((Math.random() * high - low + 1) + low).toInt()
	}

	fun onDrop(type: EntityType, drops: MutableList<ItemStack>) {
		val rand = Math.random()

		when (type) {
			EntityType.CREEPER -> {
				val amount = randRange(1, 4)

				drops.add(when {
					rand < 0.25 -> ItemStack(Material.TNT, amount)
					rand < 0.5 -> ItemStack(Material.FIREWORK_STAR, amount)
					else -> ItemStack(Material.GUNPOWDER, amount)
				})
			}

			EntityType.PHANTOM -> {
				drops.add(randomDamagedItem(Material.ELYTRA))
			}

			EntityType.ZOMBIE, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER -> {
				if (Math.random() < 0.04)
					for (i in 0..30)
						drops.add(namedItem(Material.CARROT, "${ChatColor.GOLD}${ChatColor.BOLD}Carrot Warrior #${randRange(0, Int.MAX_VALUE - 1)}"))
				else
					drops.add(ItemStack(Material.CARROT, randRange(1, 4)))
			}

			EntityType.SKELETON, EntityType.STRAY -> {
				val crossbow = randomDamagedItem(Material.CROSSBOW)

				addRandomEnchants(crossbow, arrayOf(
					Enchantment.MULTISHOT,
					Enchantment.QUICK_CHARGE,
					Enchantment.PIERCING
				), 0.5f)

				drops.add(crossbow)
			}

			EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH -> {
				drops.add(ItemStack(Material.PAPER, randRange(1, 4)))

				if (Math.random() < 0.05)
					drops.add(randomEnchantedBook())
			}

			EntityType.DROWNED -> {
				val trident = randomDamagedItem(Material.TRIDENT)

				addRandomEnchants(trident, arrayOf(
					Enchantment.RIPTIDE,
					Enchantment.CHANNELING,
					Enchantment.LOYALTY,
					Enchantment.IMPALING
				), 0.5f)

				drops.add(trident)
			}
		}
	}
}
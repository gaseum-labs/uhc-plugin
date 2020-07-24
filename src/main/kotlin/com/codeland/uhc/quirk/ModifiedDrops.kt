package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner.Companion.randRange
import net.md_5.bungee.api.ChatColor
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.ItemMeta

class ModifiedDrops(type: QuirkType) : Quirk(type) {
	override fun onEnable() {}

	override fun onDisable() {}

	companion object {
		fun onDrop(type: EntityType, drops: MutableList<ItemStack>) {
			val rand = Math.random()
			val third = 1 / 3.0
			val twoThirds = 2 / 3.0

			when (type) {
				EntityType.CREEPER -> {
					drops.add(when {
						rand < 0.25 -> ItemStack(Material.TNT, randRange(1, 3))
						rand < 0.5 -> ItemUtil.fireworkStar(randRange(1, 3), Color.LIME)
						else -> ItemStack(Material.GUNPOWDER, randRange(2, 6))
					})
				}

				EntityType.PHANTOM -> {
					if (Math.random() < twoThirds) {
						val elytra = ItemUtil.randomDamagedItem(Material.ELYTRA)

						ItemUtil.addRandomEnchants(elytra, arrayOf(
							Enchantment.DURABILITY,
							Enchantment.MENDING
						), 0.5)

						drops.add(elytra)
					}
				}

				EntityType.ZOMBIE, EntityType.HUSK, EntityType.ZOMBIE_VILLAGER -> {
					if (Math.random() < 0.04)
						for (i in 0..30)
							drops.add(ItemUtil.namedItem(Material.CARROT, "${ChatColor.GOLD}${ChatColor.BOLD}Carrot Warrior #${randRange(0, Int.MAX_VALUE - 1)}"))
					else
						drops.add(ItemStack(Material.CARROT))

					if (Math.random() < 0.15) {
						val egg = Summoner.getSpawnEgg(type, true, false)

						if (egg != null)
							drops.add(ItemStack(egg))
					}
				}

				EntityType.SKELETON, EntityType.STRAY -> {
					/* if naturally drops bow let it */
					if (drops.find { it.type == Material.BOW } == null) {
						if (rand < 0.33) {
							val bow = ItemUtil.randomDamagedItem(Material.BOW)

							ItemUtil.addRandomEnchants(bow, arrayOf(
									Enchantment.ARROW_DAMAGE,
									Enchantment.ARROW_KNOCKBACK,
									Enchantment.ARROW_INFINITE,
									Enchantment.ARROW_FIRE,
									Enchantment.MENDING,
									Enchantment.DURABILITY
							), 0.5)

							drops.add(bow)

						} else if (rand < (2 / 3.0f)) {
							val crossbow = ItemUtil.randomDamagedItem(Material.CROSSBOW)

							ItemUtil.addRandomEnchants(crossbow, arrayOf(
									Enchantment.MULTISHOT,
									Enchantment.QUICK_CHARGE,
									Enchantment.PIERCING,
									Enchantment.MENDING,
									Enchantment.DURABILITY
							), 0.5)

							drops.add(crossbow)
						}
					}
				}

				EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.SILVERFISH -> {
					val amount = randRange(0, 3)

					if (amount > 0)
						drops.add(ItemStack(Material.PAPER, amount))

					if (Math.random() < 0.04)
						drops.add(ItemUtil.randomEnchantedBook())
				}

				EntityType.DROWNED -> {
					val trident = ItemUtil.randomDamagedItem(Material.TRIDENT)

					ItemUtil.addRandomEnchants(trident, arrayOf(
							Enchantment.RIPTIDE,
							Enchantment.LOYALTY,
							Enchantment.MENDING,
							Enchantment.DURABILITY
					), 0.5)

					drops.add(trident)
				}
			}
		}
	}
}
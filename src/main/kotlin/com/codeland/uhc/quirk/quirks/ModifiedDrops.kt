package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.UHC
import com.codeland.uhc.dropFix.DropEntry
import com.codeland.uhc.dropFix.DropEntry.Companion.arrayItem
import com.codeland.uhc.dropFix.DropEntry.Companion.item
import com.codeland.uhc.dropFix.DropEntry.Companion.lootMulti
import com.codeland.uhc.dropFix.DropEntry.Companion.nothing
import com.codeland.uhc.dropFix.DropEntry.Companion.stackItem
import com.codeland.uhc.dropFix.DropEntry.Companion.lootItem
import com.codeland.uhc.dropFix.DropFix
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.phase.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

class ModifiedDrops(type: QuirkType) : Quirk(type) {
	override fun onEnable() {}

	override fun customDestroy() {}

	override val representation = ItemCreator.fromType(Material.STRING)

	override fun onPhaseSwitch(phase: PhaseVariant) {
		if (phase.type == PhaseType.GRACE) {
			Bukkit.getServer().onlinePlayers.forEach { player ->
				player.setStatistic(Statistic.TIME_SINCE_REST, 72000)
			}
		}
	}

	override fun customDrops(): Array<DropFix> {
		return arrayOf(
			DropFix(EntityType.CREEPER, arrayOf(
				arrayOf(item(Material.TNT, lootMulti(2))),
				arrayOf(stackItem { ItemUtil.fireworkStar(2, Color.LIME) }),
				arrayOf(item(Material.GUNPOWDER, lootMulti(4)))
			), arrayOf(
				nothing()
			)),

			DropFix(EntityType.PHANTOM, arrayOf(
				arrayOf(stackItem { ItemUtil.addRandomEnchants(
					ItemUtil.randomDamagedItem(Material.ELYTRA), arrayOf(
						Enchantment.DURABILITY,
						Enchantment.MENDING
					), 0.5)
				}, item(Material.PHANTOM_MEMBRANE, ::lootItem))
			), arrayOf(
				nothing()
			)),

			DropFix(EntityType.ZOMBIE, arrayOf(
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem), item(Summoner.getSpawnEgg(EntityType.ZOMBIE, true, false) ?: Material.STONE)),
				arrayOf(arrayItem { Array(30) {
					ItemUtil.namedItem(Material.CARROT, "${ChatColor.GOLD}${ChatColor.BOLD}Carrot Warrior #${Util.randRange(0, Int.MAX_VALUE - 1)}")
				}}),
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem)),
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem)),
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem)),
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem)),
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem)),
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem)),
			), arrayOf(
				item(Material.ROTTEN_FLESH)
			)),

			DropFix(EntityType.SKELETON, arrayOf(
				arrayOf(item(Material.BONE, ::lootItem), item(Material.ARROW, ::lootItem)),
				arrayOf(item(Material.BONE, ::lootItem), item(Material.ARROW, ::lootItem)),
				arrayOf(item(Material.BONE, ::lootItem), item(Material.ARROW, ::lootItem), stackItem {
					ItemUtil.addRandomEnchants(ItemUtil.randomDamagedItem(Material.BOW), arrayOf(
						Enchantment.ARROW_DAMAGE,
						Enchantment.ARROW_KNOCKBACK,
						Enchantment.ARROW_INFINITE,
						Enchantment.ARROW_FIRE,
						Enchantment.MENDING,
						Enchantment.DURABILITY
					), 0.5)
				}),
				arrayOf(item(Material.BONE, ::lootItem), item(Material.ARROW, ::lootItem), stackItem {
					ItemUtil.addRandomEnchants(ItemUtil.randomDamagedItem(Material.CROSSBOW), arrayOf(
						Enchantment.MULTISHOT,
						Enchantment.QUICK_CHARGE,
						Enchantment.PIERCING,
						Enchantment.MENDING,
						Enchantment.DURABILITY
					), 0.5)
				})
			), arrayOf(
				item(Material.BONE)
			)),

			DropFix(EntityType.SPIDER, arrayOf(
				arrayOf(item(Material.STRING, ::lootItem), item(Material.PAPER, lootMulti(2)), item(Material.SPIDER_EYE, lootMulti(-1))),
				arrayOf(item(Material.STRING, ::lootItem), item(Material.PAPER, lootMulti(2)), item(Material.SPIDER_EYE, lootMulti( 0))),
				arrayOf(item(Material.STRING, ::lootItem), item(Material.PAPER, lootMulti(2)), item(Material.SPIDER_EYE, lootMulti( 1))),
				arrayOf(item(Material.STRING, ::lootItem), item(Material.PAPER, lootMulti(2)), item(Material.SPIDER_EYE, lootMulti(-1))),
				arrayOf(item(Material.STRING, ::lootItem), item(Material.PAPER, lootMulti(2)), item(Material.SPIDER_EYE, lootMulti( 0))),
				arrayOf(item(Material.STRING, ::lootItem), item(Material.PAPER, lootMulti(2)), item(Material.SPIDER_EYE, lootMulti( 1)),
					stackItem { ItemUtil.randomEnchantedBook() }
				)
			), arrayOf(
				nothing()
			)),

			DropFix(EntityType.DROWNED, arrayOf(
				arrayOf(item(Material.ROTTEN_FLESH, ::lootItem), item(Material.GOLD_INGOT), stackItem {
					ItemUtil.addRandomEnchants(ItemUtil.randomDamagedItem(Material.TRIDENT), arrayOf(
						Enchantment.RIPTIDE,
						Enchantment.LOYALTY,
						Enchantment.MENDING,
						Enchantment.DURABILITY
					), 0.5)
				})
			), arrayOf(
				item(Material.ROTTEN_FLESH)
			))
		)
	}
}

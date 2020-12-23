package com.codeland.uhc.world.chunkPlacer.impl

import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta

class DungeonChestReplacer(size: Int, uniqueSeed: Int) : ImmediateChunkPlacer(size, uniqueSeed) {
	val NUM_ITEMS = 8

	override fun place(chunk: Chunk, chunkIndex: Int) {
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 0..255) {
					val block = chunk.getBlock(x, y, z)

					if (block.type == Material.CHEST) {
						val chest = block.state as Chest

						chest.inventory.clear()

						val numRares = Util.randRange(1, 2)
						val numCommons = NUM_ITEMS - numRares

						for (i in 0 until numRares) ItemUtil.randomAddInventory(chest.inventory, Util.randFromArray(rareEntries)())
						for (i in 0 until numCommons) ItemUtil.randomAddInventory(chest.inventory, Util.randFromArray(chestEntries)())
					}
				}
			}
		}
	}

	val chestEntries = arrayOf(
		{ ItemStack(Material.GUNPOWDER, 4) },
		{ genItem(Material.STRING, 2, 3) },
		{ genItem(Material.BREAD, 6, 10) },
		{ ItemStack(Material.SADDLE) },
		{ genItem(Material.COAL, 8, 16) },
		{ genItem(Material.REDSTONE, 8, 16) },
		{ ItemUtil.randomMusicDisc() },
		{ genItem(Material.IRON_INGOT, 4, 8) },
		{ ItemStack(Material.WATER_BUCKET) },
		{ genItem(Material.GOLD_INGOT, 4, 8) },
	)

	val enchants = arrayOf(
		Enchantment.DAMAGE_ALL,
		Enchantment.ARROW_DAMAGE,
		Enchantment.THORNS,
		Enchantment.KNOCKBACK,
		Enchantment.PROTECTION_PROJECTILE,
	)

	val rareEntries = arrayOf(
		{ ItemStack(Material.GOLDEN_APPLE) },
		{
			val book = ItemStack(Material.ENCHANTED_BOOK)
			val meta = book.itemMeta as EnchantmentStorageMeta
			meta.addStoredEnchant(Util.randFromArray(enchants), 1, true)
			book.itemMeta = meta
			book
		}
	)

	fun genItem(type: Material, vararg amounts: Int): ItemStack {
		return ItemStack(type, amounts[(Math.random() * amounts.size).toInt()])
	}
}

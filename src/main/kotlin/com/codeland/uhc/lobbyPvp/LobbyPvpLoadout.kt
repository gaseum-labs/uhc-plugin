package com.codeland.uhc.lobbyPvp

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object LobbyPvpLoadout {
	val loadoutItems = arrayOf(
		LoadoutItems.IRON_HELMET,
		LoadoutItems.IRON_CHESTPLATE,
		LoadoutItems.IRON_LEGGINGS,
		LoadoutItems.IRON_BOOTS,
		LoadoutItems.DIAMOND_HELMET,
		LoadoutItems.DIAMOND_CHESTPLATE,
		LoadoutItems.DIAMOND_LEGGINGS,
		LoadoutItems.DIAMOND_BOOTS,
		LoadoutItems.IRON_SWORD,
		LoadoutItems.DIAMOND_SWORD,
		LoadoutItems.IRON_AXE,
		LoadoutItems.DIAMOND_AXE,
		LoadoutItems.BOW,
		LoadoutItems.CROSSBOW,
		LoadoutItems.SHIELD,
		LoadoutItems.PICKAXE,
		LoadoutItems.ARROWS,
		LoadoutItems.ARROWS_2,
		LoadoutItems.SPECTRAL_ARROWS,
		LoadoutItems.SPECTRAL_ARROWS_2,
		LoadoutItems.WATER_BUCKET,
		LoadoutItems.LAVA_BUCKET,
		LoadoutItems.BLOCKS,
		LoadoutItems.BLOCKS_2,
		LoadoutItems.ENDER_PEARLS,
		LoadoutItems.GOLDEN_APPLES,
		LoadoutItems.SPEED_POTION,
		LoadoutItems.SPEED_POTION_2,
		LoadoutItems.HEALTH_POTION,
		LoadoutItems.HEALTH_POTION_2,
		LoadoutItems.DAMAGE_POTION,
		LoadoutItems.DAMAGE_POTION_2,
	)

	class PlayerLoadout() {
		val inventory = Array(9 * 4) { -1 }

		fun save(outputStream: OutputStream) {

		}

		companion object {
			//fun load(byteArray: ByteArray): PlayerLoadout? {

			//}
		}
	}

	//fun createInventory(): Inventory {

	//}
}

package org.gaseumlabs.uhc.chc.chcs.banana

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.util.Util

object BananaUtil {
	val scrapMaterials = arrayOf(
		Material.IRON_INGOT to 4,
		Material.COBBLESTONE to 32,
		Material.OBSIDIAN to 4,
		Material.ENDER_CHEST to 1,
		Material.GOLD_INGOT to 4,
		Material.ENDER_PEARL to 1,
		Material.SUGAR_CANE to 9,
		Material.LEATHER to 1,
		Material.COAL to 12,
		Material.STRING to 3,
		Material.APPLE to 1,
		Material.GUNPOWDER to 3,
		Material.EMERALD to 5,
		Material.LAPIS_LAZULI to 14,
		Material.DIAMOND to 1,
		Material.OAK_LOG to 6,
		Material.BONE_MEAL to 18,
		Material.MELON_SLICE to 1,
		Material.COOKED_BEEF to 5,
	)

	val smelts2 = HashMap<Material, ItemStack>()
	init {
		Bukkit.recipeIterator().forEach { recipe ->
			if (recipe is FurnaceRecipe) {
				smelts2[recipe.input.type] = recipe.result
			}
		}
	}

	val logs = Util.sortedArrayOf(
		*Material.values().filter { it.name.endsWith("_LOG") }.toTypedArray()
	)

	fun isLog(block: Block) = Util.binarySearch(block.type, logs)

	val facings = arrayOf(
		BlockFace.EAST,
		BlockFace.WEST,
		BlockFace.NORTH,
		BlockFace.SOUTH,
	)

	fun randomFacing() = facings.random()
}
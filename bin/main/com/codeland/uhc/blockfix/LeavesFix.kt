package com.codeland.uhc.blockfix

import com.codeland.uhc.util.Util
import com.codeland.uhc.util.Util.binarySearch
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class LeavesFix : BlockFix("Leaves", arrayOf(
	Range.countOffRange("Apple", 200, { _, _ -> ItemStack(Material.APPLE) }, { _, drops ->
		val drop = drops.firstOrNull()
		if (drop != null && isLeaves(drop.type)) drop else null
	}),
	Range.countRange("Stick", 50) { _, _ -> ItemStack(Material.STICK) },
	Range.countRange("Sapling", 20) { leaves, _ ->
		ItemStack(Util.binaryFind(leaves, leavesInfo) { info -> info.leaves }?.sapling ?: Material.OAK_SAPLING)
	}
)) {
	init {
		leavesInfo.sortBy { info -> info.leaves }
	}

	override fun isBlock(material: Material): Boolean {
		return isLeaves(material)
	}

	override fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean {
		return isSilkTouch(tool)
	}

	override fun allowTool(tool: ItemStack): Boolean {
		return true
	}

	companion object {
		class LeafInfo(var leaves: Material, var sapling: Material)

		fun isLeaves(material: Material): Boolean {
			return binarySearch(material, leavesInfo) { leafInfo -> leafInfo.leaves }
		}

		val leavesInfo = arrayOf(
			LeafInfo(Material.OAK_LEAVES, Material.OAK_SAPLING),
			LeafInfo(Material.SPRUCE_LEAVES, Material.SPRUCE_SAPLING),
			LeafInfo(Material.BIRCH_LEAVES, Material.BIRCH_SAPLING),
			LeafInfo(Material.JUNGLE_LEAVES, Material.JUNGLE_SAPLING),
			LeafInfo(Material.ACACIA_LEAVES, Material.ACACIA_SAPLING),
			LeafInfo(Material.DARK_OAK_LEAVES, Material.DARK_OAK_SAPLING)
		)
	}
}

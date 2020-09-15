package com.codeland.uhc.blockfix

import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import com.codeland.uhc.util.Util.binarySearch
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class LeavesFix : BlockFix("Leaves", arrayOf(
	Range("apple", "appleCount", "appleIndex", 200) { leaves -> ItemStack(Material.APPLE) },
	Range("stick", "stickCount", "stickIndex", 50) { leaves -> ItemStack(Material.STICK, Util.randRange(1, 2)) },
	Range("sapling", "saplingCount", "saplingIndex", 20) { leaves ->
		ItemStack(Util.binaryFind(leaves, leavesInfo) { info -> info.leaves }?.sapling ?: Material.OAK_SAPLING)
	}
)) {
	init {
		leavesInfo.sortBy { info -> info.leaves }
	}

	fun isLeaves(material: Material): Boolean {
		return binarySearch(material, leavesInfo) { leafInfo -> leafInfo.leaves }
	}

	override fun isBlock(material: Material): Boolean {
		return isLeaves(material)
	}

	override fun reject(uhc: UHC, drops: List<Item>): Boolean {
		return !uhc.appleFix || (drops.isNotEmpty() && isLeaves(drops[0].itemStack.type))
	}

	override fun allowTool(item: ItemStack): Boolean {
		return true
	}

	companion object {
		class LeafInfo(var leaves: Material, var sapling: Material)

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

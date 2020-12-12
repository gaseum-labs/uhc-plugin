package com.codeland.uhc.blockfix

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

abstract class BlockFix(val prettyName: String, val ranges: Array<Range>) {
	data class Range(
		val prettyName: String,
		val countMeta: String,
		val indexMeta: String,
		val range: Int,
		val getDrop: (Material) -> ItemStack,
		val offDrop: (Material) -> ItemStack? = { null }
	)

	abstract fun isBlock(material: Material): Boolean
	abstract fun reject(uhc: UHC, tool: ItemStack, drops: List<Item>): Boolean
	abstract fun allowTool(tool: ItemStack): Boolean

	fun getInfoString(player: Player, onString: (output: String) -> Unit) {
		onString("${ChatColor.GOLD}<$prettyName Ranges>")

		ranges.forEach { range ->
			onString("${ChatColor.YELLOW}${range.prettyName} count: ${getCount(player, range)} next drop: ${getIndex(player, range)}")
		}
	}

	/**
	 * @return the new count after incrementing
	 */
	fun increaseCount(player: Player, range: Range): Int {
		val meta = player.getMetadata(range.countMeta)

		val previous = if (meta.size == 0) resetCount(player, range)
		else meta[0].asInt()

		player.setMetadata(range.countMeta, FixedMetadataValue(UHCPlugin.plugin, previous + 1))

		return previous + 1
	}

	/**
	 * @return the new count, will always be 0
	 */
	fun resetCount(player: Player, range: Range): Int {
		player.setMetadata(range.countMeta, FixedMetadataValue(UHCPlugin.plugin, 0))

		return 0
	}

	fun getCount(player: Player, range: Range): Int {
		val meta = player.getMetadata(range.countMeta)

		return if (meta.size == 0) resetCount(player, range)
		else meta[0].asInt()
	}

	fun getIndex(player: Player, range: Range): Int {
		val meta = player.getMetadata(range.indexMeta)

		return if (meta.size == 0) resetIndex(player, range)
		else meta[0].asInt()
	}

	/**
	 * @return the new index
	 */
	fun resetIndex(player: Player, range: Range): Int {
		val index = Util.randRange(1, range.range)

		player.setMetadata(range.indexMeta, FixedMetadataValue(UHCPlugin.plugin, index))

		return index
	}

	fun onBreakBlock(uhc: UHC, material: Material, drops: MutableList<Item>, player: Player, onItem: (ItemStack?) -> Unit): Boolean {
		val tool = player.inventory.itemInMainHand

		if (isBlock(material) && !reject(uhc, tool, drops)) {
			drops.clear()

			if (allowTool(player.inventory.itemInMainHand)) onBreakBlock(material, player, onItem)

			return true
		}

		return false
	}

	fun onBreakBlock(material: Material, player: Player, onItem: (ItemStack?) -> Unit) {
		ranges.forEach { range ->
			var count = increaseCount(player, range)

			onItem(if (count == getIndex(player, range)) range.getDrop(material) else range.offDrop(material))

			if (count == range.range) {
				resetIndex(player, range)
				resetCount(player, range)
			}
		}
	}

	companion object {
		fun isSilkTouch(tool: ItemStack): Boolean {
			return tool.hasItemMeta() && tool.itemMeta.enchants.containsKey(Enchantment.SILK_TOUCH)
		}
	}
}
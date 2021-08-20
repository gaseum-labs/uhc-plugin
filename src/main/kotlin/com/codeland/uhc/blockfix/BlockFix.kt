package com.codeland.uhc.blockfix

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.Util
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import kotlin.random.Random

typealias Drop = (Material, MutableList<ItemStack>) -> ItemStack?

abstract class BlockFix(val prettyName: String, val ranges: Array<Range>) {
	data class Range private constructor(
		val prettyName: String,
		val size: Int,
		val onDrop: Drop,
		val offDrop: Drop
	) {
		val countMeta: String = "${prettyName}Count"
		val indexMeta: String = "${prettyName}Index"

		companion object {
			fun countRange(prettyName: String, size: Int, onDrop: Drop): Range {
				return Range(prettyName, size, onDrop) { _, _ -> null }
			}

			fun countOffRange(prettyName: String, size: Int, onDrop: Drop, offDrop: Drop): Range {
				return Range(prettyName, size, onDrop, offDrop)
			}

			fun nonCountRange(drop: Drop): Range {
				return Range("Default", 0, drop, drop)
			}
		}
	}

	val random = Random(prettyName.hashCode())

	abstract fun isBlock(material: Material): Boolean
	abstract fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean
	abstract fun allowTool(tool: ItemStack): Boolean

	fun getInfoString(player: Player, onString: (output: String) -> Unit) {
		onString("${ChatColor.GOLD}<$prettyName Ranges>")

		ranges.forEach { range ->
			if (range.size == 0)
				onString("${ChatColor.YELLOW}${range.prettyName}")
			else
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
		val index = random.nextInt(0, range.size) + 1

		player.setMetadata(range.indexMeta, FixedMetadataValue(UHCPlugin.plugin, index))

		return index
	}

	fun onBreakBlock(material: Material, drops: MutableList<Item>, player: Player, onItem: (ItemStack?) -> Unit): Boolean {
		val tool = player.inventory.itemInMainHand
		val stackDrops = drops.map { it.itemStack } as MutableList<ItemStack>

		if (isBlock(material) && !reject(tool, stackDrops)) {
			drops.clear()

			if (allowTool(player.inventory.itemInMainHand)) ranges.forEach { range ->
				/* noncounting ranges */
				if (range.size == 0) {
					onItem(range.onDrop(material, stackDrops))

				/* counting ranges */
				} else {
					val count = increaseCount(player, range)

					onItem(if (count == getIndex(player, range)) range.onDrop(material, stackDrops) else range.offDrop(material, stackDrops))

					if (count == range.size) {
						resetIndex(player, range)
						resetCount(player, range)
					}
				}
			}

			return true
		}

		return false
	}

	companion object {
		fun isSilkTouch(tool: ItemStack): Boolean {
			return tool.hasItemMeta() && tool.itemMeta.enchants.containsKey(Enchantment.SILK_TOUCH)
		}
	}
}
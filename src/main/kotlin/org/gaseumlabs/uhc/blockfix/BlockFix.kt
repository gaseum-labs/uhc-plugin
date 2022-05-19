package org.gaseumlabs.uhc.blockfix

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material.AIR
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.random.Random
import kotlin.random.nextInt

abstract class BlockFix(val prettyName: String, val ranges: Array<org.gaseumlabs.uhc.blockfix.Range>) {
	abstract fun isBlock(blockState: BlockState): Boolean
	abstract fun reject(tool: ItemStack, drops: List<ItemStack>): Boolean
	abstract fun allowTool(tool: ItemStack): Boolean

	fun getInfoString(player: Player, onString: (output: Component) -> Unit) {
		onString(Component.text("<$prettyName Ranges>", NamedTextColor.GOLD))

		ranges.forEach { range ->
			onString(when (range) {
				is SingleRange -> Component.text(range.prettyName, NamedTextColor.YELLOW)
				is CountingRange -> {
					val metadata = range.getMeta(player)
					Component.text(
						"${range.prettyName} count: ${metadata.count} next drop: ${metadata.index}",
						NamedTextColor.YELLOW
					)
				}
			})
		}
	}

	fun onNaturalBreakBlock(
		block: Block,
		drops: MutableList<ItemStack>,
		onItem: (ItemStack?) -> Unit,
	): Boolean {
		val tool = ItemStack(AIR)
		if (isBlock(block.state) && !reject(tool, drops)) {
			drops.clear()

			if (allowTool(tool)) ranges.forEach { range ->
				when (range) {
					is SingleRange -> {
						onItem(range.onDrop(block.type, drops, 0))
					}
					/* no multi ranges */
					else -> {
					}
				}
			}

			return true
		}

		return false
	}

	fun onBreakBlock(
		blockState: BlockState,
		drops: MutableList<Item>,
		player: Player,
		onItem: (ItemStack?) -> Unit,
	): Boolean {
		val tool = player.inventory.itemInMainHand
		val stackDrops = drops.map { it.itemStack } as MutableList<ItemStack>

		if (isBlock(blockState) && !reject(tool, stackDrops)) {
			val fortune = getFortune(player.inventory.itemInMainHand)
			drops.clear()

			if (allowTool(player.inventory.itemInMainHand)) ranges.forEach { range ->
				when (range) {
					is SingleRange -> {
						onItem(range.onDrop(blockState.type, stackDrops, fortune))
					}
					is CountingRange -> {
						val metadata = range.getMeta(player)

						for (i in 0 until 1 + fortune) {
							if (++metadata.count == metadata.index) {
								onItem(range.onDrop(blockState.type, stackDrops))
							} else if (i == 0) {
								onItem(range.offDrop(blockState.type, stackDrops))
							}

							if (metadata.count >= range.size) {
								metadata.count = 0
								metadata.index = Random.nextInt(1..range.size)
							}
						}
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

		fun getFortune(tool: ItemStack): Int {
			return if (tool.hasItemMeta()) tool.itemMeta.enchants[Enchantment.LOOT_BONUS_BLOCKS] ?: 0 else 0
		}
	}
}

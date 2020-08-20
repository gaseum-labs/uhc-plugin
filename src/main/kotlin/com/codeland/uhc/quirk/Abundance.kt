package com.codeland.uhc.quirk

import com.codeland.uhc.util.ItemUtil
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Abundance(type: QuirkType) : Quirk(type) {
	override fun onEnable() {
		/* ya nothing needs to be done */
	}

	override fun onDisable() {

	}

	companion object {
		fun makeFortunteTool(itemInHand: ItemStack): ItemStack {
			var fakeTool = itemInHand.clone()

			if (fakeTool.type == Material.AIR)
				fakeTool = ItemStack(Material.PORKCHOP)

			ItemUtil.enchantThing(fakeTool, Enchantment.LOOT_BONUS_BLOCKS, 4)

			return fakeTool
		}

		fun replaceDrops(player: Player, block: Block, oldState: BlockState, drops: MutableList<Item>) {
			val fakeTool = makeFortunteTool(player.inventory.itemInMainHand)

			/* this is so gross but it's the only way */
			val destroyedType = block.type
			val destroyedData = block.blockData

			block.type = block.type
			block.blockData = oldState.block.blockData

			val extraDrops = block.getDrops(fakeTool)

			block.type = destroyedType
			block.blockData = destroyedData
			/* end gross block */

			drops.clear()

			extraDrops.forEach { extraDrop ->
				player.world.dropItem(block.location.add(0.5, 0.5, 0.5), extraDrop)
			}
		}
	}
}
package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.ItemUtil
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Abundance(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	override fun onEnable() {
		/* ya nothing needs to be done */
	}

	override fun onDisable() {

	}

	companion object {
		private fun makeFortuneTool(itemInHand: ItemStack): ItemStack {
			var fakeTool = itemInHand.clone()

			if (fakeTool.type == Material.AIR)
				fakeTool = ItemStack(Material.PORKCHOP)

			ItemUtil.enchantThing(fakeTool, Enchantment.LOOT_BONUS_BLOCKS, 4)

			return fakeTool
		}

		fun replaceDrops(player: Player, block: Block, oldState: BlockState, drops: MutableList<Item>) {
			val fakeTool = makeFortuneTool(player.inventory.itemInMainHand)

			/* remember what the block is after it is destroyed */
			val destroyedType = block.type
			val destroyedData = block.blockData

			/* set the block back to what it was before it was destroyed */
			block.type = oldState.type
			block.blockData = oldState.blockData

			val abundanceDrops = block.getDrops(fakeTool)

			/* set the block back to what it was after it was destroyed */
			block.type = destroyedType
			block.blockData = destroyedData

			drops.clear()

			abundanceDrops.forEach { drop ->
				/* mushroom blocks like to drop air for some reason */
				if (drop.type != Material.AIR)
					player.world.dropItem(block.location.add(0.5, 0.5, 0.5), drop)
			}
		}
	}
}
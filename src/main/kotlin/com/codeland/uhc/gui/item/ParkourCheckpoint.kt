package com.codeland.uhc.gui.item

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

object ParkourCheckpoint {
	val MATERIAL = Material.GOLD_NUGGET

	fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.setDisplayName("${ChatColor.RESET}${ChatColor.YELLOW}Parkour Checkpoint")
		meta.lore = listOf("Right click to go to the last checkpoint")

		stack.itemMeta = meta
		return stack
	}

	fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	fun hasItem(inventory: Inventory): Boolean {
		return inventory.contents.any { stack ->
			if (stack == null) return@any false

			isItem(stack)
		}
	}

	val META_TAG = "uhc_checkpoint"
	val CHECKPOINT = Material.GOLD_BLOCK

	fun setPlayerCheckpoint(player: Player, location: Location) {
		player.setMetadata(META_TAG, FixedMetadataValue(UHCPlugin.plugin, location))
	}

	fun getPlayerCheckpoint(player: Player): Location? {
		val list = player.getMetadata(META_TAG)

		if (list.isEmpty()) return null

		return list[0].value() as Location
	}

	fun updateCheckpoint(player: Player) {
		val underLocation = player.location.clone().subtract(0.0, 1.0, 0.0).toBlockLocation()

		if (player.world.getBlockAt(underLocation).type == CHECKPOINT) {
			val oldLocation = getPlayerCheckpoint(player)?.toBlockLocation()
			val newLocation = underLocation.add(0.0, 1.0, 0.0).toBlockLocation()

			if (oldLocation == null ||
				(
					newLocation.x.toInt() != oldLocation.x.toInt() ||
					newLocation.y.toInt() != oldLocation.y.toInt() ||
					newLocation.z.toInt() != oldLocation.z.toInt()
				)
			) {
				setPlayerCheckpoint(player, newLocation)
				GameRunner.sendGameMessage(player, "New Checkpoint!")
			}
		}
	}
}

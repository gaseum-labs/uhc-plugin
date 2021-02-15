package com.codeland.uhc.gui.item

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phase.PhaseType
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue

class ParkourCheckpoint : CommandItem() {
	val MATERIAL = Material.GOLD_NUGGET

	override fun create(): ItemStack {
		val stack = ItemStack(MATERIAL)
		val meta = stack.itemMeta

		meta.setDisplayName("${ChatColor.RESET}${ChatColor.YELLOW}Parkour Checkpoint")
		meta.lore = listOf("Right click to go to the last checkpoint")

		stack.itemMeta = meta
		return stack
	}

	override fun isItem(stack: ItemStack): Boolean {
		return stack.type == MATERIAL && stack.itemMeta.hasLore() && stack.itemMeta.hasDisplayName()
	}

	override fun onUse(uhc: UHC, player: Player) {
		val location = getPlayerCheckpoint(player)?.toBlockLocation()
			?: return Commands.errorMessage(player, "Reach a gold block to get a checkpoint!")

		val block = Bukkit.getWorlds()[0].getBlockAt(location.clone().subtract(0.0, 1.0, 0.0).toBlockLocation())
		if (block.type != CHECKPOINT)
			return Commands.errorMessage(player, "Checkpoint has been removed!")

		player.teleport(location.add(0.5, 0.0, 0.5))
	}

	companion object {
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

		fun lobbyParkourTick() {
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				val player = Bukkit.getPlayer(uuid)

				if (player != null && !playerData.participating) {
					val underLocation = player.location.clone().subtract(0.0, 1.0, 0.0).toBlockLocation()

					if (player.world.getBlockAt(underLocation).type == CHECKPOINT) {
						val oldLocation = getPlayerCheckpoint(player)?.toBlockLocation()
						val newLocation = underLocation.add(0.0, 1.0, 0.0).toBlockLocation()

						if (oldLocation == null || (newLocation.block !== oldLocation.block)) {
							setPlayerCheckpoint(player, newLocation)
							GameRunner.sendGameMessage(player, "New Checkpoint!")
						}
					}
				}
			}
		}
	}
}

package com.codeland.uhc.event

import com.codeland.uhc.core.PlayerData
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent

class PvpListener : Listener {
	@EventHandler
	fun onBowShoot(event: EntityShootBowEvent) {
		val projectile = event.projectile as? AbstractArrow

		if (projectile != null) {
			val player = projectile.shooter as? Player

			if (player != null) {
				PlayerData.getLobbyPvp(player.uniqueId).stillTime = 0

				projectile.location.direction = player.location.direction
				projectile.velocity = player.location.direction.multiply(projectile.velocity.length())
			}
		}
	}

	@EventHandler
	fun onStartMining(event: BlockDamageEvent) {
		PlayerData.getLobbyPvp(event.player.uniqueId).stillTime = 0
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		PlayerData.getLobbyPvp(event.player.uniqueId).stillTime = 0
	}

	@EventHandler
	fun onInventory(event: InventoryClickEvent) {
		PlayerData.getLobbyPvp(event.whoClicked.uniqueId).stillTime = 0
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		PlayerData.getLobbyPvp(event.player.uniqueId).stillTime = 0
	}
}

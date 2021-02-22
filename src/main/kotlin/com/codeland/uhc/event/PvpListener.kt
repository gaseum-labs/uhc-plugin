package com.codeland.uhc.event

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.phase.phases.waiting.PvpData
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent

class PvpListener : Listener {
	@EventHandler
	fun onBowShoot(event: EntityShootBowEvent) {
		val projectile = event.projectile as? AbstractArrow

		if (projectile != null) {
			val player = projectile.shooter as? Player

			if (player != null) {
				PvpData.resetStillTimer(player)

				/* arrow spread reducer */
				projectile.location.direction = player.location.direction
				projectile.velocity = player.location.direction.multiply(projectile.velocity.length())
			}
		}
	}

	@EventHandler
	fun onStartMining(event: BlockDamageEvent) {
		PvpData.resetStillTimer(event.player)
	}

	@EventHandler
	fun onPlaceBlock(event: BlockPlaceEvent) {
		PvpData.resetStillTimer(event.player)
	}

	@EventHandler
	fun onDamage(event: EntityDamageEvent) {
		val player = event.entity
		if (player is Player) PvpData.resetStillTimer(player)
	}

	@EventHandler
	fun onUseItem(event: PlayerInteractEvent) {
		PvpData.resetStillTimer(event.player)
	}
}

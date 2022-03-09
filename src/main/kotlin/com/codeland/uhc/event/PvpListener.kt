package com.codeland.uhc.event

import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent

class PvpListener : Listener {
	@EventHandler
	fun onBowShoot(event: EntityShootBowEvent) {
		val projectile = event.projectile as? AbstractArrow

		if (projectile != null) {
			projectile.isCritical = false

			val player = projectile.shooter as? Player

			if (player != null) {
				/* arrow spread reducer */
				projectile.location.direction = player.location.direction
				projectile.velocity = player.location.direction.multiply(projectile.velocity.length())
			}
		}
	}
}

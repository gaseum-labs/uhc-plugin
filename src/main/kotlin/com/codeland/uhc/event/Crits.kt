package com.codeland.uhc.event

import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent

class Crits : Listener {
	@EventHandler
	fun onBowShoot(event: EntityShootBowEvent) {
		val arrow = event.projectile as? AbstractArrow

		if (arrow != null) arrow.isCritical = false
	}
}
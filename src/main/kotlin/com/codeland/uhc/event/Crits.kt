package com.codeland.uhc.event

import org.bukkit.entity.Arrow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent

class Crits : Listener {
	@EventHandler
	fun onBowShoot(event: EntityShootBowEvent) {
		if (event.projectile is Arrow) (event.projectile as Arrow).isCritical = false
	}
}
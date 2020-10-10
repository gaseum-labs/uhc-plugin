package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent

class Crits : Listener {
	@EventHandler
	fun onBowShoot(event: EntityShootBowEvent) {
		if (event.projectile is Arrow) (event.projectile as Arrow).isCritical = false
	}

	@EventHandler
	fun onShieldDisable(event: EntityDamageByEntityEvent) {
		val victim = if (event.entity is Player) event.entity as Player else return
		val damager = if (event.damager is Player) event.damager as Player else return
		if (victim.shieldBlockingDelay == 0 && !damager.isSprinting) {
			SchedulerUtil.nextTick {
				// re-enable their shield just in case the 25% hit
				victim.shieldBlockingDelay = 0
			}
		}
	}
}
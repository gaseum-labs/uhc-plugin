package com.codeland.uhc.event

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent
import org.bukkit.entity.*
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Snowball : Listener {

	@EventHandler
	fun snowballCollision(event: ProjectileCollideEvent): Unit {
		if ((event.entity is Snowball || event.entity is Egg) && event.collidedWith is Player) {
			val v = event.collidedWith.velocity
			val yVelocity =
				if (event
						.collidedWith.location
						.subtract(0.0, 0.01, 0.0)
						.block.type
						.isAir
				) 0.3 // if they're in the air
				else
					0.5
			event.collidedWith.velocity = event.entity.velocity.multiply(0.3).setY(yVelocity)
			(event.collidedWith as Damageable).damage(0.001)
		}
	}
}
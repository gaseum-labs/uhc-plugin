package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.extensions.VectorExtensions.times
import org.gaseumlabs.uhc.util.extensions.VectorExtensions.plus
import org.bukkit.*
import org.bukkit.entity.*

class LowGravity(type: QuirkType, game: Game) : Quirk(type, game) {
	companion object {
		var taskId: Int = 0
		var gravityModifier: Double = 0.5
	}

	init {
		taskId = SchedulerUtil.everyTick {
			PlayerData.playerDataList.keys.filter(PlayerData::isAlive).mapNotNull(Bukkit::getPlayer).forEach { player ->
				val typeUnder = player.world.getBlockAt(player.location.subtract(0.0, 0.01, 0.0)).type

				if (!typeUnder.isSolid && typeUnder != Material.WATER) {
					// G = 0.08 for living entities
					// https://minecraft.gamepedia.com/Entity#Motion_of_entities
					player.velocity.y += (1 - gravityModifier) * 0.08
					// give them a little push in the direction they're facing
					// to counteract air resistance (if they're sprinting)
					if (player.isSprinting) {
						val direction = player.location.direction
						player.velocity += direction.setY(0.0) * 0.03
					}
				}
			}
			game.world.entities.filter { e -> e !is Player }.forEach { entity ->
				val typeUnder = entity.world.getBlockAt(entity.location.subtract(0.0, 0.01, 0.0)).type

				if (!typeUnder.isSolid && typeUnder != Material.WATER) {
					val normalGravity = when (entity) {
						is Arrow -> 0.05
						is Trident -> 0.05
						is FishHook -> 0.03
						is Projectile -> 0.03
						is Item -> 0.04
						is FallingBlock -> 0.04
						is TNTPrimed -> 0.04
						is Boat -> 0.04
						is Chicken -> 0.04
						else -> 0.08
					}
					entity.velocity.y += (1 - gravityModifier) * normalGravity
				}
			}
		}
	}

	override fun customDestroy() {
		Bukkit.getScheduler().cancelTask(taskId)
	}
}
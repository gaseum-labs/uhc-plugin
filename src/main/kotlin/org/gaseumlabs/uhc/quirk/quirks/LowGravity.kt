package org.gaseumlabs.uhc.quirk.quirks

import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.quirk.Quirk
import org.gaseumlabs.uhc.quirk.QuirkType
import org.bukkit.*
import org.bukkit.Bukkit.getWorlds
import org.bukkit.entity.*
import org.bukkit.util.Vector

class LowGravity(type: QuirkType, game: Game) : Quirk(type, game) {
	companion object {
		var taskId: Int = 0
		var gravity: Double = 0.5
	}

	init {
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(org.gaseumlabs.uhc.UHCPlugin.plugin, {
			for (player in Bukkit.getOnlinePlayers()) {
				val block = player.world.getBlockAt(player.location.subtract(0.0, 0.01, 0.0)).type
				if (!block.isSolid && block != Material.WATER && player.gameMode == GameMode.SURVIVAL) {
					// - G = 0.08 for living entities
					// https://minecraft.gamepedia.com/Entity#Motion_of_entities
					player.velocity =
						Vector(player.velocity.x, player.velocity.y - gravity * 0.08 + 0.08, player.velocity.z)
					// - also give them a little push in the direction they're facing
					// to counteract air resistance (if they're sprinting)
					if (player.isSprinting) {
						val direction = player.location.direction
						player.velocity = Vector(player.velocity.x + direction.x * 0.03,
							player.velocity.y,
							player.velocity.z + direction.z * 0.03)
					}
				}
			}
			getWorlds()[0].entities.filter { e -> e !is Player }.forEach { entity ->
				val block = entity.world.getBlockAt(entity.location.subtract(0.0, 0.01, 0.0)).type

				if (!block.isSolid && block != Material.WATER) {
					val normalGravity = when (entity) {
						is Arrow -> 0.05
						is Projectile -> 0.03
						is Item -> 0.04
						is FallingBlock -> 0.04
						is TNTPrimed -> 0.04
						is Boat -> 0.04
						is Chicken -> 0.04
						else -> 0.08
					}
					entity.velocity = Vector(entity.velocity.x,
						entity.velocity.y - gravity * normalGravity + normalGravity,
						entity.velocity.z)
				}
			}
		}, 1, 1)
	}

	override fun customDestroy() {
		Bukkit.getScheduler().cancelTask(taskId)
	}
}
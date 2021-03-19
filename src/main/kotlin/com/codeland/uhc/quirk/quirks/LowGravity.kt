package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.Bukkit.getWorlds
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector


class LowGravity(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	companion object {
		var taskId: Int = 0
		var gravity: Double = 0.5
	}

	override fun onEnable() {
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			for (player in Bukkit.getOnlinePlayers()) {
				val block = player.world.getBlockAt(player.location.subtract(0.0, 0.01, 0.0)).type
				if (!block.isSolid && block != Material.WATER && player.gameMode == GameMode.SURVIVAL) {
					// - G = 0.08 for living entities
					// https://minecraft.gamepedia.com/Entity#Motion_of_entities
					player.velocity = Vector(player.velocity.x, player.velocity.y - gravity * 0.08 + 0.08, player.velocity.z)
					// - also give them a little push in the direction they're facing
					// to counteract air resistance (if they're sprinting)
					if (player.isSprinting) {
						val direction = player.location.direction
						player.velocity = Vector(player.velocity.x + direction.x * 0.03, player.velocity.y, player.velocity.z + direction.z * 0.03)
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
					entity.velocity = Vector(entity.velocity.x, entity.velocity.y - gravity * normalGravity + normalGravity, entity.velocity.z)
				}
			}
		}, 1, 1)
	}

	override fun onDisable() {
		Bukkit.getScheduler().cancelTask(taskId)
	}

	override val representation: ItemStack
		get() = ItemStack(Material.CHORUS_FRUIT)
}
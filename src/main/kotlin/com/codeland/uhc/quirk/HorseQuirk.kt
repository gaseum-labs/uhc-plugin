package com.codeland.uhc.quirk

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.event.Chat
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Horse
import org.bukkit.inventory.ItemStack
import java.util.*

class HorseQuirk(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
	companion object {
		var horseMap: MutableMap<UUID, UUID> = mutableMapOf()

		var taskID = 0
	}

	override fun onStart(uuid: UUID) {
		val location = GameRunner.getPlayerLocation(uuid) ?: return

		horseMap = horseMap.filter { (horseUUID, playerUUID) ->
			if (playerUUID == uuid) {
				Bukkit.getEntity(horseUUID)?.remove()
				false
			} else {
				true
			}
		} as MutableMap<UUID, UUID>

		val horse = location.world.spawnEntity(location, EntityType.HORSE) as Horse
		horseMap[horse.uniqueId] = uuid

		horse.fallDistance = 0.0f
		horse.inventory.saddle = ItemStack(Material.SADDLE)
		horse.isTamed = true
		horse.jumpStrength = 1.0
		horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.3375
		horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0

		GameRunner.playerAction(uuid) { player ->
			horse.owner = player
			horse.addPassenger(player)
		}

		for (x in -1..1) for (z in -1..1) for (y in 0..2) {
			location.block.getRelative(x, y, z).setType(Material.AIR, true)
		}
	}

	override fun onEnable() {
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			horseMap.forEach { (horseUuid, playerUuid) ->
				val horse = Bukkit.getEntity(horseUuid)
				val player = Bukkit.getPlayer(playerUuid)

				if (horse != null && horse.passengers.isEmpty() && player != null) horse.addPassenger(player)
			}
		}, 0, 20)
	}

	override fun onDisable() {
		Bukkit.getScheduler().cancelTask(taskID)
	}

	override val representation: ItemStack
		get() = ItemStack(Material.SADDLE)
}
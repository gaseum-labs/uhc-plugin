package com.codeland.uhc.quirk

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.event.Chat
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Horse
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class HorseQuirk(type: QuirkType) : Quirk(type) {
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

			player.addPotionEffect(PotionEffect(PotionEffectType.CONDUIT_POWER, Int.MAX_VALUE, 30, false, false, false))
		}

		for (x in -1..1) for (z in -1..1) for (y in 0..2) {
			location.block.getRelative(x, y, z).setType(Material.AIR, true)
		}
	}

	override fun onEnd(uuid: UUID) {
		GameRunner.playerAction(uuid) { player ->
			player.removePotionEffect(PotionEffectType.CONDUIT_POWER)
		}
	}

	override fun onEnable() {
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			horseMap.forEach { (horseUuid, playerUuid) ->
				val horse = Bukkit.getEntity(horseUuid) as Horse?
				val player = Bukkit.getPlayer(playerUuid)

				if (horse != null && player != null) {
					if (horse.passengers.isEmpty()) horse.addPassenger(player)

					/* armor items do not have attributes apparently */
					//val (armor, toughness) = player.inventory.armorContents.filterNotNull().fold(Pair(0.0, 0.0)) { (armor, toughness), itemStack ->
					//	Pair(
					//		armor + (itemStack.itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR)?.firstOrNull()?.amount ?: 0.0),
					//		toughness + (itemStack.itemMeta.getAttributeModifiers(Attribute.GENERIC_ARMOR_TOUGHNESS)?.firstOrNull()?.amount ?: 0.0)
					//	)
					//}
					//
					//horse.getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = armor
					//horse.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)?.baseValue = toughness
				}
			}
		}, 0, 20)
	}

	override fun onDisable() {
		Bukkit.getScheduler().cancelTask(taskID)
	}

	override val representation = ItemCreator.fromType(Material.SADDLE)
}
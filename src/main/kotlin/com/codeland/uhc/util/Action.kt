package com.codeland.uhc.util

import com.codeland.uhc.core.PlayerData
import org.bukkit.Bukkit
import org.bukkit.ChatColor.BOLD
import org.bukkit.ChatColor.GOLD
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import java.util.*

object Action {
	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage("$GOLD$BOLD$message")
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage("$GOLD$BOLD$message")
	}

	fun playerAction(uuid: UUID, action: (Player) -> Unit) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) PlayerData.getPlayerData(uuid).actionsQueue.add(action)
		else action(onlinePlayer)
	}

	fun teleportPlayer(uuid: UUID, location: Location) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)

			val zombie = playerData.offlineZombie
			if (zombie == null)
				playerData.offlineZombie = playerData.createDefaultZombie(uuid, location)
			else
				zombie.teleport(location)

		} else {
			onlinePlayer.teleport(location)
		}
	}

	fun potionEffectPlayer(uuid: UUID, effect: PotionEffect) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)
			playerData.offlineZombie?.addPotionEffect(effect)

		} else {
			onlinePlayer.addPotionEffect(effect)
		}
	}

	fun damagePlayer(uuid: UUID, damage: Double, source: Entity? = null) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)
			playerData.offlineZombie?.damage(damage, source)

		} else {
			onlinePlayer.damage(damage, source)
		}
	}

	fun getPlayerLocation(uuid: UUID): Location? {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		return if (onlinePlayer == null) {
			val playerData = PlayerData.getPlayerData(uuid)
			playerData.offlineZombie?.location

		} else {
			onlinePlayer.location
		}
	}

	fun setPlayerRiding(uuid: UUID, entity: Entity) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val zombie = PlayerData.getPlayerData(uuid).offlineZombie
			if (zombie != null) entity.addPassenger(zombie)

		} else {
			entity.addPassenger(onlinePlayer)
		}
	}
}

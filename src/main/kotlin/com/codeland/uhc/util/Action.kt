package com.codeland.uhc.util

import com.codeland.uhc.core.PlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration.*
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import java.util.*

object Action {
	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage(Component.text(message, GOLD, BOLD))
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage(Component.text(message, GOLD, BOLD))
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

	fun playerInventory(uuid: UUID): Array<ItemStack?>? {
		val player = Bukkit.getPlayer(uuid)

		return if (player == null) {
			PlayerData.getPlayerData(uuid).getZombieInventory()
		} else {
			player.inventory.contents
		}
	}

	fun awardAdvancement(player: Player, name: String) {
		val advancement = Bukkit.getServer().getAdvancement(NamespacedKey.minecraft(name)) ?: return
		val progress = player.getAdvancementProgress(advancement)
		progress.remainingCriteria.forEach { progress.awardCriteria(it) }
	}
}

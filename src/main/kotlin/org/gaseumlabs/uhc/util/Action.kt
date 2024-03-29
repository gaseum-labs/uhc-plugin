package org.gaseumlabs.uhc.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.TextDecoration.BOLD
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.core.OfflineZombie
import org.gaseumlabs.uhc.core.PlayerData
import java.util.*

object Action {
	fun sendGameMessage(player: Player, message: String) {
		player.sendMessage(Component.text(message, GOLD, BOLD))
	}

	fun sendGameMessage(sender: CommandSender, message: String) {
		sender.sendMessage(Component.text(message, GOLD, BOLD))
	}

	fun messageOrError(player: Player, message: String, error: Boolean) {
		if (error)
			Commands.errorMessage(player, message)
		else
			sendGameMessage(player, message)
	}

	fun playerAction(uuid: UUID, action: (Player) -> Unit) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) PlayerData.get(uuid).actionsQueue.add(action)
		else action(onlinePlayer)
	}

	fun teleportPlayer(uuid: UUID, location: Location) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.get(uuid)

			val zombie = playerData.offlineZombie
			if (zombie == null)
				playerData.offlineZombie = OfflineZombie.createDefaultZombie(uuid, location)
			else
				zombie.teleport(location)

		} else {
			onlinePlayer.teleport(location)
		}
	}

	fun potionEffectPlayer(uuid: UUID, effect: PotionEffect) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.get(uuid)
			playerData.offlineZombie?.addPotionEffect(effect)

		} else {
			onlinePlayer.addPotionEffect(effect)
		}
	}

	fun playerGameMode (uuid: UUID, gameMode: GameMode) {
		val onlinePlayer = Bukkit.getPlayer(uuid)

		if (onlinePlayer == null) {
			val playerData = PlayerData.get(uuid)
			playerData.shouldGameMode = gameMode
		} else {
			onlinePlayer.gameMode = gameMode
		}
	}

	fun damagePlayer(playerData: PlayerData, amount: Double) =
		(Bukkit.getPlayer(playerData.uuid) ?: playerData.offlineZombie)?.damage(amount)

	fun getPlayerLocation(uuid: UUID) = Bukkit.getPlayer(uuid)?.location
		?: PlayerData.get(uuid).offlineZombie?.location

	fun playerInventory(uuid: UUID): Array<ItemStack?>? = Bukkit.getPlayer(uuid)?.inventory?.contents
		?: PlayerData.get(uuid).offlineZombie?.let {
			OfflineZombie.getZombieData(it)?.inventory
		}

	fun playerHealth(uuid: UUID) = Bukkit.getPlayer(uuid)?.health ?: PlayerData.get(uuid).offlineZombie?.health

	fun awardAdvancement(player: Player, name: String) {
		val advancement = Bukkit.getServer().getAdvancement(NamespacedKey.minecraft(name)) ?: return
		val progress = player.getAdvancementProgress(advancement)
		progress.remainingCriteria.forEach { progress.awardCriteria(it) }
	}
}

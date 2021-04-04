package com.codeland.uhc.team

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.PlayerData
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

object NameManager {
	fun updateName(player: Player) {
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		playerData.setSkull(player)

		while (playerData.actionsQueue.isNotEmpty()) playerData.actionsQueue.remove()(player)

		playerData.replaceZombieWithPlayer(player)

		/* refresh the entity for the updated player for each other player */
		Bukkit.getOnlinePlayers().filter { it != player }.forEach { onlineplayer ->
			onlineplayer as CraftPlayer
			player as CraftPlayer

			Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
				onlineplayer.handle.playerConnection.sendPacket(
					PacketPlayOutEntityDestroy(player.entityId)
				)

				onlineplayer.handle.playerConnection.sendPacket(
					PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle)
				)

				onlineplayer.handle.playerConnection.sendPacket(
					PacketPlayOutNamedEntitySpawn(player.handle)
				)
			}
		}

		/* do NOT delete the player's entity for the player, only refresh name */
		(player as CraftPlayer).handle.playerConnection.sendPacket(
			PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle)
		)
	}
}

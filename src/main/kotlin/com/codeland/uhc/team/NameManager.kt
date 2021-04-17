package com.codeland.uhc.team

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.util.Util
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player

object NameManager {
	fun updateName(player: Player) {
		player as CraftPlayer
		val playerData = PlayerData.getPlayerData(player.uniqueId)

		playerData.setSkull(player)

		while (playerData.actionsQueue.isNotEmpty()) playerData.actionsQueue.remove()(player)

		playerData.replaceZombieWithPlayer(player)

		/* team name updating */

		/* refresh the entity for the updated player for each other player */
		Bukkit.getOnlinePlayers().filter { it != player }.forEach { onlinePlayer ->
			onlinePlayer as CraftPlayer

			onlinePlayer.handle.playerConnection.sendPacket(
				PacketPlayOutEntityDestroy(player.entityId)
			)

			onlinePlayer.handle.playerConnection.sendPacket(
				PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle)
			)

			onlinePlayer.handle.playerConnection.sendPacket(
				PacketPlayOutNamedEntitySpawn(player.handle)
			)

			Bukkit.getScheduler().scheduleSyncDelayedTask(UHCPlugin.plugin) {
				val dataWatcher = DataWatcher(player.handle)
				dataWatcher.register(DataWatcherObject(0, DataWatcherRegistry.a), 0x00)
				dataWatcher.register(DataWatcherObject(16, DataWatcherRegistry.a), 0xff.toByte())

				onlinePlayer.handle.playerConnection.sendPacket(
					PacketPlayOutEntityMetadata(player.entityId, dataWatcher, true)
				)

				/* remove glowing from other players who are no longer teammates */
				val otherPlayerGlowUpdater = DataWatcher(onlinePlayer.handle)
				otherPlayerGlowUpdater.register(DataWatcherObject(0, DataWatcherRegistry.a), 0x00)

				player.handle.playerConnection.sendPacket(
					PacketPlayOutEntityMetadata(onlinePlayer.entityId, otherPlayerGlowUpdater, true)
				)

				/* update team glow color for all other players to player */
				player.handle.playerConnection.sendPacket(
					PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, onlinePlayer.handle)
				)
			}
		}

		/* do NOT delete the player's entity for the player, only refresh name */
		player.handle.playerConnection.sendPacket(
			PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle)
		)
	}
}

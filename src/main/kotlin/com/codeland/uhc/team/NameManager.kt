package com.codeland.uhc.team

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.event.Packet
import com.codeland.uhc.event.Packet.metadataPacketDefaultState
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Player

object NameManager {
	fun updateName(player: Player) {
		player as CraftPlayer

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val team = TeamData.playersTeam(player.uniqueId)
		val newName = Packet.playersNewName(player.uniqueId)

		playerData.setSkull(player)

		while (playerData.actionsQueue.isNotEmpty()) playerData.actionsQueue.remove()(player)

		playerData.replaceZombieWithPlayer(player)

		/* team name updating */

		/* refresh the entity for the updated player for each other player */
		Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
			onlinePlayer as CraftPlayer

			/* tell other players this player's name & update glowing */
			Packet.updateTeamColor(player, team, newName, onlinePlayer)
			onlinePlayer.handle.playerConnection.sendPacket(metadataPacketDefaultState(player))

			/* tell this player about other players' names & update glowing */
			if (player != onlinePlayer) {
				Packet.updateTeamColor(onlinePlayer, TeamData.playersTeam(onlinePlayer.uniqueId), Packet.playersNewName(onlinePlayer.uniqueId), player)
				player.handle.playerConnection.sendPacket(metadataPacketDefaultState(onlinePlayer))
			}
		}
	}


}

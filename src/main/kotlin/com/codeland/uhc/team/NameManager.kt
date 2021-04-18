package com.codeland.uhc.team

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.event.Packet
import net.minecraft.server.v1_16_R3.*
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
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

	val entriesField = DataWatcher::class.java.getDeclaredField("entries")
	init { entriesField.isAccessible = true }

	/**
	 *  creates a metadata packet for the specified player
	 *  that only contains the first byte field
	 */
	private fun metadataPacketDefaultState(player: Player): PacketPlayOutEntityMetadata {
		player as CraftPlayer

		val dataWatcher = DataWatcher(player.handle)
		dataWatcher.register(DataWatcherObject(0, DataWatcherRegistry.a),
			((entriesField[player.handle.dataWatcher] as Int2ObjectOpenHashMap<DataWatcher.Item<Any>>)[0] as DataWatcher.Item<Byte>).b()
		)

		return PacketPlayOutEntityMetadata(player.entityId, dataWatcher, true)
	}
}

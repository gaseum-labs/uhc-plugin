package com.codeland.uhc.team

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.event.Packet
import com.codeland.uhc.event.Packet.metadataPacketDefaultState
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore
import net.minecraft.server.v1_16_R3.ScoreboardServer
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftScoreboard
import org.bukkit.entity.Player
import kotlin.math.ceil

object NameManager {
	fun updateName(player: Player) {
		player as CraftPlayer

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val team = TeamData.playersTeam(player.uniqueId)
		val newName = Packet.playersNewName(player.uniqueId)

		playerData.setSkull(player)

		while (playerData.actionsQueue.isNotEmpty()) playerData.actionsQueue.remove()(player)

		playerData.replaceZombieWithPlayer(player)

		/* add to hearts objective */
		val playerHealthScore = ceil(player.health).toInt()
		val scoreboard = (Bukkit.getScoreboardManager().mainScoreboard as CraftScoreboard).handle
		scoreboard.getPlayerScoreForObjective(player.name, scoreboard.getObjective(GameRunner.heartsObjective.name)).score = playerHealthScore

		/* team name updating */

		/* refresh the entity for the updated player for each other player */
		Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
			onlinePlayer as CraftPlayer

			/* tell other players this player's name & update glowing */
			Packet.updateTeamColor(player, team, newName, onlinePlayer)
			onlinePlayer.handle.playerConnection.sendPacket(metadataPacketDefaultState(player))

			/* send heart packet on joining */
			onlinePlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, GameRunner.heartsObjective.name, player.name, playerHealthScore))

			/* tell this player about other players' names & update glowing */
			if (player != onlinePlayer) {
				Packet.updateTeamColor(onlinePlayer, TeamData.playersTeam(onlinePlayer.uniqueId), Packet.playersNewName(onlinePlayer.uniqueId), player)
				player.handle.playerConnection.sendPacket(metadataPacketDefaultState(onlinePlayer))
			}
		}
	}


}

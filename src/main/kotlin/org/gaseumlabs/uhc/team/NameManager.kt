package org.gaseumlabs.uhc.team

import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.event.Packet
import org.gaseumlabs.uhc.event.Packet.metadataPacketDefaultState
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_18_R2.scoreboard.CraftScoreboard
import org.bukkit.entity.Player
import kotlin.math.ceil

object NameManager {
	fun updateName(player: Player, team: AbstractTeam?) {
		player as CraftPlayer

		val playerData = PlayerData.getPlayerData(player.uniqueId)
		val newName = Packet.playersNewName(player.uniqueId)

		playerData.setSkull(player)

		while (playerData.actionsQueue.isNotEmpty()) playerData.actionsQueue.remove()(player)

		playerData.replaceZombieWithPlayer(player)

		UHCBar.addBossBar(player)

		/* add to hearts objective */
		val playerHealthScore = ceil(player.health).toInt()
		val scoreboard = (Bukkit.getScoreboardManager().mainScoreboard as CraftScoreboard).handle

		val nmsObjective = scoreboard.getObjective(UHC.heartsObjective.name)!!
		scoreboard.getPlayerScores(player.name)[nmsObjective]?.score = playerHealthScore

		/* team name updating */

		/* refresh the entity for the updated player for each other player */
		Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
			onlinePlayer as CraftPlayer

			/* tell other players this player's name & update glowing */
			Packet.updateTeamColor(player, team, newName, onlinePlayer)
			onlinePlayer.handle.connection.send(metadataPacketDefaultState(player))

			/* send heart packet on joining */
			onlinePlayer.handle.connection.send(ClientboundSetScorePacket(
				ServerScoreboard.Method.CHANGE,
				UHC.heartsObjective.name,
				player.name,
				playerHealthScore
			))

			/* tell this player about other players' names & update glowing */
			if (player != onlinePlayer) {
				Packet.updateTeamColor(onlinePlayer,
					UHC.getTeams().playersTeam(onlinePlayer.uniqueId),
					Packet.playersNewName(onlinePlayer.uniqueId),
					player)
				player.handle.connection.send(metadataPacketDefaultState(onlinePlayer))
			}
		}
	}

}

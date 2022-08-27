package org.gaseumlabs.uhc.team

import org.gaseumlabs.uhc.core.*
import org.gaseumlabs.uhc.event.Packet
import org.gaseumlabs.uhc.event.Packet.playersMetadataPacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_18_R2.scoreboard.CraftScoreboard
import org.bukkit.entity.Player
import kotlin.math.ceil

object NameManager {
	fun onPlayerLogin(updatePlayer: Player, team: AbstractTeam?) {
		updatePlayer as CraftPlayer

		val playerData = PlayerData.get(updatePlayer.uniqueId)

		/* 1. misc login actions */
		playerData.setSkull(updatePlayer)
		while (playerData.actionsQueue.isNotEmpty()) playerData.actionsQueue.remove()(updatePlayer)
		OfflineZombie.replaceZombieWithPlayer(updatePlayer)
		UHCBar.addBossBar(updatePlayer)

		/* 2. add to hearts objective */
		val playerHealthScore = ceil(updatePlayer.health).toInt()
		val scoreboard = (Bukkit.getScoreboardManager().mainScoreboard as CraftScoreboard).handle
		val nmsObjective = scoreboard.getObjective(UHC.heartsObjective.name)!!
		scoreboard.getPlayerScores(updatePlayer.name)[nmsObjective]?.score = playerHealthScore

		/* 3. update nominal teams */
		updateNominalTeams(updatePlayer, team, true)

		/* 4. try to get player's link */
		UHC.dataManager.linkData.playersIndividualLink(updatePlayer.uniqueId)
	}

	fun updateNominalTeams(updatePlayer: Player, team: AbstractTeam?, loggingIn: Boolean) {
		updatePlayer as CraftPlayer

		val updatePlayerIdName = Packet.playersIdName(updatePlayer.uniqueId)

		Bukkit.getOnlinePlayers().forEach { sendPlayer ->
			sendPlayer as CraftPlayer

			/* tell other players updatePlayer's name color */
			Packet.updateNominalTeamColor(sendPlayer, updatePlayer, updatePlayerIdName, team)
			/* tell other players to glow updatePlayer if applicable */
			sendPlayer.handle.connection.send(playersMetadataPacket(updatePlayer))

			/* tell other players about updatePlayer's health */
			sendPlayer.handle.connection.send(ClientboundSetScorePacket(
				ServerScoreboard.Method.CHANGE,
				UHC.heartsObjective.name,
				updatePlayer.name,
				ceil(updatePlayer.health).toInt()
			))

			/* UNO REVERSE: tell updatePlayer about other players */
			if (updatePlayer != sendPlayer) {
				/* only need to tell updatePlayer about other team colors if they are just logging in */
				if (loggingIn) Packet.updateNominalTeamColor(
					updatePlayer,
					sendPlayer,
					Packet.playersIdName(sendPlayer.uniqueId),
					UHC.getTeams().playersTeam(sendPlayer.uniqueId),
				)

				updatePlayer.handle.connection.send(playersMetadataPacket(sendPlayer))
			}
		}
	}
}

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

		(player as CraftPlayer).handle.playerConnection.sendPacket(
			PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle)
		)

		//val team = TeamData.playersTeam(player.uniqueId)
		//val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		//val fakeTeam = scoreboard.getTeam(player.name) ?: makeFakeTeam(player.name)

		//if (team == null) {
		//	player.setPlayerListName(null)
		//	updateTeam(fakeTeam, ColorPair(ChatColor.WHITE))
//
		//} else {
		//	player.setPlayerListName(team.colorPair.colorString(player.name))
		//	updateTeam(fakeTeam, team.colorPair)
		//}
	}

	fun makeFakeTeam(name: String): Team {
		val scoreboard = Bukkit.getScoreboardManager().mainScoreboard
		val team = scoreboard.registerNewTeam(name)
		team.addEntry(name)

		return team
	}

	fun updateTeam(team: Team, colorPair: ColorPair) {
		team.color = colorPair.color0

		if (colorPair.color0 == ChatColor.WHITE) {
			team.prefix = ""
			team.suffix = ""
		} else {
			team.prefix = "${colorPair.color0}■ "
			team.suffix = " ${colorPair.color1 ?: colorPair.color0}■"
		}
	}
}

package com.codeland.uhc.event

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.team.TeamData
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import net.minecraft.server.v1_16_R3.*
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import java.util.*
import kotlin.math.ceil

object Packet {
	val playerNames = arrayListOf<UUID>()

	fun intToName(int: Int): String {
		var countDown = int

		val name = CharArray(16)

		for (i in 0..7) {
			name[i * 2    ] = ChatColor.COLOR_CHAR
			name[i * 2 + 1] = ChatColor.values()[countDown % 10].char
			countDown /= 10
		}

		return String(name)
	}

	fun splitName(name: String): Pair<String, String> {
		val mid: Int = ceil(name.length / 2.0).toInt()
		return Pair(name.substring(0, mid), name.substring(mid))
	}

	fun init() {
		val protocolManager = ProtocolLibrary.getProtocolManager()

		protocolManager.addPacketListener(object : PacketAdapter(UHCPlugin.plugin, ListenerPriority.HIGH, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				val packet = event.packet

				if (packet.playerInfoAction.read(0) != EnumWrappers.PlayerInfoAction.ADD_PLAYER) return

				val sentPlayer = event.player as CraftPlayer

				packet.playerInfoDataLists.write(0, packet.playerInfoDataLists.read(0).map { playerInfoData ->
					val uuid = playerInfoData.profile.uuid
					val uhcTeam = TeamData.playersTeam(uuid)

					if (uhcTeam == null) {
						/* do not change info about the player */
						playerInfoData

					} else {
						var nameIndex = playerNames.indexOf(uuid)

						if (nameIndex == -1) {
							playerNames.add(uuid)
							nameIndex = playerNames.size - 1
						}

						val (oldNamePrefix, oldNameSuffix) = splitName(playerInfoData.profile.name)
						val newName = intToName(nameIndex)

						/* send team packet */
						val team = ScoreboardTeam(Scoreboard(), newName)
						team.color = EnumChatFormat.values()[uhcTeam.colorPair.color0.ordinal]
						team.prefix = ChatMessage("${uhcTeam.colorPair.color0}${oldNamePrefix}")
						team.suffix = ChatMessage("${uhcTeam.colorPair.color1 ?: uhcTeam.colorPair.color0}${oldNameSuffix}")
						team.playerNameSet.add(newName)

						/* remove team then re-send */
						sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(team, 1))
						sentPlayer.handle.playerConnection.sendPacket(PacketPlayOutScoreboardTeam(team, 0))
						/* ---------------- */

						val customProfile = playerInfoData.profile.withName(newName)

						PlayerInfoData(customProfile, playerInfoData.latency, playerInfoData.gameMode, playerInfoData.displayName)
					}
				})
			}
		})
	}
}

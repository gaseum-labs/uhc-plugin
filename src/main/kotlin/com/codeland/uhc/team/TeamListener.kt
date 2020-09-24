package com.codeland.uhc.team

import com.codeland.uhc.UHCPlugin
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.Bukkit

object TeamListener {
	var lastPacket: PacketContainer? = null

	fun teamListen() {
		ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(UHCPlugin.plugin, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				val list = event.packet.playerInfoDataLists.read(0)
				val newList = List(list.size) { i ->
					val oldInfo = list[i]

					val player = Bukkit.getPlayer(oldInfo.profile.uuid)

					if (player != null) {
						val team = TeamData.playersTeam(player)

						if (team != null) {
							PlayerInfoData(oldInfo.profile, oldInfo.latency, oldInfo.gameMode, WrappedChatComponent.fromText(team.colorPair.colorString(player.name)))
						} else {
							oldInfo
						}
					} else {
						oldInfo
					}
				}

				event.packet.playerInfoDataLists.write(0, newList)

				lastPacket = event.packet.deepClone()
			}
		})
	}
}
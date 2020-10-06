package com.codeland.uhc.team

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.Util
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.Bukkit
import java.util.*

object TeamListener {
	fun teamListen() {
		ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(UHCPlugin.plugin, PacketType.Play.Server.SCOREBOARD_TEAM) {
			override fun onPacketSending(event: PacketEvent) {

				//Util.log(event.packet.chatComponents.size().toString())
				//Util.log(event.packet.stringArrays.size().toString())
				//Util.log(event.packet.playerInfoDataLists.size().toString())
				//Util.log(event.packet.strings.size().toString())
				//Util.log(event.packet.gameProfiles.size().toString())

				/*val list = event.packet.playerInfoDataLists.read(0)
				val newList = List(list.size) { i ->
					val oldInfo = list[i]
					oldInfo.profile.properties.forEach { t, u ->
						Util.log("${t} - ${u.value}")
					}
					PlayerInfoData(oldInfo.profile.withName("__"), oldInfo.latency, oldInfo.gameMode, oldInfo.displayName)
				}

				event.packet.playerInfoDataLists.write(0, newList)*/
			}
		})
	}
}
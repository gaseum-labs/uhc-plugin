package com.codeland.uhc.team

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.util.Util
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLib
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListeningWhitelist
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.events.PacketListener
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.ChatColor
import org.bukkit.plugin.Plugin

object TeamListener {
	fun teamListen() {
		ProtocolLibrary.getProtocolManager().addPacketListener(object : PacketAdapter(UHCPlugin.plugin, PacketType.Play.Server.PLAYER_INFO) {
			override fun onPacketSending(event: PacketEvent) {
				val list = event.packet.playerInfoDataLists.read(0)
				val newList = List(list.size) { i ->
					val oldInfo = list[i]
					PlayerInfoData(oldInfo.profile.withName("ABOVE HEAD"), oldInfo.latency, oldInfo.gameMode, WrappedChatComponent.fromText("ON SCOREBOARD"))
				}

				event.packet.playerInfoDataLists.write(0, newList)
			}
		})
	}
}
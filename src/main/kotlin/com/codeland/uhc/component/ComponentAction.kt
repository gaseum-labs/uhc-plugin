package com.codeland.uhc.component

import net.minecraft.network.chat.ChatType.*
import net.minecraft.network.protocol.game.*
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player

object ComponentAction {
	fun Player.uhcTitle(
		title: UHCComponent,
		subTitle: UHCComponent,
		fadeIn: Int,
		sustain: Int,
		fadeOut: Int,
	) {
		val connection = (this as CraftPlayer).handle.connection

		connection.send(ClientboundSetTitleTextPacket(title.complete()))
		connection.send(ClientboundSetSubtitleTextPacket(subTitle.complete()))
		connection.send(ClientboundSetTitlesAnimationPacket(fadeIn, sustain, fadeOut))
	}

	fun Player.uhcMessage(
		message: UHCComponent,
	) {
		val connection = (this as CraftPlayer).handle.connection

		connection.send(ClientboundChatPacket(message.complete(), CHAT, null))
	}

	fun Player.uhcHotbar(
		message: UHCComponent,
	) {
		val connection = (this as CraftPlayer).handle.connection

		connection.send(ClientboundChatPacket(message.complete(), GAME_INFO, null))
	}
}
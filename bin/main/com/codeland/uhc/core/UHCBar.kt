package com.codeland.uhc.core

import net.minecraft.network.chat.ChatComponentText
import net.minecraft.network.protocol.game.PacketPlayOutBoss
import net.minecraft.world.BossBattle
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player

object UHCBar {
	fun addBossBar(player: Player) {
		(player as CraftPlayer).handle.b.sendPacket(
			PacketPlayOutBoss.createAddPacket(
				object : BossBattle(
					player.uniqueId, ChatComponentText(""), BarColor.a, BarStyle.a
				) {}
			)
		)
	}

	fun updateBossBar(
		player: Player,
		name: String,
		progress: Float,
		barColor: BossBattle.BarColor,
	) {
		player as CraftPlayer

		val bossBar = object : BossBattle(
			player.uniqueId, ChatComponentText(name),
			barColor, BarStyle.a
		) {}
		bossBar.progress = progress

		player.handle.b.sendPacket(PacketPlayOutBoss.createUpdateNamePacket(bossBar))
		player.handle.b.sendPacket(PacketPlayOutBoss.createUpdateProgressPacket(bossBar))
		player.handle.b.sendPacket(PacketPlayOutBoss.createUpdateStylePacket(bossBar))
	}
}

package com.codeland.uhc.core

import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.minecraft.network.chat.ChatComponentText
import net.minecraft.network.protocol.game.PacketPlayOutBoss
import net.minecraft.world.BossBattle
import org.bukkit.ChatColor
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

	fun updateBar(player: Player) {
		val arena = ArenaManager.playersArena(player.uniqueId)

		when {
			arena is PvpArena -> {
				updateBossBar(
					player,
					if (arena.isOver()) {
						"${ChatColor.RED}Game Over"
					} else {
						"${ChatColor.RED}${PvpArena.typeName(arena.matchType)} PVP" +
							if (arena.startTime >= 0) {
								" | " + if (arena.shouldGlow()) {
									"${ChatColor.GOLD}Glowing"
								} else {
									"Glowing in ${Util.timeString(arena.glowTimer)}"
								}
							} else {
								""
							}
					},
					if (arena.isOver() || arena.glowPeriod == 0 || arena.glowTimer <= 0) {
						1.0f
					} else if (arena.startTime < 0) {
						0.0f
					} else {
						1.0f - (arena.glowTimer.toFloat() / arena.glowPeriod)
					},
					BossBattle.BarColor.c
				)
			}
			arena is ParkourArena -> {
				updateBossBar(
					player,
					"Parkour",
					1.0f,
					BossBattle.BarColor.g
				)
			}
			player.world.name == WorldManager.LOBBY_WORLD_NAME -> {
				val phase = UHC.game?.phase
				val phaseType = phase?.phaseType

				updateBossBar(
					player,
					"${ChatColor.WHITE}Waiting Lobby" +
						if (phaseType != null) {
							" | ${phaseType.chatColor}Game Ongoing: ${phaseType.prettyName}"
						} else {
							""
						},
					phase?.updateBarLength(phase.remainingTicks) ?: 1.0f,
					BossBattle.BarColor.g
				)
			}
			else -> {
				val phase = UHC.game?.phase
				if (phase != null) updateBossBar(
					player,
					phase.updateBarTitle(player.world, phase.remainingSeconds()),
					phase.updateBarLength(phase.remainingTicks),
					phase.phaseType.barColor
				)
			}
		}
	}
}

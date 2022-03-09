package com.codeland.uhc.core

import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.minecraft.network.chat.TextComponent
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarColor.*
import net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player

object UHCBar {
	fun addBossBar(player: Player) {
		(player as CraftPlayer).handle.connection.send(
			ClientboundBossEventPacket.createAddPacket(
				UHCBossEvent(player.uniqueId, TextComponent(""), WHITE, PROGRESS)
			)
		)
	}

	fun updateBossBar(
		player: Player,
		name: String,
		progress: Float,
		barColor: BossBarColor,
	) {
		val connection = (player as CraftPlayer).handle.connection

		val bossBar = UHCBossEvent(player.uniqueId, TextComponent(name), barColor, PROGRESS)
		bossBar.progress = progress

		connection.send(ClientboundBossEventPacket.createUpdateNamePacket(bossBar))
		connection.send(ClientboundBossEventPacket.createUpdateProgressPacket(bossBar))
		connection.send(ClientboundBossEventPacket.createUpdateStylePacket(bossBar))
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
					RED
				)
			}
			arena is ParkourArena -> {
				updateBossBar(
					player,
					"Parkour",
					1.0f,
					GREEN
				)
			}
			player.world === WorldManager.lobbyWorld -> {
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
					WHITE
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

package com.codeland.uhc.core

import com.codeland.uhc.component.UHCColor
import com.codeland.uhc.component.UHCColor.U_GOLD
import com.codeland.uhc.component.UHCColor.U_GREEN
import com.codeland.uhc.component.UHCColor.U_RED
import com.codeland.uhc.component.UHCColor.U_WHITE
import com.codeland.uhc.component.UHCComponent
import com.codeland.uhc.component.UHCComponent.Companion
import com.codeland.uhc.lobbyPvp.ArenaManager
import com.codeland.uhc.lobbyPvp.arena.ParkourArena
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import net.minecraft.network.chat.*
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer
import org.bukkit.entity.Player

object UHCBar {
	fun addBossBar(player: Player) {
		(player as CraftPlayer).handle.connection.send(
			ClientboundBossEventPacket.createAddPacket(
				UHCBossEvent(player.uniqueId, TextComponent(""), BossBarColor.WHITE, PROGRESS)
			)
		)
	}

	fun updateBossBar(
		player: Player,
		name: UHCComponent,
		progress: Float,
		barColor: BossBarColor,
	) {
		val connection = (player as CraftPlayer).handle.connection

		val bossBar = UHCBossEvent(player.uniqueId, name.complete() as TextComponent, barColor, PROGRESS)
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
					UHCComponent.text()
						.andSwitch(arena.isOver()) {
							Companion.text("Game Over", U_RED)
						}
						.andSwitch(true) {
							UHCComponent.text("${PvpArena.typeName(arena.matchType)} PVP", U_RED)
								.and(" | ", U_WHITE)
								.and(
									UHCComponent.text()
										.andSwitch(arena.startTime >= 0) { UHCComponent.text() }
										.andSwitch(arena.shouldGlow()) { UHCComponent.text("Glowing", U_GOLD) }
										.andSwitch(true) {
											UHCComponent.text("Glowing in ${Util.timeString(arena.glowTimer)}", U_RED)
										}
								)
						},
					if (arena.isOver() || arena.glowPeriod == 0 || arena.glowTimer <= 0) {
						1.0f
					} else if (arena.startTime < 0) {
						0.0f
					} else {
						1.0f - (arena.glowTimer.toFloat() / arena.glowPeriod)
					},
					BossBarColor.RED
				)
			}
			arena is ParkourArena -> {
				updateBossBar(
					player,
					UHCComponent.text("Parkour", U_GREEN),
					1.0f,
					BossBarColor.GREEN
				)
			}
			player.world === WorldManager.lobbyWorld -> {
				val phase = UHC.game?.phase
				val phaseType = phase?.phaseType

				updateBossBar(
					player,
					UHCComponent.text("Waiting Lobby", U_WHITE)
						.andIf(phaseType != null,
							UHCComponent.text(" | ", U_WHITE)
								.and("Game Ongoing: ${phaseType?.prettyName}", phaseType?.color ?: U_WHITE)
						),
					phase?.updateBarLength(phase.remainingTicks) ?: 1.0f,
					BossBarColor.WHITE
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

package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.UHCPhase
import com.destroystokyo.paper.utils.PaperPluginLogger
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.logging.Level

class WaitingEventListener() : Listener {

	private val gameRunner = GameRunner

	@EventHandler
	fun onPlayerHurt(e : EntityDamageEvent) {
		if (gameRunner.phase != UHCPhase.WAITING) {
			return
		}
		if (e.entityType == EntityType.PLAYER) {
			e.isCancelled = true
		}
	}

	@EventHandler
	fun onPlayerJoin(e : PlayerJoinEvent) {
		if (gameRunner.phase != UHCPhase.WAITING) {
			if (GameRunner.playersTeam(e.player.name) == null) {
				e.player.gameMode = GameMode.SPECTATOR
			}
			return
		}
		e.player.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, Int.MAX_VALUE, 0, false, false, false))
		e.player.gameMode = GameMode.ADVENTURE;
	}

	@EventHandler
	fun onWorldLoad(e : WorldLoadEvent) {
		e.world.setSpawnLocation(10000, 70, 10000)
		e.world.worldBorder.setCenter(10000.0, 10000.0)
		e.world.worldBorder.size = 50.0
		e.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
		e.world.setGameRule(GameRule.DO_MOB_SPAWNING, false)
		e.world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false) // could cause issue with dynamic spawn limit if true
		e.world.time = 1000
		e.world.difficulty = Difficulty.NORMAL
	}

	@EventHandler
	fun onPlayerDeath(e : PlayerDeathEvent) {
		e.entity.gameMode = GameMode.SPECTATOR
		GameRunner.playerDeath(e.entity)
	}

	@EventHandler
	fun onMessage(e : AsyncPlayerChatEvent) {
		if (GameRunner.phase != UHCPhase.WAITING) {
			if (!e.message.startsWith("!")) {
				val team = GameRunner.playersTeam(e.player.displayName)
				if (team != null) {
					e.isCancelled = true
					PaperPluginLogger.getGlobal().log(Level.INFO, "PLAYER SENT MESSAGE IN TEAM CHAT")
					for (entry in team.entries) {
						val precomp = TextComponent("<")
						val name = TextComponent(e.player.displayName)
						name.color = team.color.asBungee()
						name.isUnderlined = true
						val remaining = TextComponent("> ${e.message}")
						Bukkit.getPlayer(entry)?.sendMessage(precomp, name, remaining)
					}
				}
			} else {
				e.message = e.message.substring(1)
			}
		}
	}

	@EventHandler
	fun onPlayerTeleport(e : PlayerTeleportEvent) {
		if (!GameRunner.netherIsAllowed()) {
			if (e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && e.player.gameMode == GameMode.SURVIVAL) {
				e.isCancelled = true
			}
		}
	}
}
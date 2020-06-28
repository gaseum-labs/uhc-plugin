package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHCPhase
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.RenderType
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
			return
		}
		e.player.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, Int.MAX_VALUE, 1, false, false, false))
		e.player.gameMode = GameMode.ADVENTURE;
	}

	@EventHandler
	fun onWorldLoad(e : WorldLoadEvent) {
		e.world.setSpawnLocation(10000, 70, 10000)
		e.world.worldBorder.setCenter(10000.0, 10000.0)
		e.world.worldBorder.size = 50.0
		e.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		e.world.time = 1000
		PaperPluginLogger.getGlobal().log(Level.INFO, "Final monster limit is " + e.world.monsterSpawnLimit)
	}

	@EventHandler
	fun onPlayerDeath(e : PlayerDeathEvent) {
		e.entity.gameMode = GameMode.SPECTATOR
	}
}
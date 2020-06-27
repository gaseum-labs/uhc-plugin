package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHCPhase
import com.codeland.uhc.di
import com.destroystokyo.paper.utils.PaperPluginLogger
import org.bukkit.GameMode
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.scoreboard.Team
import org.kodein.di.instance
import java.util.logging.Level

class WaitingEventListener() : Listener {

    private val gameRunner: GameRunner by di.instance()

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
        e.player.gameMode = GameMode.ADVENTURE;
    }

    @EventHandler
    fun onWorldLoad(e : WorldLoadEvent) {
        PaperPluginLogger.getGlobal().log(Level.INFO, "WORLD TYPE: " + e.world.worldType)

        e.world.setSpawnLocation(10000, 70, 10000)
        e.world.worldBorder.setCenter(10000.0, 10000.0)
        e.world.worldBorder.size = 50.0
    }
}
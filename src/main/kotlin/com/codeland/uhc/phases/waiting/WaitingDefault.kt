package com.codeland.uhc.phases.waiting

import com.codeland.uhc.phases.Phase
import org.bukkit.*
import org.bukkit.entity.EntityType

class WaitingDefault : Phase() {

    override fun customStart() {
        Bukkit.getWorlds().forEach { world ->
            world.setSpawnLocation(10000, 70, 10000)
            world.worldBorder.setCenter(10000.0, 10000.0)
            world.worldBorder.size = 50.0
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false) // could cause issue with dynamic spawn limit if true
            world.time = 1000
            world.difficulty = Difficulty.NORMAL

            world.entities.forEach { entity ->
                if (entity.type != EntityType.PLAYER)
                    entity.remove()
            }
        }

        Bukkit.getServer().onlinePlayers.forEach { player ->
            player.exp = 0.0F
            player.health = 20.0
            player.teleport(Location(Bukkit.getWorlds()[0], 10000.0, 100.0, 10000.0))
            player.gameMode = GameMode.ADVENTURE
        }
    }

    override fun perSecond(remainingSeconds: Long) {

    }

    override fun getCountdownString(): String {
        return ""
    }

    override fun endPhrase(): String {
        return ""
    }
}

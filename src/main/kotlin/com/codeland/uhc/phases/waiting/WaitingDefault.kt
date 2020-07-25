package com.codeland.uhc.phases.waiting

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.Util
import com.codeland.uhc.gui.GuiOpener
import com.codeland.uhc.phases.Phase
import com.codeland.uhc.quirk.Pests
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

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
           onPlayerJoin(player)
        }
    }

    override fun perSecond(remainingSeconds: Int) {

    }

    override fun getCountdownString(): String {
        return ""
    }

    override fun endPhrase(): String {
        return ""
    }

    companion object {
        fun onPlayerJoin(player: Player) {
            player.exp = 0.0F
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
            player.health = 20.0
            player.foodLevel = 20
            player.teleport(Location(Bukkit.getWorlds()[0], 10000.5, Util.topBlockY(Bukkit.getWorlds()[0], 10000, 10000) + 1.0, 10000.5))
            player.gameMode = GameMode.ADVENTURE

            Pests.makeNotPest(player)

            /* get them on the health scoreboard */
            player.damage(1.0)

            val inventory = player.inventory
            inventory.clear()

            inventory.addItem(GuiOpener.createGuiOpener())
        }
    }
}

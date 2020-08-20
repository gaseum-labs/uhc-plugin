package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.HashMap


class LowGravity(type: QuirkType) : Quirk(type) {

    companion object {
        var taskId: Int = 0
        private const val GRAVITY: Double = 0.5
        // these are blocks that should be counted as 'air'
        private val airBlocks = listOf<Material>(
                AIR, CAVE_AIR, GRASS, TALL_GRASS, WHEAT_SEEDS,
                BEETROOT_SEEDS, MELON_SEEDS, PUMPKIN_SEEDS
        )
    }

    override fun onEnable() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(GameRunner.plugin, {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.world.getBlockAt(player.location.subtract(0.0, 0.01, 0.0)).type in airBlocks) {
                    // - G = 0.08 for living entities
                    // https://minecraft.gamepedia.com/Entity#Motion_of_entities
                    player.velocity = Vector(player.velocity.x, player.velocity.y - GRAVITY * 0.08 + 0.08, player.velocity.z)
                    // - also give them a little push in the direction they're facing
                    // to counteract air resistance (if they're sprinting)
                    if (player.isSprinting) {
                        val direction = player.location.direction
                        player.velocity = Vector(player.velocity.x + direction.x * 0.03, player.velocity.y, player.velocity.z + direction.z * 0.03)
                    }
                }
            }
        }, 1, 1)
    }

    override fun onDisable() {
        Bukkit.getScheduler().cancelTask(taskId)
    }
}
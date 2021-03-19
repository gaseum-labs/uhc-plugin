package com.codeland.uhc.event

import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent

class ClassesEvents : Listener {

    private fun surface(block: Block): Boolean {
        return block.getRelative(0, 1, 0).type.isAir
                || block.getRelative(1, 0, 0).type.isAir
                || block.getRelative(0, 0, 1).type.isAir
                || block.getRelative(0, -1, 0).type.isAir
                || block.getRelative(-1, 0, 0).type.isAir
                || block.getRelative(0, 0, -1).type.isAir
    }

    @EventHandler
    fun playerMove(event: PlayerMoveEvent) {
        val player = event.player

        when (Classes.getClass(player.uniqueId)) {
            QuirkClass.LAVACASTER -> {
                for (dx in -4..4) for (dy in -3..-1) for (dz in -4..4) {
                    val block = player.location.block.getRelative(dx, dy, dz)
                    if (block.type === Material.LAVA && surface(block)) {
                        block.setType(Material.OBSIDIAN, false)
                        Classes.obsidianifiedLava.add(Classes.ObsidianifiedLava(block, false))
                    }
                }
            }
            QuirkClass.DIVER -> {
                if (player.isSwimming) {
                    player.velocity = player.location.direction.multiply((1.5 - player.velocity.length()) * 0.1 + player.velocity.length())
                }
                if (event.from.block.type == Material.WATER && event.to.block.type.isAir) {
                    if (player.velocity.length() > 0.3)
                        player.velocity = player.location.direction.multiply(player.velocity.length() * 3)
                    player.sendMessage(player.velocity.length().toString())
                }
            }
        }
    }

    @EventHandler
    fun playerDamage(event: EntityDamageEvent) {
        val player = if (event.entity is Player) event.entity as Player else return

        if (Classes.getClass(player.uniqueId) == QuirkClass.DIVER && event.cause == EntityDamageEvent.DamageCause.FALL && player.world.environment != World.Environment.NETHER) {
            event.isCancelled = true
            val block = player.location.block
            if (!block.type.isAir) block.breakNaturally()
            block.type = Material.WATER
            block.world.playSound(block.location, Sound.ITEM_BUCKET_EMPTY, 1.0f, 1.0f)
            SchedulerUtil.later(10) {
                if (block.type == Material.WATER) {
                    block.type = Material.AIR
                    block.world.playSound(block.location, Sound.ITEM_BUCKET_FILL, 1.0f, 1.0f)
                }
            }
        }
    }

    @EventHandler
    fun entityDamageEntity(event: EntityDamageByEntityEvent) {
        val player = if (event.damager is Player) event.damager as Player else return

        if (Classes.getClass(player.uniqueId) == QuirkClass.LAVACASTER) {
            event.entity.fireTicks = 80
        }
    }
}
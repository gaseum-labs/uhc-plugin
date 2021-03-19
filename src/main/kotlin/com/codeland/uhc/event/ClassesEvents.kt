package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EnchantingInventory
import org.bukkit.inventory.ItemStack

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
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
            val player = event.player

            when (Classes.getClass(player.uniqueId)) {
                QuirkClass.LAVACASTER -> {
                    for (dx in -3..3) for (dy in -2..-1) for (dz in -3..3) {
                        val block = player.location.block.getRelative(dx, dy, dz)
                        if (block.type === Material.LAVA && surface(block)) {
                            val flowing = (block.blockData as Levelled).level != 0
                            block.setType(Material.OBSIDIAN, true)
                            Classes.obsidianifiedLava.add(Classes.ObsidianifiedLava(block, flowing))
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
    }

    @EventHandler
    fun playerDamage(event: EntityDamageEvent) {
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
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
    }

    @EventHandler
    fun entityDamageEntity(event: EntityDamageByEntityEvent) {
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
            val player = if (event.damager is Player) event.damager as Player else return

            if (Classes.getClass(player.uniqueId) == QuirkClass.LAVACASTER) {
                event.entity.fireTicks = 80
            }
        }
    }

    @EventHandler
    fun onXP(event: PlayerExpChangeEvent) {
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
            if (Classes.getClass(event.player.uniqueId) == QuirkClass.ENCHANTER) {
                event.amount = event.amount * 2
            }
        }
    }

    @EventHandler
    fun onEnchant(event: EnchantItemEvent) {
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
            if (Classes.getClass(event.enchanter.uniqueId) == QuirkClass.ENCHANTER) {
                val inventory = event.view.topInventory as EnchantingInventory

                val lapis = inventory.secondary

                if (lapis == null)
                    inventory.secondary = ItemStack(Material.LAPIS_LAZULI)
                else
                    ++lapis.amount
            }
        }
    }
}
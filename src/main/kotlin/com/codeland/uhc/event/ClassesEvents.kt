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
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.Grindstone
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerExpChangeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
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

    fun <T> auraReplacer(player: Player, list: MutableList<T>, from: Material, to: Material, radiusX: Int, lowY: Int, highY: Int, radiusZ: Int, produce: (Block) -> T) {
        for (dx in -radiusX..radiusX)
            for (dy in lowY..highY)
                for (dz in -radiusZ..radiusZ) {
                    val block = player.location.block.getRelative(dx, dy, dz)

                    if (block.type === from && surface(block)) {
                        block.setType(to, true)
                        list.add(produce(block))
                    }
                }
    }

    @EventHandler
    fun playerMove(event: PlayerMoveEvent) {
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
            val player = event.player

            when (Classes.getClass(player.uniqueId)) {
                QuirkClass.LAVACASTER -> {
                    auraReplacer(player, Classes.obsidianifiedLava, Material.LAVA, Material.OBSIDIAN, 3, -2, -1, 3) { block ->
                        Classes.ObsidianifiedLava(block, (block.blockData as Levelled).level != 0)
                    }
                }
                QuirkClass.ENCHANTER -> {
                    auraReplacer(player, Classes.grindedStone, Material.STONE, Material.GRINDSTONE, 1, -2, -1, 1) { block ->
                        val data = block.blockData as Grindstone
                        data.attachedFace = FaceAttachable.AttachedFace.FLOOR
                        block.setBlockData(data, false)
                        block
                    }
                }
                QuirkClass.DIVER -> {
                    if (player.isSwimming) {
                        player.velocity = player.location.direction.multiply((1.5 - player.velocity.length()) * 0.1 + player.velocity.length())
                    }
                    if (event.from.block.type === Material.WATER && event.to.block.type.isAir) {
                        if (player.velocity.length() > 0.3)
                            player.velocity = player.location.direction.multiply(player.velocity.length() * 3)
                    }
                }
            }
        }
    }

    @EventHandler
    fun playerDamage(event: EntityDamageEvent) {
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
            val player = if (event.entity is Player) event.entity as Player else return

            if (Classes.getClass(player.uniqueId) === QuirkClass.DIVER && event.cause === EntityDamageEvent.DamageCause.FALL && player.world.environment !== World.Environment.NETHER) {
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
            if (Classes.getClass(player.uniqueId) == QuirkClass.TRAPPER && event.cause == EntityDamageEvent.DamageCause.FALL) {
                event.isCancelled = true
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

    @EventHandler
    fun onShift(event: PlayerToggleSneakEvent) {
        if (event.isSneaking && Classes.getClass(event.player.uniqueId) == QuirkClass.TRAPPER) {
            var activated = false
            if (Classes.lastShiftMap[event.player.uniqueId] != null) {
                val lastShift = Classes.lastShiftMap[event.player.uniqueId]!!
                if (System.currentTimeMillis() - lastShift < 500) {
                    activated = true
                    val RADIUS = 10
                    for (dx in -RADIUS..RADIUS) for (dy in -RADIUS..RADIUS) for (dz in -RADIUS..RADIUS) {
                        val block = event.player.location.block.getRelative(dx, dy, dz)
                        if (block.type == Material.LEVER) {
                            val data: Switch = block.blockData as Switch
                            data.isPowered = !data.isPowered
                            block.blockData = data
                        }
                    }
                }
            }
            Classes.lastShiftMap[event.player.uniqueId] = System.currentTimeMillis()
            if (activated) {
                // to prevent triple shift from triggering twice
                Classes.lastShiftMap[event.player.uniqueId] = 0
            }
        }
    }
}
package com.codeland.uhc.event

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.quirk.quirks.classes.Classes
import com.codeland.uhc.quirk.quirks.classes.QuirkClass
import com.codeland.uhc.util.SchedulerUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.Grindstone
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
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
        return BlockFace.values().take(6).any { d ->
            block.getRelative(d).type.isAir
        }
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
                    } else if (player.location.block.type == Material.WATER) {
                        player.velocity = player.velocity.multiply(3)
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
            val player = event.damager as? Player ?: return

            when (Classes.getClass(player.uniqueId)) {
                QuirkClass.LAVACASTER -> {
                    event.entity.fireTicks = 80
                }
                QuirkClass.ENCHANTER -> {
                    val hurtPlayer = event.entity as? Player ?: return

                    val enchantColors = arrayOf(
                        ChatColor.RED, ChatColor.GOLD,
                        ChatColor.YELLOW, ChatColor.GREEN,
                        ChatColor.AQUA, ChatColor.BLUE,
                        ChatColor.LIGHT_PURPLE
                    )

                    fun enchantColor(index: Int) = if (index > enchantColors.lastIndex)
                        enchantColors.last() else enchantColors[index]

                    fun tellItem(itemStack: ItemStack) {
                        val meta = itemStack.itemMeta

                        player.sendActionBar(meta.enchants.map { (enchant, level) -> "${enchant.key.key} $level" }
                            .foldIndexed("${ChatColor.WHITE}${hurtPlayer.name}'s ${itemStack.type.name.toLowerCase()}: ") { index, acc, next ->
                                if (index == meta.enchants.size - 1)
                                    "$acc${enchantColor(index)}$next"
                                else
                                    "$acc${enchantColor(index)}$next, "
                            }
                        )
                    }

                    fun validItem(itemStack: ItemStack?) = itemStack != null &&
                        itemStack?.itemMeta?.enchants?.isNotEmpty() != null

                    fun tellSlot(slot: Int): Boolean {
                        val item = if (slot == 4)
                            hurtPlayer.inventory.itemInMainHand
                        else
                            hurtPlayer.inventory.armorContents[slot]

                        return if (validItem(item)) {
                            tellItem(item); true
                        } else false
                    }

                    val offset = (Math.random() * 5).toInt()
                    (0..4).any { slot -> tellSlot((slot + offset) % 5) }
                }
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
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
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

    @EventHandler
    fun blockBreak(event: BlockBreakEvent) {
        if (GameRunner.uhc.isEnabled(QuirkType.CLASSES)) {
            if (Classes.getClass(event.player.uniqueId) == QuirkClass.TRAPPER) {
                val logs = listOf(
                        Material.OAK_LOG,
                        Material.BIRCH_LOG,
                        Material.DARK_OAK_LOG,
                        Material.ACACIA_LOG,
                        Material.SPRUCE_LOG,
                        Material.JUNGLE_LOG)
                if (event.block.type in logs) {
                    // breaks all blocks adjacent to this block of the same type, with a delay
                    fun breakRecursively(block: Block, type: Material) {
                        for (d in BlockFace.values().take(6)) {
                            val nextBlock = block.getRelative(d)
                            if (nextBlock.type == type) {
                                SchedulerUtil.later(1) {
                                    // extra check to make sure the block hasn't changed
                                    if (nextBlock.type == type) {
                                        nextBlock.breakNaturally()
                                        breakRecursively(nextBlock, type)
                                    }
                                }
                            }
                        }
                    }
                    breakRecursively(event.block, event.block.type)
                }
            }
        }
    }
}

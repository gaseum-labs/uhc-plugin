package com.codeland.uhc.phase.phases.waiting

import com.codeland.uhc.core.GameRunner
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object LobbyPvp {
    class PvpData(
            var inPvp: Boolean = false,
            var onPvpBlock: Boolean = false,
            var gameMode: GameMode,
            var inventoryContents: Array<out ItemStack>
    )
    val pvpMap = mutableMapOf<Player, PvpData>()
    val PVP_BLOCK = Material.LAPIS_BLOCK
    val REGEN_BLOCK = Material.EMERALD_BLOCK

    val PVP_ARMOR = Array(4) {
        i -> ItemStack(Material.values()[Material.IRON_HELMET.ordinal + (3 - i)])
    }

    val PVP_ITEMS = listOf(
            ItemStack(Material.IRON_SWORD, 1),
            ItemStack(Material.IRON_AXE, 1),
            {
                val b = ItemStack(Material.BOW, 1)
                b.addEnchantment(Enchantment.ARROW_INFINITE, 1)
                b
            }(),
            ItemStack(Material.ARROW, 1),
            ItemStack(Material.GOLDEN_APPLE, 3)
    )

    fun getPvpData(player: Player): PvpData {
        if (pvpMap[player] == null) pvpMap[player] = PvpData(gameMode = player.gameMode, inventoryContents = player.inventory.contents)
        return pvpMap[player]!!
    }

    fun enablePvp(player: Player, pvpData: PvpData) {
        GameRunner.sendGameMessage(player, "You enabled pvp.")
        pvpData.inPvp = true

        // save
        pvpData.inventoryContents = player.inventory.contents.clone()
        pvpData.gameMode = player.gameMode

        player.gameMode = GameMode.ADVENTURE
        player.inventory.clear()
        fun unbreakable(item: ItemStack): ItemStack {
            val m = item.itemMeta
            m.isUnbreakable = true
            item.itemMeta = m
            return item
        }
        player.inventory.addItem(*PVP_ITEMS.map(::unbreakable).toTypedArray())
        player.inventory.setItemInOffHand(unbreakable(ItemStack(Material.SHIELD)))
        player.inventory.setArmorContents(PVP_ARMOR.map(::unbreakable).toTypedArray())
    }

    fun disablePvp(player: Player, pvpData: PvpData) {
        GameRunner.sendGameMessage(player, "You disabled pvp.")
        pvpData.inPvp = false

        // restore
        player.inventory.contents = pvpData.inventoryContents
        player.gameMode = pvpData.gameMode

        player.health = player.maxHealth
        player.activePotionEffects.clear()
    }

    fun swapPvp(player: Player, pvpData: PvpData) {
        if (pvpData.inPvp) {
            disablePvp(player, pvpData)
        } else {
            enablePvp(player, pvpData)
        }
    }

    fun updatePvp(player: Player) {
        val pvpData = getPvpData(player)

        if (player.location.block.getRelative(0, -1, 0).type == PVP_BLOCK) {
            if (!pvpData.onPvpBlock) {
                pvpData.onPvpBlock = true
                swapPvp(player, pvpData)
            }
        } else {
            if (pvpData.onPvpBlock) {
                pvpData.onPvpBlock = false
            }
        }

        if (
            player.location.block.getRelative(0, -1, 0).type == REGEN_BLOCK &&
            getPvpData(player).inPvp &&
            !player.hasPotionEffect(PotionEffectType.REGENERATION)
        ) {
            player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 40, 2))
        }
    }
}
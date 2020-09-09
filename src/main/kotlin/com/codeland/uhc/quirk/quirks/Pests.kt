package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import com.codeland.uhc.util.ItemUtil.randomDyeArmor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

class Pests(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {
    override fun onEnable() {}

    override fun onDisable() {
        if (!(GameRunner.uhc.isPhase(PhaseType.WAITING) || GameRunner.uhc.isPhase(PhaseType.POSTGAME))) {
            /* kill all pests in the game */
            Bukkit.getOnlinePlayers().forEach { player ->
                if (isPest(player))
                    player.gameMode = GameMode.SPECTATOR
            }
        }

        /* now nobody is a pest */
        Bukkit.getOnlinePlayers().forEach { player ->
            makeNotPest(player)
        }
    }

    companion object {
        private var META_TAG = "isPest"

        private val pestArmorMeta: LeatherArmorMeta = ItemStack(Material.LEATHER_HELMET).itemMeta as LeatherArmorMeta
        private val pestToolMeta: ItemMeta = ItemStack(Material.WOODEN_PICKAXE).itemMeta

        init {
            pestArmorMeta.isUnbreakable = true
            pestArmorMeta.setDisplayName("${ChatColor.RESET}${ChatColor.GRAY}Pest Armor")
            pestArmorMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
            pestArmorMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)

            pestToolMeta.isUnbreakable = true
            pestToolMeta.setDisplayName("${ChatColor.RESET}${ChatColor.GRAY}Pest Tool")
            pestToolMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        }

        fun isPest(player: Player): Boolean {
            var meta = player.getMetadata(META_TAG)

            return meta.size != 0 && meta[0].asBoolean()
        }

        fun makePest(player: Player) {
            player.setMetadata(META_TAG, FixedMetadataValue(UHCPlugin.plugin as Plugin, true))
        }

        fun givePestSetup(player: Player) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 4.0

            /* give pest a bunch of crap */
            player.inventory.helmet = genPestArmor(Material.LEATHER_HELMET)
            player.inventory.chestplate = genPestArmor(Material.LEATHER_CHESTPLATE)
            player.inventory.leggings = genPestArmor(Material.LEATHER_LEGGINGS)
            player.inventory.boots = genPestArmor(Material.LEATHER_BOOTS)

            player.inventory.setItem(0, genPestTool(Material.WOODEN_SWORD))
            player.inventory.setItem(1, genPestTool(Material.WOODEN_PICKAXE))
            player.inventory.setItem(2, genPestTool(Material.WOODEN_AXE))
            player.inventory.setItem(3, genPestTool(Material.WOODEN_SHOVEL))
        }

        fun makeNotPest(player: Player) {
            player.setMetadata(META_TAG, FixedMetadataValue(UHCPlugin.plugin as Plugin, false))
        }

        fun genPestArmor(item: Material): ItemStack {
            var stack = ItemStack(item)

            stack.itemMeta = pestArmorMeta

            return randomDyeArmor(stack)
        }

        val pestToolList = arrayOf(
            Material.WOODEN_SWORD,
            Material.WOODEN_PICKAXE,
            Material.WOODEN_AXE,
            Material.WOODEN_SHOVEL
        )

        fun genPestTool(item: Material): ItemStack {
            var stack = ItemStack(item)

            stack.itemMeta = pestToolMeta

            return stack
        }

        val banList = {
            val arr = arrayOf<Material>(
                    Material.IRON_PICKAXE,
                    Material.IRON_AXE,
                    Material.IRON_HOE,
                    Material.IRON_SHOVEL,
                    Material.IRON_SWORD,
                    Material.IRON_HELMET,
                    Material.IRON_CHESTPLATE,
                    Material.IRON_LEGGINGS,
                    Material.IRON_BOOTS,
                    Material.BOW,
                    Material.CROSSBOW,
                    Material.SHIELD,
                    Material.BUCKET,
                    Material.DIAMOND_PICKAXE,
                    Material.DIAMOND_AXE,
                    Material.DIAMOND_HOE,
                    Material.DIAMOND_SHOVEL,
                    Material.DIAMOND_SWORD,
                    Material.DIAMOND_HELMET,
                    Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_LEGGINGS,
                    Material.DIAMOND_BOOTS
            )

            arr.sort()

            arr
        }()
    }
}
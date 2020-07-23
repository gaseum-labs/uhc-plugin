package com.codeland.uhc.quirk

import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.phaseType.PhaseType
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

class Pests(type: QuirkType) : Quirk(type) {
    override fun onEnable() {

    }

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

        fun isPest(player: Player): Boolean {
            var meta = player.getMetadata(META_TAG)

            return meta.size != 0 && meta[0].asBoolean()
        }

        fun makePest(player: Player) {
            player.setMetadata(META_TAG, FixedMetadataValue(GameRunner.plugin as Plugin, true))
        }

        fun makeNotPest(player: Player) {
            player.setMetadata(META_TAG, FixedMetadataValue(GameRunner.plugin as Plugin, false))
        }

        private val pestArmorMeta = {
            var meta = ItemStack(Material.LEATHER_HELMET).itemMeta

            meta.isUnbreakable = true
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true)
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)

            meta
        }()

        private val pestToolMeta = {
            var meta = ItemStack(Material.WOODEN_PICKAXE).itemMeta

            meta.isUnbreakable = true;
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)

            meta
        }()

        fun genPestArmor(item: Material): ItemStack {
            var stack = ItemStack(item)

            stack.itemMeta = pestArmorMeta;

            return stack
        }

        fun genPestTool(item: Material): ItemStack {
            var stack = ItemStack(item)

            stack.itemMeta = pestToolMeta;

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
package com.codeland.uhc.quirk

import com.codeland.uhc.phaseType.PhaseVariant
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Hotbar(type: QuirkType) : Quirk(type) {

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    override fun onPhaseSwitch(phase: PhaseVariant) {
        if (phase == PhaseVariant.GRACE_DEFAULT) {
            for (player in Bukkit.getOnlinePlayers()) {
                val inv = player.inventory
                for (slot in 9 until 36) {
                    val item = ItemStack(Material.BLACK_STAINED_GLASS_PANE)
                    item.itemMeta = {
                        val meta = item.itemMeta
                        meta.setDisplayName("Unusable Slot")
                        meta
                    }()
                    inv.setItem(slot, ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                }
            }
        }
    }
}
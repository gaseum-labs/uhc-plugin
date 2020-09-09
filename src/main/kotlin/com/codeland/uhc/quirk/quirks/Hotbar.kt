package com.codeland.uhc.quirk.quirks

import com.codeland.uhc.core.UHC
import com.codeland.uhc.phaseType.PhaseType
import com.codeland.uhc.phaseType.PhaseVariant
import com.codeland.uhc.quirk.Quirk
import com.codeland.uhc.quirk.QuirkType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Hotbar(uhc: UHC, type: QuirkType) : Quirk(uhc, type) {

    override fun onEnable() {
    }

    override fun onDisable() {
    }

    override fun onPhaseSwitch(phase: PhaseVariant) {
        if (phase.type == PhaseType.GRACE) {
            for (player in Bukkit.getOnlinePlayers()) {
                val inv = player.inventory
                for (slot in 9 until 36) {
                    inv.setItem(slot, ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                }
            }
        }
    }
}
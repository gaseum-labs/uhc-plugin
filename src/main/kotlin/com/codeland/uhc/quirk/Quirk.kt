package com.codeland.uhc.quirk

import com.codeland.uhc.gui.Gui
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

enum class Quirk(prettyName: String, representation: Material) {
    HALF_ZATOICHI("Half Zatoichi", Material.IRON_SWORD),
    ABUNDANCE("Abundance", Material.BLUE_ORCHID),
    UNSHELTERED("Unsheltered", Material.SHULKER_SHELL),
    PESTS("Pests", Material.LEATHER_CHESTPLATE),
	WET_SPONGE("Wet Sponge (don't question it)", Material.WET_SPONGE),
	MODIFIED_DROPS("Modified Drops", Material.ROTTEN_FLESH);

    var prettyName = prettyName
    var representation = representation

    var enabled = false
    set(value) {
        field = value
        Gui.updateQuirk(this)
    }

    var incompatibilities = mutableSetOf<Quirk>()

    /**
     * you have to do this after initialization to
     * have all the quirks already there
     *
     * this function is both ways, it will update the
     * incompatibilities of each quirk passed in as well
     */
    fun setIncompatible(vararg quirks: Quirk) {
        incompatibilities.addAll(quirks)

        quirks.forEach { quirk ->
            quirk.incompatibilities.add(this)
        }
    }

    fun isIncompatible(other: Quirk) {
        incompatibilities.contains(other)
    }

    companion object {
        init {
            HALF_ZATOICHI.setIncompatible(PESTS)
        }

        fun randomEnchantedBook(): ItemStack {
            val ret = ItemStack(Material.BOOK)

            val meta = ret.itemMeta

            val enchant = Enchantment.values()[(Math.random() * Enchantment.values().size).toInt()]
            meta.addEnchant(enchant, (Math.random() * (enchant.maxLevel + 1)).toInt(), true)

            ret.itemMeta = meta

            return ret
        }
    }
}
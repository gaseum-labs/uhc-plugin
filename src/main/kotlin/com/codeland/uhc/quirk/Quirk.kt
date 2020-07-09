package com.codeland.uhc.quirk

import org.bukkit.Material

enum class Quirk(prettyName: String, representation: Material) {
    HALF_ZATOICHI("Half Zatoichi", Material.IRON_SWORD),
    ABUNDANCE("Abundance", Material.BLUE_ORCHID),
    UNSHELTERED("Unsheltered", Material.SHULKER_SHELL),
    PESTS("Pests", Material.LEATHER_CHESTPLATE);

    var prettyName = prettyName
    var representation = representation

    var enabled = false
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
    }
}
package com.codeland.uhc.quirk

import com.codeland.uhc.gui.Gui
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

enum class Quirk(var prettyName: String, var representation: Material, var description: Array<String>) {
    HALF_ZATOICHI("Half Zatoichi", Material.IRON_SWORD, arrayOf(
        "Everyone gets a special sword",
        "You are honor bound to kill",
        "Only wield if a battle is unavoidable"
    )),

    ABUNDANCE("Abundance", Material.BLUE_ORCHID, arrayOf(
        "All block and mobs drop extra loot",
        "Similar to if everything had fortune and looting"
    )),

    UNSHELTERED("Unsheltered", Material.SHULKER_SHELL, arrayOf(
		"Terrain cannot be modified",
		"You cannot place or mine blocks",
		"But you still get the block loot"
    )),

    PESTS("Pests", Material.LEATHER_CHESTPLATE, arrayOf(
		"Dead players come back to exact their revenge",
		"But they are weak and have no access to advanced tools"
	)),

	WET_SPONGE("Wet Sponge (don't question it)", Material.WET_SPONGE, arrayOf(
		"I have no idea"
	)),

	MODIFIED_DROPS("Modified Drops", Material.ROTTEN_FLESH, arrayOf(
		"Hostile mobs drop exceptional loot"
	)),

    CREATIVE("Creative", Material.STONE, arrayOf(
		"You may place blocks without them emptying from your inventory"
	)),

	SUMMONER("Summoner", Material.CREEPER_SPAWN_EGG, arrayOf(
		"Mobs drop their spawn eggs"
	));

    var enabled = false
    private set

    fun updateEnabled(value: Boolean) {
        enabled = value
        Gui.updateQuirk(this)

        incompatibilities.forEach { other ->
            if (other.enabled) {
                other.enabled = false
                Gui.updateQuirk(other)
            }
        }
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
            CREATIVE.setIncompatible(UNSHELTERED)
        }
    }
}
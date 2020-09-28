package com.codeland.uhc.quirk

import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.quirks.*
import org.bukkit.Material

enum class QuirkType(var prettyName: String, var create: (UHC, QuirkType) -> Quirk, var defaultEnabled: Boolean, var representation: Material, var description: Array<String>) {
    HALF_ZATOICHI("Half Zatoichi", ::HalfZatoichi, false, Material.IRON_SWORD, arrayOf(
        "Everyone gets a special sword",
        "You are honor bound to kill",
        "Only wield if a battle is unavoidable"
    )),

    ABUNDANCE("Abundance", ::Abundance, false, Material.BLUE_ORCHID, arrayOf(
        "All block and mobs drop extra loot",
        "Similar to if everything had fortune and looting"
    )),

    UNSHELTERED("Unsheltered", ::Unsheltered, false, Material.SHULKER_SHELL, arrayOf(
		"Terrain cannot be modified",
		"You cannot place or mine blocks",
		"But you still get the block loot"
    )),

    PESTS("Pests", ::Pests, false, Material.LEATHER_CHESTPLATE, arrayOf(
		"Dead players come back to exact their revenge",
		"But they are weak and have no access to advanced tools"
	)),

	WET_SPONGE("Wet Sponge (don't question it)", ::WetSponge, false, Material.WET_SPONGE, arrayOf(
		"Gain an infinite amount of wet sponge that you must turn into dry sponge"
	)),

	MODIFIED_DROPS("Modified Drops", ::ModifiedDrops, false, Material.ROTTEN_FLESH, arrayOf(
		"Hostile mobs drop exceptional loot"
	)),

    CREATIVE("Creative", ::Creative, false, Material.STONE, arrayOf(
		"you may place tough to get blocks without them emptying from your inventory"
	)),

	SUMMONER("Summoner", ::Summoner, false, Material.MULE_SPAWN_EGG, arrayOf(
		"Mobs drop their spawn eggs when killed"
	)),

	RANDOM_EFFECTS("Random Effects", ::RandomEffects, false, Material.POTION, arrayOf(
		"Every 3 minutes,",
		"Everyone gets a random potion effect"
	)),

	SHARED_INVENTORY("Shared Inventory", ::SharedInventory, false, Material.KNOWLEDGE_BOOK, arrayOf(
		"Everyone has one combined inventory"
	)),

	LOW_GRAVITY("Low Gravity", ::LowGravity, false, Material.END_STONE, arrayOf(
		"Gravity is much lower than usual"
	)),

	HOTBAR("Limited Inventory", ::Hotbar, false, Material.OBSIDIAN, arrayOf(
		"All players are limited to only",
		"their hotbar to store items"
  	)),

	CARE_PACKAGES("Care Packages", ::CarePackages, true, Material.CHEST_MINECART, arrayOf(
		"Chests periodically drop containing good loot",
		"go there and you should expect a fight"
	)),

	DEATHSWAP("Deathswap", ::Deathswap, false, Material.MAGENTA_GLAZED_TERRACOTTA, arrayOf(
			"Players switch places with each other",
			"at randomly chosen intervals"
	));

   	var incompatibilities = mutableSetOf<QuirkType>()

    /**
     * you have to do this after initialization to
     * have all the quirks already there
     *
     * this function is both ways, it will update the
     * incompatibilities of each quirk passed in as well
     */
    fun setIncompatible(vararg quirks: QuirkType) {
        incompatibilities.addAll(quirks)

        quirks.forEach { quirk ->
            quirk.incompatibilities.add(this)
        }
    }

    fun isIncompatible(other: QuirkType) {
        incompatibilities.contains(other)
    }

    companion object {
        init {
            CREATIVE.setIncompatible(UNSHELTERED)
			SHARED_INVENTORY.setIncompatible(HOTBAR) // could this be compatible?
		}
    }

	fun createQuirk(uhc: UHC): Quirk {
		return create(uhc, this)
	}
}
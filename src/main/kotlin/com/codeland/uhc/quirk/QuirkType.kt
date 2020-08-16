package com.codeland.uhc.quirk

import CarePackages
import org.bukkit.Material

enum class QuirkType(var prettyName: String, var create: (QuirkType) -> Quirk, var defaultEnabled: Boolean, var representation: Material, var description: Array<String>) {
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

	AGGRO_SUMMONER("Summoner Aggro", ::Summoner, false, Material.CREEPER_SPAWN_EGG, arrayOf(
		"Hostile mobs drop their spawn eggs"
	)),

	PASSIVE_SUMMONER("Summoner Passive", ::Summoner, false, Material.CHICKEN_SPAWN_EGG, arrayOf(
		"Passive mobs drop their spawn eggs"
	)),

	COMMANDER("Commander", ::Commander, false, Material.NETHERITE_HELMET, arrayOf(
		"Spawned mobs obey your command"
	)),

	APPLE_FIX("Apple Fix", ::AppleFix, true, Material.APPLE, arrayOf(
		"Less random apple drops"
	)),

	RANDOM_EFFECTS("Random Effects", ::RandomEffects, false, Material.POTION, arrayOf(
		"Every 5 minutes,",
		"Everyone gets a random potion effect"
	)),

	SHARED_INVENTORY("Shared Inventory", ::SharedInventory, false, Material.KNOWLEDGE_BOOK, arrayOf(
		"Everyone has one combined inventory"
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
        }
    }

	fun createQuirk(): Quirk {
		return create(this)
	}
}
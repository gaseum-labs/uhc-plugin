package com.codeland.uhc.quirk

import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.quirk.quirks.carePackages.CarePackages
import com.codeland.uhc.quirk.quirks.carePackages.ChaoticCarePackages
import org.bukkit.Material

enum class QuirkType(var prettyName: String, var create: (UHC, QuirkType) -> Quirk, var defaultEnabled: Boolean, var representation: Material, var description: Array<String>) {
    UNSHELTERED("Unsheltered", ::Unsheltered, false, Material.SHULKER_SHELL, arrayOf(
		"Terrain cannot be modified",
		"You cannot place or mine blocks",
		"But you still get the block loot"
    )),

    PESTS("Pests", ::Pests, false, Material.LEATHER_CHESTPLATE, arrayOf(
		"Dead players come back to exact their revenge",
		"But they are weak and have no access to advanced tools"
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

	CARE_PACKAGES("Care Packages", ::CarePackages, false, Material.CHEST_MINECART, arrayOf(
		"Chests periodically drop containing good loot",
		"go there and you should expect a fight"
	)),

	CHAOTIC_CARE_PACKAGES("Chaotic Care Packages", ::ChaoticCarePackages, false, Material.TIPPED_ARROW, arrayOf(
		"Chests drop every 5 seconds",
		"Wacky loot is inside"
	)),

	DEATHSWAP("Deathswap", ::Deathswap, false, Material.MAGENTA_GLAZED_TERRACOTTA, arrayOf(
		"Players switch places with each other",
		"at randomly chosen intervals"
	)),

	BETRAYAL("Betrayal", ::Betrayal, false, Material.BONE, arrayOf(
		"Players swap teams when killed",
		"Game ends when everyone is on one team"
	)),

	HALLOWEEN("Halloween", ::Halloween, false, Material.PUMPKIN, arrayOf(
		"Mobs drop candy",
		"Witches?!"
	)),

	CHRISTMAS("Christmas", ::Christmas, false, Material.COOKIE, arrayOf(
		"It's snowing all the time!"
	)),

	FLYING("Flying", ::Flying, false, Material.FIREWORK_ROCKET, arrayOf(
		"Start with an elytra and rockets"
	)),

	PLAYER_COMPASS("Player Compasses", ::PlayerCompass, false, Material.COMPASS, arrayOf(
		"Track down players with a special compass"
	)),

	UNDERWATER("Underwater", ::Underwater, false, Material.COD, arrayOf(
		"Breath and mine fast underwater",
		"Dolphins drop leather"
	)),

	INFINITE_INVENTORY("Infinite Inventory", ::InfiniteInventory, false, Material.CHEST, arrayOf(
			"Your inventory is unbounded in size"
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

	fun createQuirk(uhc: UHC): Quirk {
		/* quirk instance from quirk type */
		val quirk = create(uhc, this)

		/* give quirk instance to UHC */
		return quirk
	}

    companion object {
        init {
            CREATIVE.setIncompatible(UNSHELTERED)
			PESTS.setIncompatible(BETRAYAL)
		}
    }
}
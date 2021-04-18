package com.codeland.uhc.quirk

import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.quirk.quirks.carePackages.CarePackages
import com.codeland.uhc.quirk.quirks.carePackages.ChaoticCarePackages
import com.codeland.uhc.quirk.quirks.Pumpkin
import com.codeland.uhc.quirk.quirks.classes.Classes

enum class QuirkType(val prettyName: String, val create: (QuirkType) -> Quirk, val description: Array<String>) {
	UNSHELTERED("Unsheltered", ::Unsheltered, arrayOf(
		"Terrain cannot be modified",
		"You cannot place or mine blocks",
		"But you still get the block loot"
    )),

    PESTS("Pests", ::Pests, arrayOf(
		"Dead players come back to exact their revenge",
		"But they are weak and have no access to advanced tools"
	)),

	MODIFIED_DROPS("Modified Drops", ::ModifiedDrops, arrayOf(
		"Hostile mobs drop exceptional loot"
	)),

    CREATIVE("Creative", ::Creative, arrayOf(
		"you may place tough to get blocks without them emptying from your inventory"
	)),

	SUMMONER("Summoner", ::Summoner, arrayOf(
		"Mobs drop their spawn eggs when killed"
	)),

	RANDOM_EFFECTS("Random Effects", ::RandomEffects, arrayOf(
		"Every 3 minutes,",
		"Everyone gets a random potion effect"
	)),

	SHARED_INVENTORY("Shared Inventory", ::SharedInventory, arrayOf(
		"Everyone has one combined inventory"
	)),

	LOW_GRAVITY("Low Gravity", ::LowGravity, arrayOf(
		"Gravity is much lower than usual"
	)),

	HOTBAR("Limited Inventory", ::Hotbar, arrayOf(
		"All players are limited to only",
		"their hotbar to store items"
  	)),

	CARE_PACKAGES("Care Packages", ::CarePackages, arrayOf(
		"Chests periodically drop containing good loot",
		"go there and you should expect a fight"
	)),

	CHAOTIC_CARE_PACKAGES("Chaotic Care Packages", ::ChaoticCarePackages, arrayOf(
		"Chests drop every 5 seconds",
		"Wacky loot is inside"
	)),

	DEATHSWAP("Deathswap", ::Deathswap, arrayOf(
		"Players switch places with each other",
		"at randomly chosen intervals"
	)),

	HALLOWEEN("Halloween", ::Halloween, arrayOf(
		"Mobs drop candy",
		"Witches?!"
	)),

	PUMPKIN("Pumpkin", ::Pumpkin, arrayOf(
		"You are forced to have a pumpkin on your head"
	)),

	CHRISTMAS("Christmas", ::Christmas, arrayOf(
		"It's snowing all the time!"
	)),

	FLYING("Flying", ::Flying, arrayOf(
		"Start with an elytra and rockets"
	)),

	PLAYER_COMPASS("Player Compasses", ::PlayerCompass, arrayOf(
		"Track down players with a special compass"
	)),

	INFINITE_INVENTORY("Infinite Inventory", ::InfiniteInventory, arrayOf(
			"Your inventory is unbounded in size"
	)),

	CLASSES("Classes", ::Classes, arrayOf(
		"Pick a class as the game begins",
		"Get cool abilities"
	)),
	HORSE("Horse", ::HorseQuirk, arrayOf(
			"Horse",
			"Horse"
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

	fun createQuirk(): Quirk {
		/* quirk instance from quirk type */
		val quirk = create(this)

		/* give quirk instance to UHC */
		return quirk
	}

    companion object {
        init {
            CREATIVE.setIncompatible(UNSHELTERED)
	        CARE_PACKAGES.setIncompatible(CHAOTIC_CARE_PACKAGES)
	        INFINITE_INVENTORY.setIncompatible(HOTBAR)
		}
    }
}
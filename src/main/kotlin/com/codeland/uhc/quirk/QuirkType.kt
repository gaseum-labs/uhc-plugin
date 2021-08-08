package com.codeland.uhc.quirk

import com.codeland.uhc.core.Game
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.quirk.quirks.carePackages.CarePackages
import com.codeland.uhc.quirk.quirks.carePackages.ChaoticCarePackages
import com.codeland.uhc.quirk.quirks.classes.Classes
import net.kyori.adventure.text.Component

enum class QuirkType(val prettyName: String, val create: (QuirkType, Game) -> Quirk, val description: List<Component>) {
	UNSHELTERED("Unsheltered", ::Unsheltered, listOf(
		Component.text("Terrain cannot be modified"),
		Component.text("You cannot place or mine blocks"),
		Component.text("But you still get the block loot")
    )),

    PESTS("Pests", ::Pests, listOf(
		Component.text("Dead players come back to exact their revenge"),
		Component.text("But they are weak and have no access to advanced tools")
	)),

	MODIFIED_DROPS("Modified Drops", ::ModifiedDrops, listOf(
		Component.text("Hostile mobs drop exceptional loot")
	)),

    CREATIVE("Creative", ::Creative, listOf(
	    Component.text("you may place tough to get blocks without them emptying from your inventory")
	)),

	SUMMONER("Summoner", ::Summoner, listOf(
		Component.text("Mobs drop their spawn eggs when killed")
	)),

	RANDOM_EFFECTS("Random Effects", ::RandomEffects, listOf(
		Component.text("Every 3 minutes,"),
		Component.text("Everyone gets a random potion effect")
	)),

	SHARED_INVENTORY("Shared Inventory", ::SharedInventory, listOf(
		Component.text("Everyone has one combined inventory")
	)),

	LOW_GRAVITY("Low Gravity", ::LowGravity, listOf(
		Component.text("Gravity is much lower than usual")
	)),

	HOTBAR("Limited Inventory", ::Hotbar, listOf(
		Component.text("All players are limited to only"),
		Component.text("their hotbar to store items")
  	)),

	CARE_PACKAGES("Care Packages", ::CarePackages, listOf(
		Component.text("Chests periodically drop containing good loot"),
		Component.text("go there and you should expect a fight")
	)),

	CHAOTIC_CARE_PACKAGES("Chaotic Care Packages", ::ChaoticCarePackages, listOf(
		Component.text("Chests drop every 5 seconds"),
		Component.text("Wacky loot is inside")
	)),

	DEATHSWAP("Deathswap", ::Deathswap, listOf(
		Component.text("Players switch places with each other"),
		Component.text("at randomly chosen intervals")
	)),

	HALLOWEEN("Halloween", ::Halloween, listOf(
		Component.text("Mobs drop candy"),
		Component.text("Witches?!")
	)),

	PUMPKIN("Pumpkin", ::Pumpkin, listOf(
		Component.text("You are forced to have a pumpkin on your head")
	)),

	CHRISTMAS("Christmas", ::Christmas, listOf(
		Component.text("It's snowing all the time!")
	)),

	FLYING("Flying", ::Flying, listOf(
		Component.text("Start with an elytra and rockets")
	)),

	PLAYER_COMPASS("Player Compasses", ::PlayerCompass, listOf(
		Component.text("Track down players with a special compass")
	)),

	INFINITE_INVENTORY("Infinite Inventory", ::InfiniteInventory, listOf(
		Component.text("Your inventory is unbounded in size")
	)),

	CLASSES("Classes", ::Classes, listOf(
		Component.text("Pick a class as the game begins"),
		Component.text("Get cool abilities")
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

	fun createQuirk(game: Game): Quirk {
		/* quirk instance from quirk type */
		val quirk = create(this, game)

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
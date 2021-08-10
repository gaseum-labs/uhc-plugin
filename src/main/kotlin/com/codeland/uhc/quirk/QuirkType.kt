package com.codeland.uhc.quirk

import com.codeland.uhc.core.Game
import com.codeland.uhc.event.Brew
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.quirk.quirks.*
import com.codeland.uhc.quirk.quirks.carePackages.CarePackages
import com.codeland.uhc.quirk.quirks.carePackages.ChaoticCarePackages
import com.codeland.uhc.quirk.quirks.classes.Classes
import org.bukkit.Material
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

enum class QuirkType(
	val prettyName: String,
	val create: (QuirkType, Game) -> Quirk,
	val representation: () -> ItemCreator,
	val description: Array<String>,
) {
	UNSHELTERED(
		"Unsheltered",
		::Unsheltered,
		{ ItemCreator.fromType(Material.SHULKER_SHELL) },
		arrayOf(
			"Terrain cannot be modified",
			"You cannot place or mine blocks",
			"But you still get the block loot"
        )
	),

    PESTS(
	    "Pests",
	    ::Pests,
	    { ItemCreator.fromType(Material.WOODEN_SWORD) },
	    arrayOf(
			"Dead players come back to exact their revenge",
			"But they are weak and have no access to advanced tools"
		)
    ),

	MODIFIED_DROPS(
		"Modified Drops",
		::ModifiedDrops,
		{ ItemCreator.fromType(Material.BONE) },
		arrayOf(
			"Hostile mobs drop exceptional loot"
		)
	),

    CREATIVE(
	    "Creative",
	    ::Creative,
	    { ItemCreator.fromType(Material.BRICK) },
	    arrayOf(
	        "you may place tough to get blocks without them emptying from your inventory"
		)
    ),

	SUMMONER(
		"Summoner",
		::Summoner,
		{ ItemCreator.fromType(Material.DONKEY_SPAWN_EGG) },
		arrayOf(
			"Mobs drop their spawn eggs when killed"
		)
	),

	RANDOM_EFFECTS(
		"Random Effects",
		::RandomEffects,
		{ Brew.createDefaultPotion(Material.POTION, PotionData(PotionType.STRENGTH)) },
		arrayOf(
			"Every 3 minutes,",
			"Everyone gets a random potion effect"
		)
	),

	LOW_GRAVITY(
		"Low Gravity",
		::LowGravity,
		{ ItemCreator.fromType(Material.CHORUS_FRUIT) },
		arrayOf(
			"Gravity is much lower than usual"
		)
	),

	HOTBAR(
		"Limited Inventory",
		::Hotbar,
		{ ItemCreator.fromType(Material.STRUCTURE_VOID) },
		arrayOf(
			"All players are limited to only",
			"their hotbar to store items"
  	    )
	),

	CARE_PACKAGES(
		"Care Packages",
		::CarePackages,
		{ ItemCreator.fromType(Material.CHEST_MINECART) },
		arrayOf(
			"Chests periodically drop containing good loot",
			"go there and you should expect a fight"
		)
	),

	CHAOTIC_CARE_PACKAGES(
		"Chaotic Care Packages",
		::ChaoticCarePackages,
		{ ItemCreator.fromType(Material.CHORUS_FRUIT) },
		arrayOf(
			"Chests drop every 5 seconds",
			"Wacky loot is inside"
		)
	),

	DEATHSWAP(
		"Deathswap",
		::Deathswap,
		{ ItemCreator.fromType(Material.MAGENTA_GLAZED_TERRACOTTA) },
		arrayOf(
			"Players switch places with each other",
			"at randomly chosen intervals"
		)
	),

	HALLOWEEN(
		"Halloween",
		::Halloween,
		{ ItemCreator.fromType(Material.PUMPKIN_PIE) },
		arrayOf(
			"Mobs drop candy",
			"Witches?!"
		)
	),

	PUMPKIN(
		"Pumpkin",
		::Pumpkin,
		{ ItemCreator.fromType(Material.PUMPKIN_SEEDS) },
		arrayOf(
			"You are forced to have a pumpkin on your head"
		)
	),

	CHRISTMAS(
		"Christmas",
		::Christmas,
		{ ItemCreator.fromType(Material.SNOWBALL) },
		arrayOf(
			"It's snowing all the time!"
		)
	),

	FLYING(
		"Flying",
		::Flying,
		{ ItemCreator.fromType(Material.FIREWORK_ROCKET) },
		arrayOf(
			"Start with an elytra and rockets"
		)
	),

	PLAYER_COMPASS(
		"Player Compasses",
		::PlayerCompass,
		{ ItemCreator.fromType(Material.COMPASS) },
		arrayOf(
			"Track down players with a special compass"
		)
	),

	INFINITE_INVENTORY(
		"Infinite Inventory",
		::InfiniteInventory,
		{ ItemCreator.fromType(Material.FEATHER) },
		arrayOf(
			"Your inventory is unbounded in size"
		)
	),

	CLASSES(
		"Classes",
		::Classes,
		{ ItemCreator.fromType(Material.NETHER_STAR) },
		arrayOf(
			"Pick a class as the game begins",
			"Get cool abilities"
		)
	);

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
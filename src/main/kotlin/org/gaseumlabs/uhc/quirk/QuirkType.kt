package org.gaseumlabs.uhc.quirk

import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.quirk.quirks.*
import org.gaseumlabs.uhc.quirk.quirks.carePackages.CarePackages
import org.gaseumlabs.uhc.quirk.quirks.carePackages.ChaoticCarePackages
import org.gaseumlabs.uhc.quirk.quirks.classes.Classes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

enum class QuirkType(
	val prettyName: String,
	val create: (QuirkType, Game) -> Quirk,
	val representation: () -> ItemCreator,
	val description: List<Component>,
) {
	UNSHELTERED(
		"Unsheltered",
		::Unsheltered,
		{ ItemCreator.fromType(Material.SHULKER_SHELL) },
		listOf(
			Component.text("Terrain cannot be modified"),
			Component.text("You cannot place or mine blocks"),
			Component.text("But you still get the block loot")
		)
	),

	PESTS(
		"Pests",
		::Pests,
		{ ItemCreator.fromType(Material.WOODEN_SWORD) },
		listOf(
			Component.text("Dead players come back to exact their revenge"),
			Component.text("But they are weak and have no access to advanced tools")
		)
	),

	CREATIVE(
		"Creative",
		::Creative,
		{ ItemCreator.fromType(Material.BRICK) },
		listOf(
			Component.text("you may place tough to get blocks without them emptying from your inventory")
		)
	),

	SUMMONER(
		"Summoner",
		::Summoner,
		{ ItemCreator.fromType(Material.DONKEY_SPAWN_EGG) },
		listOf(
			Component.text("Mobs drop their spawn eggs when killed")
		)
	),

	RANDOM_EFFECTS(
		"Random Effects",
		::RandomEffects,
		{ ItemCreator.fromType(Material.DRAGON_BREATH) },
		listOf(
			Component.text("Every 3 minutes,"),
			Component.text("Everyone gets a random potion effect")
		)
	),

	LOW_GRAVITY(
		"Low Gravity",
		::LowGravity,
		{ ItemCreator.fromType(Material.CHORUS_FRUIT) },
		listOf(
			Component.text("Gravity is much lower than usual")
		)
	),

	HOTBAR(
		"Limited Inventory",
		::Hotbar,
		{ ItemCreator.fromType(Material.STRUCTURE_VOID) },
		listOf(
			Component.text("All players are limited to only"),
			Component.text("their hotbar to store items")
		)
	),

	CARE_PACKAGES(
		"Care Packages",
		::CarePackages,
		{ ItemCreator.fromType(Material.CHEST_MINECART) },
		listOf(
			Component.text("Chests periodically drop containing good loot"),
			Component.text("go there and you should expect a fight")
		)
	),

	CHAOTIC_CARE_PACKAGES(
		"Chaotic Care Packages",
		::ChaoticCarePackages,
		{
			ItemCreator.fromType(Material.TIPPED_ARROW)
				.customMeta<PotionMeta> { it.basePotionData = PotionData(PotionType.INSTANT_HEAL, false, false) }
		},
		listOf(
			Component.text("Tons of chests drop throughout the world"),
			Component.text("Wacky loot is inside")
		)
	),

	DEATHSWAP(
		"Deathswap",
		::Deathswap,
		{ ItemCreator.fromType(Material.MAGENTA_GLAZED_TERRACOTTA) },
		listOf(
			Component.text("Players switch places with each other"),
			Component.text("at randomly chosen intervals")
		)
	),

	HALLOWEEN(
		"Halloween",
		::Halloween,
		{ ItemCreator.fromType(Material.PUMPKIN_PIE) },
		listOf(
			Component.text("Mobs drop candy"),
			Component.text("Witches?!")
		)
	),

	PUMPKIN(
		"Pumpkin",
		::Pumpkin,
		{ ItemCreator.fromType(Material.PUMPKIN_SEEDS) },
		listOf(
			Component.text("You are forced to have a pumpkin on your head")
		)
	),

	CHRISTMAS(
		"Christmas",
		::Christmas,
		{ ItemCreator.fromType(Material.SNOWBALL) },
		listOf(
			Component.text("It's snowing all the time!")
		)
	),

	FLYING(
		"Flying",
		::Flying,
		{ ItemCreator.fromType(Material.FIREWORK_ROCKET) },
		listOf(
			Component.text("Start with an elytra and rockets")
		)
	),

	PLAYER_COMPASS(
		"Player Compasses",
		::PlayerCompass,
		{ ItemCreator.fromType(Material.COMPASS) },
		listOf(
			Component.text("Track down players with a special compass")
		)
	),

	INFINITE_INVENTORY(
		"Infinite Inventory",
		::InfiniteInventory,
		{ ItemCreator.fromType(Material.FEATHER) },
		listOf(
			Component.text("Your inventory is unbounded in size")
		)
	),

	CLASSES(
		"Classes",
		::Classes,
		{ ItemCreator.fromType(Material.IRON_HELMET) },
		listOf(
			Component.text("Pick a class as the game begins"),
			Component.text("Get cool abilities")
		)
	),

	ACHIEVEMENTS(
		"Achievements",
		::Achievements,
		{ ItemCreator.fromType(Material.WRITABLE_BOOK) },
		listOf(
			Component.text("When you earn an achievement, you get"),
			Component.text("extra health based on its difficulty.")
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
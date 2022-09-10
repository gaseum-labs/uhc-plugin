package org.gaseumlabs.uhc.chc

import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.chc.chcs.*
import org.gaseumlabs.uhc.chc.chcs.carePackages.CarePackages
import org.gaseumlabs.uhc.chc.chcs.carePackages.ChaoticCarePackages
import org.gaseumlabs.uhc.chc.chcs.classes.Classes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhc.chc.chcs.banana.Banana

enum class CHCType(
	val prettyName: String,
	val create: () -> CHC<*>,
	val representation: () -> ItemCreator,
	val description: List<Component>,
) {
	UNSHELTERED(
		"Unsheltered",
		::Unsheltered,
		{ ItemCreator.display(Material.SHULKER_SHELL) },
		listOf(
			Component.text("Terrain cannot be modified"),
			Component.text("You cannot place or mine blocks"),
			Component.text("But you still get the block loot")
		)
	),

	PESTS(
		"Pests",
		::Pests,
		{ ItemCreator.display(Material.WOODEN_SWORD) },
		listOf(
			Component.text("Dead players come back to exact their revenge"),
			Component.text("But they are weak and have no access to advanced tools")
		)
	),

	SUMMONER(
		"Summoner",
		::Summoner,
		{ ItemCreator.display(Material.DONKEY_SPAWN_EGG) },
		listOf(
			Component.text("Mobs drop their spawn eggs when killed")
		)
	),

	CARE_PACKAGES(
		"Care Packages",
		::CarePackages,
		{ ItemCreator.display(Material.CHEST_MINECART) },
		listOf(
			Component.text("Chests periodically drop containing good loot"),
			Component.text("go there and you should expect a fight")
		)
	),

	CHAOTIC_CARE_PACKAGES(
		"Chaotic Care Packages",
		::ChaoticCarePackages,
		{
			ItemCreator.display(Material.TIPPED_ARROW)
				.customMeta<PotionMeta> { it.basePotionData = PotionData(PotionType.INSTANT_HEAL, false, false) }
		},
		listOf(
			Component.text("Tons of chests drop throughout the world"),
			Component.text("Wacky loot is inside")
		)
	),

	FLOATING_ISLANDS(
		"Floating Islands",
		::FloatingIslands,
		{ ItemCreator.display(Material.FIREWORK_ROCKET) },
		listOf(
			Component.text("Start with an elytra and rockets")
		)
	),

	CLASSES(
		"Classes",
		::Classes,
		{ ItemCreator.display(Material.IRON_HELMET) },
		listOf(
			Component.text("Pick a class as the game begins"),
			Component.text("Get cool abilities")
		)
	),

	BANANA(
		"Banana",
		::Banana,
		{ ItemCreator.display(Material.GOLDEN_PICKAXE) },
		listOf(
			Component.text("Banana")
		)
	);
}
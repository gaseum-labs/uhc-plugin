package com.codeland.uhc.world

import net.kyori.adventure.text.Component
import org.bukkit.Material

enum class WorldGenOption(
	val prettyName: String,
	val defaultEnabled: Boolean,
	val description: List<Component>,
	val representation: Material
) {
	NETHER_FIX("Nether Fix", true, listOf(
		Component.text("Nether wart and blazes spawn randomly in the nether")
	), Material.NETHER_WART),

	MUSHROOM_FIX("Mushroom Fix", true, listOf(
		Component.text("Oxeye daisy generation reduced"),
		Component.text("Giant mushroom drop rate reduced"),
		Component.text("Mushroom generation in caves increased")
	), Material.RED_MUSHROOM),

	ORE_FIX("Ore Fix", true, listOf(
		Component.text("Gold, lapis, and diamond only generate on the sides of caves"),
	), Material.DIAMOND),

	MELON_FIX("Melon Fix", true, listOf(
		Component.text("Melons are hidden in jungles")
	), Material.MELON_SLICE),

	SUGAR_CANE_REGEN("Sugar Cane Regen", true, listOf(
		Component.text("Sugar cane generates as the game goes on"),
	), Material.SUGAR),

	SUGAR_CANE_FIX("Sugar Cane Fix", false, listOf(
		Component.text("Sugar cane generate is spread out"),
		Component.text("Sugar cane always generates 3 at a time")
	), Material.SUGAR_CANE),

	CAVE_INDICATORS("Cave Indicators", false, listOf(
		Component.text("Minerals underground point you toward caves"),
	), Material.DIORITE),

	DUNGEON_FIX("Dungeon Fix", false, listOf(
		Component.text("Loot in dungeon chest is normalized")
	), Material.MOSSY_COBBLESTONE),

	NETHER_INDICATORS("Nether Indicators", false, listOf(
		Component.text("Nether blocks generate below y level 15"),
		Component.text("Indicates the corresponding biome in the nether")
	), Material.SOUL_SOIL),

	HALLOWEEN("Halloween", false, listOf(
		Component.text("Adds spooky decorations to the world"),
	), Material.JACK_O_LANTERN),

	CHRISTMAS("Christmas", false, listOf(
		Component.text("Covers the world in snow"),
	), Material.SNOWBALL),

	CHUNK_BIOMES("Chunk Biomes", false, listOf(
		Component.text("Each chunk is a different biome"),
	), Material.DEAD_BUSH),

	AMPLIFIED("Amplified", false, listOf (
		Component.text("Incredibly mountainous terrain")
	), Material.ACACIA_SAPLING),

	REVERSE_ORE_FIX("Reverse Ore Fix", false, listOf(
	Component.text("Ores generate at the top of the world")
	), Material.GOLD_INGOT);
}

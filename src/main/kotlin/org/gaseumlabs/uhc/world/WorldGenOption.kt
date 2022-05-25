package org.gaseumlabs.uhc.world

import net.kyori.adventure.text.Component
import org.bukkit.Material

enum class WorldGenOption(
	val prettyName: String,
	val defaultEnabled: Boolean,
	val description: List<Component>,
	val representation: Material,
) {
	CHUNK_BIOMES("Chunk Biomes", false, listOf(
		Component.text("Each chunk is a different biome"),
	), Material.DEAD_BUSH),

	AMPLIFIED("Amplified", false, listOf(
		Component.text("Incredibly mountainous terrain")
	), Material.ACACIA_SAPLING),

	TOWERS("Towers", false, listOf(
		Component.text("Tall dungeons generate across the world")
	), Material.IRON_BARS),

	HALLOWEEN("Halloween", false, listOf(
		Component.text("Adds spooky decorations to the world"),
	), Material.JACK_O_LANTERN),

	CHRISTMAS("Christmas", false, listOf(
		Component.text("Covers the world in snow"),
	), Material.SNOWBALL);
}

package com.codeland.uhc.core

import com.codeland.uhc.gui.GuiItem
import com.codeland.uhc.gui.GuiItemProperty
import com.codeland.uhc.gui.GuiManager
import com.codeland.uhc.gui.GuiPage
import com.codeland.uhc.quirk.BoolToggle
import com.codeland.uhc.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minecraft.server.v1_16_R3.BiomeBase
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

enum class WorldGenOption(
	val prettyName: String,
	val property: UHCProperty<Boolean>,
	val description: List<Component>,
	val representation: Material
) {
	NETHER_FIX("Nether Fix", UHCProperty(true), listOf(
		Component.text("Nether wart and blazes spawn randomly in the nether")
	), Material.NETHER_WART),

	MUSHROOM_FIX("Mushroom Fix", UHCProperty(true), listOf(
		Component.text("Oxeye daisy generation reduced"),
		Component.text("Giant mushroom drop rate reduced"),
		Component.text("Mushroom generation in caves increased")
	), Material.RED_MUSHROOM),

	ORE_FIX("Ore Fix", UHCProperty(true), listOf(
		Component.text("Gold, lapis, and diamond only generate on the sides of caves"),
		Component.text("Mineral indicators help find caves")
	), Material.DIAMOND),

	MELON_FIX("Melon Fix", UHCProperty(true), listOf(
		Component.text("Melons generate in all biomes")
	), Material.MELON_SLICE),

	DUNGEON_FIX("Dungeon Fix", UHCProperty(true), listOf(
		Component.text("Loot in dungeon chest is normalized")
	), Material.MOSSY_COBBLESTONE),

	SUGAR_CANE_FIX("Sugar Cane Fix", UHCProperty(true), listOf(
		Component.text("Sugar cane generate is spread out"),
		Component.text("Sugar cane always generates 3 at a time")
	), Material.SUGAR_CANE),

	NETHER_INDICATORS("Nether Indicators", UHCProperty(true), listOf(
		Component.text("Nether blocks generate below y level 15"),
		Component.text("Indicates the corresponding biome in the nether")
	), Material.SOUL_SOIL),

	HALLOWEEN("Halloween", UHCProperty(false), listOf(
		Component.text("Adds spooky decorations to the world"),
	), Material.JACK_O_LANTERN),

	CHRISTMAS("Christmas", UHCProperty(false), listOf(
		Component.text("Covers the world in snow"),
	), Material.SNOWBALL),

	CHUNK_BIOMES("Chunk Biomes", UHCProperty(false), listOf(
		Component.text("Each chunk is a different biome"),
	), Material.DEAD_BUSH);

	companion object {
		var centerBiome: Biome? = null

		fun getEnabled(worldGenOption: WorldGenOption): Boolean {
			return worldGenOption.property.get()
		}

		val worldGenGui = GuiManager.register(object : GuiPage(5, Util.gradientString("World Gen Options", TextColor.color(0x238006), TextColor.color(0x788f61))) {
			init {
				values().forEachIndexed { i, option ->
					addItem(object : GuiItemProperty <Boolean> (i, option.property) {
						override fun onClick(player: Player, shift: Boolean) {
							if (!UHC.isGameGoing()) option.property.set(!option.property.get())
						}

						override fun getStackProperty(value: Boolean): ItemStack {
							val item = lore(
								name(ItemStack(option.representation), enabledName(option.prettyName, value)),
								option.description
							)

							return if (value) enchant(item) else item
						}
					})
				}

				addItem(object : GuiItem(coords(7, 4)) {
					override fun onClick(player: Player, shift: Boolean) {
						if (!UHC.isGameGoing()) values().forEach { it.property.reset() }
					}
					override fun getStack() = name(ItemStack(Material.MUSIC_DISC_WAIT), "${ChatColor.AQUA}Reset")
				})

				addItem(object : GuiItem(coords(8, 4)) {
					override fun onClick(player: Player, shift: Boolean) = if (shift) player.closeInventory() else UHC.setupGui.open(player)
					override fun getStack() = name(ItemStack(Material.PRISMARINE_SHARD), "${ChatColor.BLUE}Back")
				})
			}
		})
	}
}

package org.gaseumlabs.uhc.chc.chcs.banana

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.KeyGen
import kotlin.math.pow

val KEY_BANANA_TYPE = KeyGen.genKey("banana_type")

enum class BananaType(val color: TextColor, val creator: ItemCreator) {
	REGULAR(
		TextColor.color(0xedb92b),
		ItemCreator.display(Material.GOLDEN_HOE)
		.lore(listOf(
			Component.text("Craftable into the UHC Super Banana"),
			Component.text("+1 banana points"),
			Component.text("Ability: gain random materials"),
		))
	),
	SUPER(
		TextColor.color(0xeda32b),
		ItemCreator.display(Material.GOLDEN_SHOVEL)
		.lore(listOf(
			Component.text("Craftable into the UHC Mega Banana"),
			Component.text("+9 banana points"),
			Component.text("Ability: teleport away"),
		))
	),
	MEGA(
		TextColor.color(0xed7f2b),
		ItemCreator.display(Material.GOLDEN_PICKAXE)
		.lore(listOf(
			Component.text("Craftable into the UHC Hyper Banana"),
			Component.text("+81 banana points"),
			Component.text("Ability: dig a super hole"),
		))
	),
	HYPER(
		TextColor.color(0xed6c2b),
		ItemCreator.display(Material.GOLDEN_AXE)
		.lore(listOf(
			Component.text("Craftable into the UHC Giga Banana"),
			Component.text("+4 banana points"),
			Component.text("Ability: build a bridge/fort"),
		))
	),
	GIGA(
		TextColor.color(0xed582b),
		ItemCreator.display(Material.GOLDEN_SWORD)
		.lore(listOf(
			Component.text("The toppest tier banana"),
			Component.text("+5 banana points"),
			Component.text("Ability: give your opponents a bad time"),
		))
	);

	val recipeKey = KeyGen.genKey("banana_recipe_${name.lowercase()}")

	fun text(string: String) = Component.text(string, color, TextDecoration.BOLD)

	val displayName = if (this.ordinal == 0)
		"UHC Banana"
	else "UHC ${name.lowercase().replaceFirstChar { it.uppercase() }} Banana"

	init {
		creator.name(text(displayName))
		creator.setData(KEY_BANANA_TYPE, ordinal)
		creator.enchant(Enchantment.ARROW_INFINITE, 1)
	}

	val points = 9.0f.pow(ordinal).toInt()

	fun create() = creator.create()

	companion object {
		fun getBananaType(stack: ItemStack?): BananaType? {
			if (stack == null) return null
			return BananaType.values()[
				(stack.itemMeta as PersistentDataHolder)
					.persistentDataContainer
					.get(KEY_BANANA_TYPE, PersistentDataType.INTEGER)
						?: return null
			]
		}

		fun genRecipe(type: BananaType) =
			ShapedRecipe(type.recipeKey, BananaType.values()[type.ordinal + 1].create())
				.shape("EEE","EEE","EEE")
				.setIngredient('E', type.create())
	}
}
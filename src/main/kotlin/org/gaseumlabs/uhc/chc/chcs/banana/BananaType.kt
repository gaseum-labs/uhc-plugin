package org.gaseumlabs.uhc.chc.chcs.banana

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BlockVector
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.KeyGen
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.Util
import kotlin.math.pow
import kotlin.random.Random
import kotlin.random.nextInt

val KEY_BANANA_TYPE = KeyGen.genKey("banana_type")

enum class BananaType(val color: TextColor, val creator: ItemCreator, val ability: (Player) -> Unit) {
	REGULAR(
		TextColor.color(0xedb92b),
		ItemCreator.display(Material.GOLDEN_HOE)
		.lore(listOf(
			Component.text("Craftable into the UHC Super Banana"),
			Component.text("+1 banana points"),
			Component.text("Ability: gain random materials"),
		)),
		{ player ->
			Util.addOrDrop(player, BananaUtil.scrapMaterials.random().let {
				(material, count) -> ItemStack(material, count)
			})
		}
	),
	SUPER(
		TextColor.color(0xeda32b),
		ItemCreator.display(Material.GOLDEN_SHOVEL)
		.lore(listOf(
			Component.text("Craftable into the UHC Mega Banana"),
			Component.text("+4 banana points"),
			Component.text("Ability: teleport away"),
		)),
		ability@{ player ->
			val centerBlock = player.location.block
			for (i in 0 until 128) {
				val x = centerBlock.x + Util.randomMirrored(16 until 32)
				val y = centerBlock.x + Random.nextInt(-32.. 32)
				val z = centerBlock.x + Util.randomMirrored(16 until 32)

				val tryBlock = centerBlock.getRelative(x, y, z)
				if (
					tryBlock.getRelative(BlockFace.DOWN).isSolid &&
					tryBlock.isPassable &&
					tryBlock.getRelative(BlockFace.UP).isPassable
				) {
					player.teleport(tryBlock.location.add(0.5, 0.0, 0.5).setDirection(player.location.direction))
					return@ability
				}
			}
			Commands.errorMessage(player, "Could not teleport")
		}
	),
	MEGA(
		TextColor.color(0xed7f2b),
		ItemCreator.display(Material.GOLDEN_PICKAXE)
		.lore(listOf(
			Component.text("Craftable into the UHC Hyper Banana"),
			Component.text("+16 banana points"),
			Component.text("Ability: dig a super hole"),
		)),
		{ player ->
			val world = player.world
			val direction = player.location.direction
			val start = player.eyeLocation.toVector()
			val pickaxe = ItemStack(Material.IRON_PICKAXE)

			SchedulerUtil.delayedFor(10, 1..10) { i ->
				val center = world.getBlockAt(start.add(direction.multiply(i * 3)).toLocation(world))
				for (x in -2 until 2) {
					for (y in -2 until 2) {
						for (z in -2 until 2) {
							val block = center.getRelative(x, y, z)
							if (!block.type.isAir) block.breakNaturally(pickaxe)
						}
					}
				}
			}
		}
	),
	HYPER(
		TextColor.color(0xed6c2b),
		ItemCreator.display(Material.GOLDEN_AXE)
		.lore(listOf(
			Component.text("Craftable into the UHC Giga Banana"),
			Component.text("+64 banana points"),
			Component.text("Ability: build a bridge/fort"),
		)),
		{ player ->
			
		}
	),
	GIGA(
		TextColor.color(0xed582b),
		ItemCreator.display(Material.GOLDEN_SWORD)
		.lore(listOf(
			Component.text("The toppest tier banana"),
			Component.text("+256 banana points"),
			Component.text("Ability: give your opponents a bad time"),
		)),
		{ player ->

		}
	),

	HEATER(
		TextColor.color(0xedb92b),
		ItemCreator.display(Material.GOLDEN_HOE)
			.lore(listOf(
				Component.text("+1 banana points"),
				Component.text("Ability: smelt a random stack in your inventory"),
			)),
		{ player ->

		}
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

		fun genRecipe(type: BananaType, to: BananaType) =
			ShapedRecipe(type.recipeKey, to.create())
				.shape("EE","EE")
				.setIngredient('E', type.create())
	}
}
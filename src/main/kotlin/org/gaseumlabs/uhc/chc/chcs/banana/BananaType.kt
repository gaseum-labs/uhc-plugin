package org.gaseumlabs.uhc.chc.chcs.banana

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.gaseumlabs.uhc.command.Commands
import org.gaseumlabs.uhc.gui.ItemCreator
import org.gaseumlabs.uhc.util.KeyGen
import org.gaseumlabs.uhc.util.SchedulerUtil
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.Util.comma
import org.gaseumlabs.uhc.util.extensions.VectorExtensions.plus
import org.gaseumlabs.uhc.util.extensions.VectorExtensions.times
import kotlin.random.Random

class RecipeCreator(val lines: Array<String>, val ingredients: Array<Pair<Char, ItemStack>>)

val KEY_BANANA_TYPE = KeyGen.genKey("banana_type")

val bananaRegistry = ArrayList<BananaType>()

fun register(bananaType: BananaType): Int {
	bananaRegistry.add(bananaType)
	return bananaRegistry.size - 1
}

abstract class BananaType(
	val name: String,
	val points: Int,
	val cooldown: Int,
	val color: TextColor,
	val material: Material,
	val description: String?,
	recipeCreator: RecipeCreator?,
	val overrideName: Boolean = false
) {
	val id = register(this)

	abstract fun ability(player: Player): Boolean?

	fun text(string: String) = Component.text(string, color, TextDecoration.BOLD)

	open fun displayName() = if (overrideName) name else "UHC $name Banana"

	private val creator = ItemCreator.display(material)
		.name(text(displayName()))
		.setData(KEY_BANANA_TYPE, id)
		.lore(listOfNotNull(
			Component.text("+${points} banana points"),
			description?.let { Component.text("Ability: $description") }
		))
		.unbreakable()
		.enchant(Enchantment.ARROW_INFINITE, 1)

	fun create() = creator.create()

	val recipeKey = KeyGen.genKey("banana_recipe_${name.lowercase().replace(" ", "_")}")
	val recipe = recipeCreator?.let {
		val recipe = ShapedRecipe(recipeKey, create())
		recipe.shape(*recipeCreator.lines)
		recipeCreator.ingredients.forEach { (c, i) -> recipe.setIngredient(c, i) }
		recipe
	}

	companion object {
		fun getBananaType(stack: ItemStack?): BananaType? {
			if (stack == null) return null
			return bananaRegistry[
				(stack.itemMeta as PersistentDataHolder)
					.persistentDataContainer
					.get(KEY_BANANA_TYPE, PersistentDataType.INTEGER)
					?: return null
			]
		}
	}
}

val REGULAR = object : BananaType(
	"Regular",
	1,
    0,
	TextColor.color(0xedb92b),
	Material.GOLDEN_HOE,
	null,
	null
) {
	override fun displayName() = "UHC Banana"
	override fun ability(player: Player) = null
}

val SCRAP = object : BananaType(
	"Scrap",
	2,
		10,
	TextColor.color(0xedb92b),
	Material.GOLDEN_HOE,
	"gain random materials",
	RecipeCreator(
		arrayOf("LSE", "OBG", "DCA"),
		arrayOf(
			'L' to ItemStack(Material.OAK_LEAVES),
			'S' to ItemStack(Material.OAK_SAPLING),
			'E' to ItemStack(Material.WHEAT_SEEDS),
			'O' to ItemStack(Material.OAK_LOG),
			'B' to REGULAR.create(),
			'G' to ItemStack(Material.GRASS),
			'D' to ItemStack(Material.DIRT),
			'C' to ItemStack(Material.COBBLESTONE),
			'A' to ItemStack(Material.COAL),
		)
	)
) {
	override fun ability(player: Player): Boolean {
		Util.addOrDrop(player, BananaUtil.scrapMaterials.random().let {
				(material, count) -> ItemStack(material, count)
		})
		return true
	}
}

val SMELTER = object : BananaType(
	"Smelter",
	2,
		10,
	TextColor.color(0xe62d20),
	Material.GOLDEN_HOE,
	"smelt a random stack in your inventory",
	RecipeCreator(
		arrayOf("L","B","L"),
		arrayOf(
			'L' to ItemStack(Material.LAVA_BUCKET),
			'B' to REGULAR.create()
		)
	)
) {
	override fun ability(player: Player): Boolean {
		val result = player.inventory.contents.mapIndexedNotNull { i, item ->
			item?.let { i to (BananaUtil.smelts2[it.type] ?: return@mapIndexedNotNull null)  }
		}.randomOrNull()?.let { (i, result) ->
			player.inventory.setItem(i, result.asQuantity(player.inventory.getItem(i)!!.amount))
		}

		if (result == null) {
			Commands.errorMessage(player, "Do not have an stack to smelt")
			return false
		}

		return true
	}
}

val LOGGING = object : BananaType(
	"Logging",
	2,
		10,
	TextColor.color(0xe62d20),
	Material.GOLDEN_AXE,
	"chop down entire trees around you",
	RecipeCreator(
		arrayOf(" X ","XBX"," X "),
		arrayOf(
			'X' to ItemStack(Material.IRON_AXE),
			'B' to REGULAR.create()
		)
	)
) {
	override fun ability(player: Player): Boolean {
		val lookingAt = player.rayTraceBlocks(10.0)?.hitBlock
			?: return Commands.errorMessage(player, "Not looking at a tree").comma(false)

		if (!BananaUtil.isLog(lookingAt))
			return Commands.errorMessage(player, "Not looking at a tree").comma(false)

		var positionsList = mutableSetOf(lookingAt)

		SchedulerUtil.delayedFor(5, 0 until 24) {
			if (positionsList.isEmpty()) return@delayedFor

			val arounds = positionsList.flatMap {
				arrayListOf(
					it.getRelative(BlockFace.UP),
					it.getRelative(BlockFace.DOWN),
					it.getRelative(BlockFace.EAST),
					it.getRelative(BlockFace.WEST),
					it.getRelative(BlockFace.NORTH),
					it.getRelative(BlockFace.SOUTH)
				)
			}.toMutableSet()

			arounds.removeIf { !BananaUtil.isLog(it) }

			arounds.forEach { log -> log.breakNaturally() }

			positionsList = arounds
		}

		return true
	}
}

val SUPER = object : BananaType(
	"Super",
	4,
		0,
	TextColor.color(0xeda32b),
	Material.GOLDEN_SHOVEL,
	null,
	RecipeCreator(arrayOf("EE", "EE"), arrayOf('E' to REGULAR.create())),
) {
	override fun ability(player: Player) = null
}


val TELEPORT = object : BananaType(
	"Teleport",
	5,
		10,
	TextColor.color(0xeda32b),
	Material.GOLDEN_SHOVEL,
	"teleport away",
	RecipeCreator(
		arrayOf("PBP"),
		arrayOf(
			'P' to ItemStack(Material.ENDER_PEARL),
			'B' to SUPER.create()
		)
	),
) {
	override fun ability(player: Player): Boolean {
		val centerBlock = player.location.block
		for (i in 0 until 96) {
			val x = centerBlock.x + Util.randomMirrored(16 until 32)
			val z = centerBlock.x + Util.randomMirrored(16 until 32)

			if (!player.world.worldBorder.isInside(
					Location(player.world, x.toDouble(), 0.0, z.toDouble())
				)) continue

			val startY = Random.nextInt(32)
			for (yy in 0 until 32) {
				val y = ((startY + yy) % 32) - 16

				val tryBlock = centerBlock.getRelative(x, y, z)
				if (
					tryBlock.getRelative(BlockFace.DOWN).isSolid &&
					tryBlock.isPassable &&
					tryBlock.getRelative(BlockFace.UP).isPassable
				) {
					player.teleport(tryBlock.location.add(0.5, 0.0, 0.5).setDirection(player.location.direction))
					return true
				}
			}
		}
		Commands.errorMessage(player, "Could not teleport")
		return false
	}
}

val MAZE = object : BananaType(
	"Maze",
	5,
		10,
	TextColor.color(0xeda32b),
	Material.GOLDEN_SHOVEL,
	"get lost in a maze",
	RecipeCreator(
		arrayOf("DDD", "DBD", "DDD"),
		arrayOf(
			'B' to SUPER.create(),
			'D' to ItemStack(Material.OAK_DOOR)
		)
	),
) {
	override fun ability(player: Player): Boolean {
		val centerBlock = player.location.block
		for (x in -12 until 12) {
			for (z in -12 until 12) {
				for (y in -2 until 2) {
					val block = centerBlock.getRelative(x, y, z)
					val above = block.getRelative(BlockFace.UP)

					if (
						block.getRelative(BlockFace.DOWN).isSolid &&
						block.isPassable &&
						above.isPassable
					) {
						val blockData = Material.CRIMSON_DOOR.createBlockData() as Door
						blockData.facing = BananaUtil.randomFacing()
						blockData.hinge = if (Random.nextBoolean()) Door.Hinge.LEFT else Door.Hinge.RIGHT
						blockData.isOpen = Random.nextBoolean()
						blockData.half = Bisected.Half.BOTTOM
						block.setBlockData(blockData, false)

						blockData.half = Bisected.Half.TOP
						above.setBlockData(blockData, false)
					}
				}
			}
		}
		return true
	}
}

val MINER = object : BananaType(
	"Miner",
	5,
		10,
	TextColor.color(0xed7f2b),
	Material.GOLDEN_PICKAXE,
	"dig a super hole",
	RecipeCreator(
		arrayOf(" P ", "PBP", " P "),
		arrayOf(
			'P' to ItemStack(Material.IRON_PICKAXE),
			'B' to SUPER.create(),
		)
	),
) {
	override fun ability(player: Player): Boolean {
		val world = player.world
		val direction = player.location.direction
		val start = player.eyeLocation.toVector()
		val pickaxe = ItemStack(Material.IRON_PICKAXE)

		SchedulerUtil.delayedFor(10, 1..10) { i ->
			val center = world.getBlockAt(start.clone().add(direction.clone().multiply(i * 3)).toLocation(world))
			for (x in -2 until 2) {
				for (y in -2 until 2) {
					for (z in -2 until 2) {
						val block = center.getRelative(x, y, z)
						if (!block.type.isAir) block.breakNaturally(pickaxe)
					}
				}
			}
		}

		return true
	}
}

val MEGA = object : BananaType(
	"Mega",
	16,
		0,
	TextColor.color(0xed7f2b),
	Material.GOLDEN_PICKAXE,
	null,
	RecipeCreator(arrayOf("EE", "EE"), arrayOf('E' to SUPER.create())),
) {
	override fun ability(player: Player) = null
}

val ROCKET = object : BananaType(
		"Rocket",
		5,
		20,
		TextColor.color(0xff0000),
		Material.GOLDEN_SWORD,
		"launches you in the direction you're facing",
		RecipeCreator(
				arrayOf(" B ", "GGG"),
				arrayOf(
						'B' to SUPER.create(),
						'G' to ItemStack(Material.GUNPOWDER)
				)
		),
) {
	override fun ability(player: Player): Boolean {
		player.world.spawnParticle(
				Particle.EXPLOSION_HUGE,
				player.location,
				5
		)
		player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f)
		player.velocity += player.location.direction * 5.0
		BananaManager.launched += player
		return true
	}
}

val BANANARANG = object : BananaType(
		"Bananarang",
		5,
		0,
		TextColor.color(0xeda32b), // todo: change
		Material.GOLDEN_PICKAXE,
		"will always come back to you",
		RecipeCreator(
				arrayOf(" B ", "  B", " B "),
				arrayOf(
						'B' to SUPER.create()
				)
		),
		true,
) {
	override fun ability(player: Player): Boolean {
		BananaManager.createBananarang(player)
		return true
	}
}

val MAGIC = object : BananaType(
		"Magic",
		5,
		120,
		NamedTextColor.LIGHT_PURPLE,
		Material.GOLDEN_PICKAXE,
		"gives you 3 random positive potion effects",
		RecipeCreator(
				arrayOf("DDD", "DBD", "DDD"),
				arrayOf(
						'B' to SUPER.create(),
						'D' to ItemStack(Material.GLOWSTONE_DUST)
				)
		),
) {
	override fun ability(player: Player): Boolean {
		listOf(
				PotionEffectType.ABSORPTION,
				PotionEffectType.SPEED,
				PotionEffectType.FAST_DIGGING,
				PotionEffectType.JUMP,
				PotionEffectType.FIRE_RESISTANCE,
				PotionEffectType.DAMAGE_RESISTANCE,
				PotionEffectType.INVISIBILITY,
				PotionEffectType.SATURATION,
		).shuffled().take(3).forEach { effectType ->
			player.addPotionEffect(
					PotionEffect(
							effectType,
							60 * 20,
							Random.nextInt(3)
					)
			)
		}
		return true
	}
}

val BANANA_PHONE = object : BananaType(
		"Banana Phone",
		5,
		0,
		NamedTextColor.YELLOW,
		Material.GOLD_INGOT,
		"???",
		RecipeCreator(
				arrayOf("DDD", "DBD", "DDD"),
				arrayOf(
						'B' to REGULAR.create(),
						'D' to ItemStack(Material.REDSTONE)
				)
		),
		true,
) {
	override fun ability(player: Player): Boolean {
		BananaManager.onBananaPhoneRightClick(player)
		return false
	}
}

val recipes = bananaRegistry.mapNotNull { it.recipe }
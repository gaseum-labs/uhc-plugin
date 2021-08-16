package com.codeland.uhc.quirk.quirks.carePackages

import com.codeland.uhc.event.Brew
import com.codeland.uhc.event.Brew.Companion.POISON_INFO
import com.codeland.uhc.event.Brew.Companion.REGEN_INFO
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import kotlin.math.*
import kotlin.random.Random

object CarePackageUtil {
	/* generation */
	data class SpireData(val ore: Material, val block: Material)

	val SPIRE_COAL = SpireData(COAL_ORE, COAL_BLOCK)
	val SPIRE_IRON = SpireData(IRON_ORE, IRON_BLOCK)
	val SPIRE_LAPIS = SpireData(LAPIS_ORE, LAPIS_BLOCK)
	val SPIRE_GOLD = SpireData(GOLD_ORE, GOLD_BLOCK)
	val SPIRE_DIAMOND = SpireData(DIAMOND_ORE, DIAMOND_ORE)

	fun generateSpire(world: World, block: Block, maxRadius: Float, height: Int, spireData: SpireData) {
		val magnitudeField = Array(9) { (Math.random() * 0.2 + 0.9).toFloat() }

		fun fillBlock(block: Block) {
			val random = Math.random()

			block.setType(when {
				random < 1 / 16.0 -> spireData.ore
				random < 1 / 5.0 -> ANDESITE
				else -> STONE
			}, false)
		}

		fun isSpireBlock(block: Block): Boolean {
			return block.type == STONE || block.type == ANDESITE || block.type == spireData.ore
		}

		fun fillCircle(radius: Float, y: Int, magnitudeHeight: Float, allowHangers: Boolean, onBlock: (Block) -> Unit) {
			val intRadius = ceil(radius).toInt()
			val boundingSize = intRadius * 2 + 1

			for (i in 0 until boundingSize * boundingSize) {
				val offX = (i % boundingSize) - intRadius
				val offZ = ((i / boundingSize) % boundingSize) - intRadius

				val angle = (atan2(offZ.toDouble(), offX.toDouble()) + PI).toFloat()

				val blockRadius = radius * Util.bilinear2D(magnitudeField, 3, 3, angle / (PI.toFloat() * 2.0f), magnitudeHeight)

				if (sqrt(offX.toDouble().pow(2) + offZ.toDouble().pow(2)) < blockRadius) {
					val circleBlock = world.getBlockAt(block.x + offX, y, block.z + offZ)

					if (allowHangers || isSpireBlock(circleBlock.getRelative(BlockFace.DOWN))) onBlock(circleBlock)
				}
			}
		}

		for (y in block.y - 1 downTo 0) {
			var allFilled = true

			fillCircle(maxRadius, y, 0.0f, true) { circleBlock ->
				if (circleBlock.isPassable) allFilled = false
				fillBlock(circleBlock)
			}

			if (allFilled) break
		}

		for (y in 0 until height) {
			val along = y / (height - 1).toFloat()
			val usingRadius = Util.interp(1.0f, maxRadius, 1 - along)

			fillCircle(usingRadius, block.y + y, along, y == 0) { circleBlock ->
				fillBlock(circleBlock)
			}
		}

		world.getBlockAt(block.x, block.y + height, block.z).setType(spireData.block, false)
	}

	fun generateChest(world: World, block: Block, color: ChatColor): Inventory {
		/* create the chest */
		block.breakNaturally()
		block.type = CHEST

		/* set the name of the chest */
		val chest = block.getState(false) as Chest
		chest.customName = "$color${ChatColor.BOLD}Care Package"

		/* blast a firework right above the chest */
		val firework = world.spawnEntity(block.location.add(0.5, 1.0, 0.5), EntityType.FIREWORK) as Firework

		val meta = firework.fireworkMeta
		meta.addEffect(ItemUtil.fireworkEffect(FireworkEffect.Type.BALL_LARGE, 3))
		meta.power = 2
		firework.fireworkMeta = meta

		return chest.blockInventory
	}

	fun dropBlock(world: World, x: Int, z: Int): Block {
		val (liquidY, solidY) = Util.topLiquidSolidY(world, x, z)

		return if (liquidY != -1) {
			val waterBlock = world.getBlockAt(x, liquidY, z)
			waterBlock.setType(STONE, false)

			/* chest block is one above the water */
			waterBlock.getRelative(BlockFace.UP)

		} else {
			/* chest block is one above the ground */
			world.getBlockAt(x, solidY + 1, z)
		}
	}

	/* items */

	data class ItemPossibilities(val materials: Array<Material>, val amounts: Array<Array<Int>>)

	fun randomItem(possibilities: ItemPossibilities): ItemStack {
		val materialIndex = Util.randRange(0, possibilities.materials.lastIndex)
		return ItemStack(possibilities.materials[materialIndex], Util.randFromArray(possibilities.amounts[materialIndex]))
	}

	fun randomItem(items: Array<Material>, vararg amounts: Array<Int>): ItemStack {
		val index = (Math.random() * items.size).toInt()

		return ItemStack(items[index], Util.randFromArray(amounts[index]))
	}

	fun randomItem(material: Material, vararg amounts: Int): ItemStack {
		return ItemStack(material, amounts[(Math.random() * amounts.size).toInt()])
	}

	private val bottlePossibilities = ItemPossibilities(arrayOf(SAND, GLASS, GLASS_BOTTLE), arrayOf(arrayOf(6, 9), arrayOf(3, 6, 9), arrayOf(3, 6)))

	fun randomBottlePart(): ItemStack {
		return randomItem(bottlePossibilities)
	}

	private val stewPossibilities = ItemPossibilities(arrayOf(RED_MUSHROOM, BROWN_MUSHROOM, OXEYE_DAISY), arrayOf(arrayOf(3,5), arrayOf(3,5), arrayOf(3,5)))

	fun randomStewPart(): ItemStack {
		return randomItem(stewPossibilities)
	}

	private val ingredientPossibilities = arrayOf(
		ItemPossibilities(arrayOf(GLISTERING_MELON_SLICE, GHAST_TEAR), arrayOf(arrayOf(1, 2), arrayOf(2))),
		ItemPossibilities(arrayOf(SPIDER_EYE, FERMENTED_SPIDER_EYE), arrayOf(arrayOf(1, 2), arrayOf(2), arrayOf(1, 2))),
		ItemPossibilities(arrayOf(MAGMA_CREAM), arrayOf(arrayOf(2))),
		ItemPossibilities(arrayOf(BLAZE_POWDER), arrayOf(arrayOf(1)))
	)

	fun randomBrewingIngredient(): ItemStack {
		return randomItem(Util.randFromArray(ingredientPossibilities))
	}

	fun regenerationStew(): ItemStack {
		val stew = ItemStack(SUSPICIOUS_STEW)

		val meta = stew.itemMeta as SuspiciousStewMeta
		meta.addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 0), true)
		stew.itemMeta = meta

		return stew
	}

	fun randomBucket(): ItemStack {
		return when {
			Math.random() < 0.5 -> ItemStack(LAVA_BUCKET)
			else -> ItemStack(WATER_BUCKET)
		}
	}

	private data class EnchantData(val enchantment: Enchantment, val level: Int)

	private val enchantedBooks = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.THORNS, 1),
		EnchantData(Enchantment.PROTECTION_PROJECTILE, 1),
		EnchantData(Enchantment.DIG_SPEED, 5),
		EnchantData(Enchantment.ARROW_DAMAGE, 1),
		EnchantData(Enchantment.KNOCKBACK, 2),
		EnchantData(Enchantment.ARROW_KNOCKBACK, 2)
	)

	fun randomEnchantedBook(twoEnchants: Boolean): ItemStack {
		val book = ItemStack(ENCHANTED_BOOK)

		val index0 = Util.randRange(0, enchantedBooks.lastIndex)
		var index1 = Util.randRange(0, enchantedBooks.lastIndex)

		if (index0 == index1) index1 = (index1 + 1) % enchantedBooks.size

		val meta = book.itemMeta as EnchantmentStorageMeta
		meta.addStoredEnchant(enchantedBooks[index0].enchantment, enchantedBooks[index0].level, true)
		if (twoEnchants) meta.addStoredEnchant(enchantedBooks[index1].enchantment, enchantedBooks[index1].level, true)
		book.itemMeta = meta

		return book
	}

	private data class PotionAssociation(val potionType: PotionType, val potionInfo: Brew.Companion.PotionInfo?, val bottleType: Material)

	private val potionAssociations = arrayOf(
		PotionAssociation(PotionType.INSTANT_HEAL, null, SPLASH_POTION),
		PotionAssociation(PotionType.WEAKNESS, null, SPLASH_POTION),
		PotionAssociation(PotionType.INSTANT_DAMAGE, null, SPLASH_POTION),
		PotionAssociation(PotionType.POISON, POISON_INFO, SPLASH_POTION),
		PotionAssociation(PotionType.REGEN, REGEN_INFO, SPLASH_POTION),
		PotionAssociation(PotionType.SLOWNESS, null, SPLASH_POTION),
		PotionAssociation(PotionType.FIRE_RESISTANCE, null, POTION),
		PotionAssociation(PotionType.SPEED, null, POTION),
	)

	fun randomPotion(random: Random): ItemStack {
		val assoc = potionAssociations[random.nextInt(0, potionAssociations.size)]

		val extended = assoc.potionType.isExtendable && (!assoc.potionType.isUpgradeable || random.nextBoolean())
		val upgraded = assoc.potionType.isUpgradeable && !extended

		return if (assoc.potionInfo != null) {
			Brew.externalCreatePotion(assoc.bottleType, assoc.potionInfo, extended, upgraded)
		} else {
			Brew.createDefaultPotion(assoc.bottleType, PotionData(assoc.potionType, extended, upgraded))
		}.create()
	}

	private val armorEnchantments = arrayOf(Enchantment.THORNS, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_PROJECTILE, null)
	private val diamondArmor = arrayOf(DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS)
	private val ironArmor = arrayOf(IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS)

	fun randomArmor(diamond: Boolean): ItemStack {
		val item = ItemStack(Util.randFromArray(if (diamond) diamondArmor else ironArmor))

		val enchantment = Util.randFromArray(armorEnchantments)

		val meta = item.itemMeta
		if (enchantment != null) meta.addEnchant(enchantment, 1, true)
		item.itemMeta = meta

		return item
	}

	enum class ArmorType(vararg val items: Material) {
		LEATHER(LEATHER_BOOTS, LEATHER_LEGGINGS, LEATHER_CHESTPLATE, LEATHER_HELMET),
		GOLD(GOLDEN_BOOTS, GOLDEN_LEGGINGS, GOLDEN_CHESTPLATE, GOLDEN_HELMET),
		CHAIN(CHAINMAIL_BOOTS, CHAINMAIL_LEGGINGS, CHAINMAIL_CHESTPLATE, CHAINMAIL_HELMET),
		IRON(IRON_BOOTS, IRON_LEGGINGS, IRON_CHESTPLATE, IRON_HELMET),
		DIAMOND(DIAMOND_BOOTS, DIAMOND_LEGGINGS, DIAMOND_CHESTPLATE, DIAMOND_HELMET)
	}

	fun chaoticArmor(random: Random, type: ArmorType): ItemStack {
		val itemStack = ItemStack(type.items[random.nextInt(0, 4)])

		val enchantment = Util.randFromArray(armorEnchantments)

		if (enchantment != null) {
			val meta = itemStack.itemMeta
			meta.addEnchant(enchantment, 1, true)
			itemStack.itemMeta = meta
		}

		return itemStack
	}

	fun turtleShell(random: Random): ItemStack {
		val itemStack = ItemStack(TURTLE_HELMET)

		val enchantment = Util.randFromArray(armorEnchantments)

		if (enchantment != null) {
			val meta = itemStack.itemMeta
			meta.addEnchant(enchantment, 1, true)
			itemStack.itemMeta = meta
		}

		return itemStack
	}

	enum class ToolType(val pick: Material, val sword: Material, val axe: Material) {
		WOOD(WOODEN_PICKAXE, WOODEN_SWORD, WOODEN_AXE),
		STONE(STONE_PICKAXE, STONE_SWORD, STONE_AXE),
		GOLD(GOLDEN_PICKAXE, GOLDEN_SWORD, GOLDEN_AXE),
		IRON(IRON_PICKAXE, IRON_SWORD, IRON_AXE),
		DIAMOND(DIAMOND_PICKAXE, DIAMOND_SWORD, DIAMOND_AXE)
	}

	private val pickEnchants = arrayOf(
		EnchantData(Enchantment.DIG_SPEED, 2),
		EnchantData(Enchantment.LOOT_BONUS_BLOCKS, 1),
		EnchantData(Enchantment.MENDING, 1),
		EnchantData(Enchantment.SILK_TOUCH, 1),
		null
	)

	private val swordEnchants = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.LOOT_BONUS_MOBS, 1),
		EnchantData(Enchantment.KNOCKBACK, 2),
		null
	)

	private val axeEnchants = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.DIG_SPEED, 3),
		null
	)

	fun randomPick(diamond: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_PICKAXE else IRON_PICKAXE, pickEnchants)
	}

	fun randomSword(diamond: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_SWORD else IRON_SWORD, swordEnchants)
	}

	fun randomAxe(diamond: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_AXE else IRON_AXE, axeEnchants)
	}

	fun chaoticPick(type: ToolType) = enchantedTool(type.pick, pickEnchants)
	fun chaoticSword(type: ToolType) = enchantedTool(type.sword, swordEnchants)
	fun chaoticAxe(type: ToolType) = enchantedTool(type.axe, axeEnchants)

	fun powerBow(power: Int): ItemStack {
		val bow = ItemStack(BOW)

		val meta = bow.itemMeta
		meta.addEnchant(Enchantment.ARROW_DAMAGE, power, true)
		bow.itemMeta = meta

		return bow
	}

	fun piercingCrossbow(): ItemStack {
		val crossbow = ItemStack(CROSSBOW)

		val meta = crossbow.itemMeta
		meta.addEnchant(Enchantment.PIERCING, 1, true)
		crossbow.itemMeta = meta

		return crossbow
	}

	fun quickChargeCrossbow(): ItemStack {
		val crossbow = ItemStack(CROSSBOW)

		val meta = crossbow.itemMeta
		meta.addEnchant(Enchantment.QUICK_CHARGE, 1, true)
		crossbow.itemMeta = meta

		return crossbow
	}

	private fun enchantedTool(tool: Material, enchantList: Array<EnchantData?>): ItemStack {
		val item = ItemStack(tool)

		val enchantData = Util.randFromArray(enchantList)

		if (enchantData != null) {
			val meta = item.itemMeta
			meta.addEnchant(enchantData.enchantment, enchantData.level, true)
			item.itemMeta = meta
		}

		return item
	}

	fun flamingLazerSword(): ItemStack {
		val item = ItemStack(STONE_SWORD)

		val meta = item.itemMeta
		meta.setDisplayName("${ChatColor.RED}Flaming ${ChatColor.GOLD}Lazer ${ChatColor.YELLOW}Sword")
		meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true)
		meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
		(meta as Damageable).damage = 65
		item.itemMeta = meta

		return item
	}

	fun superSwaggyPants(): ItemStack {
		val item = ItemStack(CHAINMAIL_LEGGINGS)

		val meta = item.itemMeta
		meta.setDisplayName("${ChatColor.AQUA}Super ${ChatColor.BLUE}Swaggy ${ChatColor.DARK_PURPLE}Pants")
		meta.addEnchant(Enchantment.PROTECTION_PROJECTILE, 4, true)
		meta.addEnchant(Enchantment.THORNS, 1, true)
		meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
		(meta as Damageable).damage = 112
		item.itemMeta = meta

		return item
	}

	fun pickOne(vararg possibilities: Int): Int {
		return possibilities[(Math.random() * possibilities.size).toInt()]
	}

	private val glowStonePossibilities = ItemPossibilities(arrayOf(GLOWSTONE, GLOWSTONE_DUST), arrayOf(arrayOf(4, 6), arrayOf(12, 18)))

	fun glowstone(): ItemStack {
		return randomItem(glowStonePossibilities)
	}

	val boats = arrayOf(OAK_BOAT, ACACIA_BOAT, JUNGLE_BOAT, SPRUCE_BOAT, DARK_OAK_BOAT, BIRCH_BOAT)

	fun randomBoat(): ItemStack {
		return ItemStack(Util.randFromArray(boats))
	}

	fun chaoticTippedArrow(random: Random, low: Int, high: Int): ItemStack {
		val assoc = potionAssociations[random.nextInt(0, potionAssociations.size)]
		val itemStack = ItemStack(TIPPED_ARROW, random.nextInt(low, high + 1))

		val extended = assoc.potionType.isExtendable && (!assoc.potionType.isUpgradeable || random.nextBoolean())
		val upgraded = assoc.potionType.isUpgradeable && !extended

		val meta = itemStack.itemMeta as PotionMeta
		meta.basePotionData = PotionData(assoc.potionType, extended, upgraded)
		itemStack.itemMeta = meta

		return itemStack
	}

	fun elytraRocket(amount: Int): ItemStack {
		val itemStack = ItemStack(FIREWORK_ROCKET, amount)

		val meta = itemStack.itemMeta as FireworkMeta
		meta.power = 3
		itemStack.itemMeta = meta

		return itemStack
	}
}

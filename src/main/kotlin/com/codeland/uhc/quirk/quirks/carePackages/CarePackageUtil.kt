package com.codeland.uhc.quirk.quirks.carePackages

import com.codeland.uhc.event.Brew
import com.codeland.uhc.event.Brew.Companion.POISON_INFO
import com.codeland.uhc.event.Brew.Companion.REGEN_INFO
import com.codeland.uhc.gui.ItemCreator
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.*
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

				val blockRadius =
					radius * Util.bilinear2D(magnitudeField, 3, 3, angle / (PI.toFloat() * 2.0f), magnitudeHeight)

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

	//TODO all of this sucks, care packages needs to be reworked

	data class ItemPossibilities(val materials: Array<Material>, val amounts: Array<Array<Int>>)

	fun randomItem(possibilities: ItemPossibilities): ItemStack {
		val materialIndex = Random.nextInt(possibilities.materials.size)
		val amounts = possibilities.amounts[materialIndex]

		return ItemStack(possibilities.materials[materialIndex], amounts[Random.nextInt(0, amounts.size)])
	}

	fun randomItem(items: Array<Material>, vararg amounts: Array<Int>): ItemStack {
		val index = (Math.random() * items.size).toInt()

		return ItemStack(items[index], amounts[index][Random.nextInt(0, amounts[index].size)])
	}

	fun randomItem(material: Material, vararg amounts: Int): ItemStack {
		return ItemStack(material, amounts[(Math.random() * amounts.size).toInt()])
	}

	private val bottlePossibilities =
		ItemPossibilities(arrayOf(SAND, GLASS, GLASS_BOTTLE), arrayOf(arrayOf(6, 9), arrayOf(3, 6, 9), arrayOf(3, 6)))

	fun randomBottlePart(): ItemStack {
		return randomItem(bottlePossibilities)
	}

	private val stewPossibilities = ItemPossibilities(arrayOf(RED_MUSHROOM, BROWN_MUSHROOM, OXEYE_DAISY),
		arrayOf(arrayOf(3, 5), arrayOf(3, 5), arrayOf(3, 5)))

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
		return randomItem(ingredientPossibilities[Random.nextInt(ingredientPossibilities.size)])
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

		val index0 = Random.nextInt(enchantedBooks.size)
		var index1 = Random.nextInt(enchantedBooks.size)

		if (index0 == index1) index1 = (index1 + 1) % enchantedBooks.size

		val meta = book.itemMeta as EnchantmentStorageMeta
		meta.addStoredEnchant(enchantedBooks[index0].enchantment, enchantedBooks[index0].level, true)
		if (twoEnchants) meta.addStoredEnchant(enchantedBooks[index1].enchantment, enchantedBooks[index1].level, true)
		book.itemMeta = meta

		return book
	}

	private data class PotionAssociation(
		val potionType: PotionType,
		val potionInfo: Brew.Companion.PotionInfo?,
		val bottleType: Material,
	)

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

	private val armorEnchantments =
		arrayOf(Enchantment.THORNS, Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_PROJECTILE, null)
	private val diamondArmor = arrayOf(DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS)
	private val ironArmor = arrayOf(IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS)

	fun randomArmor(diamond: Boolean): ItemStack {
		val armorArray = if (diamond) diamondArmor else ironArmor
		val item = ItemStack(armorArray[Random.nextInt(armorArray.size)])

		val enchantment = armorEnchantments[Random.nextInt(armorEnchantments.size)]

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

	private val swordEnchantsOld = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.LOOT_BONUS_MOBS, 1),
		EnchantData(Enchantment.KNOCKBACK, 2),
		null
	)

	private val axeEnchantsOld = arrayOf(
		EnchantData(Enchantment.DAMAGE_ALL, 1),
		EnchantData(Enchantment.DIG_SPEED, 3),
		null
	)

	fun randomPick(diamond: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_PICKAXE else IRON_PICKAXE, pickEnchants)
	}

	fun randomSword(diamond: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_SWORD else IRON_SWORD, swordEnchantsOld)
	}

	fun randomAxe(diamond: Boolean): ItemStack {
		return enchantedTool(if (diamond) DIAMOND_AXE else IRON_AXE, axeEnchantsOld)
	}

	fun chaoticPick(type: ToolType) = enchantedTool(type.pick, pickEnchants)
	fun chaoticSword(type: ToolType) = enchantedTool(type.sword, swordEnchantsOld)
	fun chaoticAxe(type: ToolType) = enchantedTool(type.axe, axeEnchantsOld)

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

		val enchantData = enchantList[Random.nextInt(enchantList.size)]

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

	private val glowStonePossibilities =
		ItemPossibilities(arrayOf(GLOWSTONE, GLOWSTONE_DUST), arrayOf(arrayOf(4, 6), arrayOf(12, 18)))

	fun glowstone(): ItemStack {
		return randomItem(glowStonePossibilities)
	}

	val boats = arrayOf(OAK_BOAT, ACACIA_BOAT, JUNGLE_BOAT, SPRUCE_BOAT, DARK_OAK_BOAT, BIRCH_BOAT)

	fun randomBoat(): ItemStack {
		return ItemStack(boats[Random.nextInt(boats.size)])
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

	/* new chaotic */

	class ItemReference(var remaining: Int, val min: Int, val max: Int, val create: ItemCreator) {
		constructor(remaining: Int, create: ItemCreator) : this(remaining, 1, 1, create)
	}

	val toolEnchants = arrayOf(
		Pair(Enchantment.DIG_SPEED, 3),
		Pair(Enchantment.LOOT_BONUS_BLOCKS, 3),
	)

	val swordEnchants = arrayOf(
		Pair(Enchantment.DAMAGE_ALL, 2),
		Pair(Enchantment.FIRE_ASPECT, 1),
		Pair(Enchantment.LOOT_BONUS_MOBS, 2),
		Pair(Enchantment.KNOCKBACK, 2),
	)

	val axeEnchants = arrayOf(
		Pair(Enchantment.DAMAGE_ALL, 2),
		Pair(Enchantment.DIG_SPEED, 3),
		Pair(Enchantment.LOOT_BONUS_BLOCKS, 3),
	)

	val armorEnchants = arrayOf(
		Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
		Pair(Enchantment.PROTECTION_PROJECTILE, 2),
		Pair(Enchantment.THORNS, 1),
	)

	val tridentEnchants = arrayOf(
		Pair(Enchantment.LOYALTY, 3),
		Pair(Enchantment.RIPTIDE, 3),
	)

	val bowEnchants = arrayOf(
		Pair(Enchantment.ARROW_DAMAGE, 2),
		Pair(Enchantment.ARROW_KNOCKBACK, 2),
	)

	val crossbowEnchants = arrayOf(
		Pair(Enchantment.PIERCING, 4),
		Pair(Enchantment.MULTISHOT, 1),
		Pair(Enchantment.QUICK_CHARGE, 2),
	)

	val bookEnchants = arrayOf(
		*toolEnchants,
		*swordEnchants,
		*axeEnchants,
		*armorEnchants,
		*tridentEnchants,
		*bowEnchants,
		*crossbowEnchants
	)

	fun <T> randFrom(arr: Array<T>): T {
		return arr[Random.nextInt(arr.size)]
	}

	fun genReferenceItems(): Array<ItemReference> {
		return arrayOf(
			ItemReference(324, 18, 64, ItemCreator.regular(IRON_NUGGET)),
			ItemReference(36, 2, 5, ItemCreator.regular(IRON_INGOT)),
			ItemReference(36, 2, 5, ItemCreator.regular(IRON_ORE)),
			ItemReference(36, 2, 5, ItemCreator.regular(DEEPSLATE_IRON_ORE)),
			ItemReference(4, ItemCreator.regular(IRON_BLOCK)),
			ItemReference(4, ItemCreator.regular(RAW_IRON_BLOCK)),

			ItemReference(2, ItemCreator.regular(IRON_HOE).enchant(randFrom(toolEnchants))),
			ItemReference(2, ItemCreator.regular(IRON_SHOVEL).enchant(randFrom(toolEnchants))),
			ItemReference(2, ItemCreator.regular(IRON_PICKAXE).enchant(randFrom(toolEnchants))),
			ItemReference(2, ItemCreator.regular(IRON_SWORD).enchant(randFrom(swordEnchants))),
			ItemReference(2, ItemCreator.regular(IRON_AXE).enchant(randFrom(axeEnchants))),

			ItemReference(2, ItemCreator.regular(IRON_HELMET).enchant(randFrom(armorEnchants))),
			ItemReference(
				2,
				ItemCreator.regular(IRON_CHESTPLATE).enchant(randFrom(armorEnchants))
			),
			ItemReference(
				2,
				ItemCreator.regular(IRON_LEGGINGS).enchant(randFrom(armorEnchants))
			),
			ItemReference(2, ItemCreator.regular(IRON_BOOTS).enchant(randFrom(armorEnchants))),

			ItemReference(
				2,
				ItemCreator.regular(TURTLE_HELMET).enchant(randFrom(armorEnchants))
			),

			ItemReference(8, ItemCreator.regular(DIAMOND)),
			ItemReference(8, ItemCreator.regular(DIAMOND_ORE)),
			ItemReference(8, ItemCreator.regular(DEEPSLATE_DIAMOND_ORE)),

			ItemReference(1, ItemCreator.regular(DIAMOND_HOE).enchant(randFrom(toolEnchants))),
			ItemReference(
				1,
				ItemCreator.regular(DIAMOND_SHOVEL).enchant(randFrom(toolEnchants))
			),
			ItemReference(
				1,
				ItemCreator.regular(DIAMOND_PICKAXE).enchant(randFrom(toolEnchants))
			),
			ItemReference(
				1,
				ItemCreator.regular(DIAMOND_SWORD).enchant(randFrom(swordEnchants))
			),
			ItemReference(1, ItemCreator.regular(DIAMOND_AXE).enchant(randFrom(axeEnchants))),

			ItemReference(
				1,
				ItemCreator.regular(DIAMOND_HELMET).enchant(randFrom(armorEnchants))
			),
			ItemReference(
				1,
				ItemCreator.regular(DIAMOND_CHESTPLATE).enchant(randFrom(armorEnchants))
			),
			ItemReference(
				1,
				ItemCreator.regular(DIAMOND_LEGGINGS).enchant(randFrom(armorEnchants))
			),
			ItemReference(
				1,
				ItemCreator.regular(DIAMOND_BOOTS).enchant(randFrom(armorEnchants))
			),

			ItemReference(162, 9, 64, ItemCreator.regular(GOLD_NUGGET)),
			ItemReference(18, 1, 3, ItemCreator.regular(GOLD_INGOT)),
			ItemReference(18, 1, 3, ItemCreator.regular(GOLD_ORE)),
			ItemReference(18, 1, 3, ItemCreator.regular(DEEPSLATE_GOLD_ORE)),
			ItemReference(2, ItemCreator.regular(GOLD_BLOCK)),
			ItemReference(2, ItemCreator.regular(RAW_GOLD_BLOCK)),

			ItemReference(8, ItemCreator.regular(ANCIENT_DEBRIS)),
			ItemReference(8, ItemCreator.regular(NETHERITE_SCRAP)),

			ItemReference(1, ItemCreator.regular(ELYTRA)),
			ItemReference(1, ItemCreator.regular(NETHER_STAR)),
			ItemReference(2, ItemCreator.regular(COBWEB)),
			ItemReference(2, ItemCreator.regular(ENDER_EYE)),
			ItemReference(4, ItemCreator.regular(TRIDENT).enchant(randFrom(tridentEnchants))),

			ItemReference(96, 4, 8, ItemCreator.regular(COAL)),
			ItemReference(96, 4, 8, ItemCreator.regular(CHARCOAL)),
			ItemReference(10, 1, 2, ItemCreator.regular(COAL_BLOCK)),

			ItemReference(64, 2, 9, ItemCreator.regular(EXPERIENCE_BOTTLE)),

			ItemReference(64, 3, 5, ItemCreator.regular(JUNGLE_SAPLING)),
			ItemReference(64, 3, 5, ItemCreator.regular(BONE_MEAL)),
			ItemReference(32, 1, 3, ItemCreator.regular(BONE)),

			ItemReference(10, 1, 2, ItemCreator.regular(GLASS)),
			ItemReference(10, 1, 2, ItemCreator.regular(SAND)),
			ItemReference(32, 2, 3, ItemCreator.regular(GLASS_BOTTLE)),
			ItemReference(32, 2, 3, ItemCreator.regular(REDSTONE)),
			ItemReference(32, 2, 3, ItemCreator.regular(GLOWSTONE_DUST)),
			ItemReference(12, 1, 2, ItemCreator.regular(GLOWSTONE)),
			ItemReference(32, 2, 3, ItemCreator.regular(GUNPOWDER)),
			ItemReference(10, 1, 2, ItemCreator.regular(BLAZE_POWDER)),
			ItemReference(10, 1, 2, ItemCreator.regular(BLAZE_ROD)),
			ItemReference(20, 1, 2, ItemCreator.regular(NETHER_WART)),
			ItemReference(8, ItemCreator.regular(GLISTERING_MELON_SLICE)),
			ItemReference(4, ItemCreator.regular(MELON)),
			ItemReference(4, ItemCreator.regular(MELON_SLICE)),
			ItemReference(8, ItemCreator.regular(MAGMA_CREAM)),
			ItemReference(8, ItemCreator.regular(GOLDEN_CARROT)),
			ItemReference(8, ItemCreator.regular(FERMENTED_SPIDER_EYE)),
			ItemReference(8, ItemCreator.regular(SPIDER_EYE)),
			ItemReference(8, ItemCreator.regular(SUGAR)),
			ItemReference(8, ItemCreator.regular(GHAST_TEAR)),

			ItemReference(15, ItemCreator.regular(BOOKSHELF)),
			ItemReference(30, 1, 3, ItemCreator.regular(BOOK)),
			ItemReference(30, 1, 3, ItemCreator.regular(LEATHER)),
			ItemReference(60, 3, 9, ItemCreator.regular(SUGAR_CANE)),
			ItemReference(60, 3, 9, ItemCreator.regular(PAPER)),
			ItemReference(64, 4, 6, ItemCreator.regular(OBSIDIAN)),
			ItemReference(2, ItemCreator.regular(ENCHANTING_TABLE)),
			ItemReference(2, ItemCreator.regular(GRINDSTONE)),
			ItemReference(4, ItemCreator.regular(ANVIL)),

			ItemReference(20, 1, 2, ItemCreator.regular(ENDER_PEARL)),

			ItemReference(3, ItemCreator.regular(BUCKET)),
			ItemReference(3, ItemCreator.regular(WATER_BUCKET)),
			ItemReference(9, ItemCreator.regular(LAVA_BUCKET)),
			ItemReference(3, ItemCreator.regular(POWDER_SNOW_BUCKET)),

			ItemReference(3, 3, 3, ItemCreator.regular(MAGMA_CUBE_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(HOGLIN_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(EVOKER_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(ELDER_GUARDIAN_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(BLAZE_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(COW_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(WITHER_SKELETON_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(VINDICATOR_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(PIGLIN_BRUTE_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(VEX_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(WITCH_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(POLAR_BEAR_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(PANDA_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(HORSE_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(SHULKER_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(GUARDIAN_SPAWN_EGG)),
			ItemReference(3, 3, 3, ItemCreator.regular(GHAST_SPAWN_EGG)),

			ItemReference(3, ItemCreator.regular(ENDER_CHEST)),
			ItemReference(3, ItemCreator.regular(CHEST)),
			ItemReference(3, ItemCreator.regular(FURNACE)),
			ItemReference(3, ItemCreator.regular(SMOKER)),
			ItemReference(3, ItemCreator.regular(CRAFTING_TABLE)),
			ItemReference(3, ItemCreator.regular(SMITHING_TABLE)),
			ItemReference(3, ItemCreator.regular(BREWING_STAND)),
			ItemReference(3, ItemCreator.regular(BLAST_FURNACE)),
			ItemReference(3, ItemCreator.regular(HOPPER)),

			ItemReference(32, 2, 6, ItemCreator.regular(TNT)),
			ItemReference(5, ItemCreator.regular(TNT_MINECART)),
			ItemReference(64, 16, 16, ItemCreator.regular(RAIL)),

			ItemReference(3, ItemCreator.regular(SPYGLASS)),
			ItemReference(3, ItemCreator.regular(SHEARS).enchant(Enchantment.DURABILITY, 1)),
			ItemReference(3, ItemCreator.regular(FLINT_AND_STEEL)),
			ItemReference(3, ItemCreator.regular(SADDLE)),
			ItemReference(4, ItemCreator.regular(SHIELD)),
			ItemReference(3,
				ItemCreator.regular(FISHING_ROD).enchant(Enchantment.LUCK, 3).enchant(Enchantment.LURE, 3)),

			ItemReference(32, 2, 4, ItemCreator.regular(ARROW)),
			ItemReference(32, 2, 4, ItemCreator.regular(SPECTRAL_ARROW)),
			ItemReference(32, 2, 4, ItemCreator.regular(FLINT)),
			ItemReference(32, 2, 4, ItemCreator.regular(STICK)),
			ItemReference(32, 2, 4, ItemCreator.regular(FEATHER)),

			ItemReference(32, 2, 4, ItemCreator.regular(TIPPED_ARROW)
				.customMeta<PotionMeta> { meta ->
					meta.basePotionData = PotionData(PotionType.INSTANT_HEAL, false, false)
				}
			),
			ItemReference(32, 2, 4, ItemCreator.regular(TIPPED_ARROW)
				.customMeta<PotionMeta> { meta ->
					meta.basePotionData = PotionData(PotionType.INSTANT_HEAL, false, true)
				}
			),
			ItemReference(32, 2, 4, ItemCreator.regular(TIPPED_ARROW)
				.customMeta<PotionMeta> { meta ->
					meta.basePotionData = PotionData(PotionType.REGEN, true, false)
				}
			),
			ItemReference(32, 2, 4, ItemCreator.regular(TIPPED_ARROW)
				.customMeta<PotionMeta> { meta ->
					meta.basePotionData = PotionData(PotionType.SLOWNESS, false, true)
				}
			),
			ItemReference(32, 2, 4, ItemCreator.regular(TIPPED_ARROW)
				.customMeta<PotionMeta> { meta ->
					meta.basePotionData = PotionData(PotionType.WEAKNESS, false, false)
				}
			),

			ItemReference(3, ItemCreator.regular(BOW).enchant(randFrom(bowEnchants))),
			ItemReference(3, ItemCreator.regular(CROSSBOW).enchant(randFrom(crossbowEnchants))),
			ItemReference(3, ItemCreator.regular(CROSSBOW)),
			ItemReference(32, 2, 3, ItemCreator.regular(STRING)),

			ItemReference(48, 3, 4, ItemCreator.regular(GUNPOWDER)),
			ItemReference(48, 3, 4, ItemCreator.regular(PAPER)),
			ItemReference(64, 3, 4, ItemCreator.regular(RED_DYE)),

			ItemReference(2, ItemCreator.regular(OAK_BOAT)),
			ItemReference(2, ItemCreator.regular(SPRUCE_BOAT)),
			ItemReference(2, ItemCreator.regular(BIRCH_BOAT)),
			ItemReference(2, ItemCreator.regular(JUNGLE_BOAT)),
			ItemReference(2, ItemCreator.regular(DARK_OAK_BOAT)),
			ItemReference(2, ItemCreator.regular(ACACIA_BOAT)),

			ItemReference(32, 8, 16, ItemCreator.regular(SNOWBALL)),
			ItemReference(32, 8, 16, ItemCreator.regular(EGG)),

			ItemReference(128, 16, 64, ItemCreator.regular(COBBLESTONE)),
			ItemReference(128, 16, 64, ItemCreator.regular(COBBLED_DEEPSLATE)),
			ItemReference(128, 16, 64, ItemCreator.regular(BLACKSTONE)),

			ItemReference(16, 1, 2, ItemCreator.regular(OXEYE_DAISY)),
			ItemReference(16, 1, 2, ItemCreator.regular(RED_MUSHROOM)),
			ItemReference(22, 1, 3, ItemCreator.regular(BROWN_MUSHROOM)),
			ItemReference(3, 3, 3, ItemCreator.regular(BOWL)),
			ItemReference(
				5, ItemCreator.regular(SUSPICIOUS_STEW).customMeta<SuspiciousStewMeta> {
					it.addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 0), true)
				}
			),

			ItemReference(64, 8, 10, ItemCreator.regular(OAK_PLANKS)),
			ItemReference(64, 8, 10, ItemCreator.regular(SPRUCE_PLANKS)),
			ItemReference(64, 8, 10, ItemCreator.regular(BIRCH_PLANKS)),
			ItemReference(64, 8, 10, ItemCreator.regular(JUNGLE_PLANKS)),
			ItemReference(64, 8, 10, ItemCreator.regular(ACACIA_PLANKS)),
			ItemReference(64, 8, 10, ItemCreator.regular(DARK_OAK_PLANKS)),
			ItemReference(64, 8, 10, ItemCreator.regular(CRIMSON_PLANKS)),
			ItemReference(64, 8, 10, ItemCreator.regular(WARPED_PLANKS)),

			ItemReference(32, 2, 6, ItemCreator.regular(PORKCHOP)),
			ItemReference(32, 2, 6, ItemCreator.regular(COOKED_PORKCHOP)),
			ItemReference(32, 2, 6, ItemCreator.regular(BEEF)),
			ItemReference(32, 2, 6, ItemCreator.regular(COOKED_BEEF)),
			ItemReference(8, ItemCreator.regular(CAKE)),
			ItemReference(16, 1, 2, ItemCreator.regular(APPLE)),
			ItemReference(8, ItemCreator.regular(GOLDEN_APPLE)),

			ItemReference(3, ItemCreator.regular(LIGHT_BLUE_SHULKER_BOX)),
			ItemReference(18, ItemCreator.regular(ENCHANTED_BOOK).customMeta<EnchantmentStorageMeta> {
				val (enchant, level) = randFrom(bookEnchants)
				it.addStoredEnchant(enchant, level, true)
			})
		)
	}
}

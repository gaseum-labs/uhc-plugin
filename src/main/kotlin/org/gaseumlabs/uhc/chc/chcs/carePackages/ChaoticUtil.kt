package org.gaseumlabs.uhc.chc.chcs.carePackages

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SuspiciousStewMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.gaseumlabs.uhc.gui.ItemCreator
import kotlin.random.Random
import kotlin.random.nextInt

object ChaoticUtil {
	class ItemReference(
		var remaining: Int,
		val min: Int,
		val max: Int,
		val creator: () -> ItemCreator,
	) {
		constructor(remaining: Int, creator: () -> ItemCreator) : this(remaining, 1, 1, creator)
	}

	private val toolEnchants = arrayOf(
		Pair(Enchantment.DIG_SPEED, 3),
		Pair(Enchantment.LOOT_BONUS_BLOCKS, 3),
	)

	private val swordEnchants = arrayOf(
		Pair(Enchantment.DAMAGE_ALL, 2),
		Pair(Enchantment.FIRE_ASPECT, 1),
		Pair(Enchantment.LOOT_BONUS_MOBS, 2),
		Pair(Enchantment.KNOCKBACK, 2),
	)

	private val axeEnchants = arrayOf(
		Pair(Enchantment.DAMAGE_ALL, 2),
		Pair(Enchantment.DIG_SPEED, 3),
		Pair(Enchantment.LOOT_BONUS_BLOCKS, 3),
	)

	private val armorEnchants = arrayOf(
		Pair(Enchantment.PROTECTION_ENVIRONMENTAL, 2),
		Pair(Enchantment.PROTECTION_PROJECTILE, 2),
		Pair(Enchantment.THORNS, 1),
	)

	private val tridentEnchants = arrayOf(
		Pair(Enchantment.LOYALTY, 3),
		Pair(Enchantment.RIPTIDE, 3),
	)

	private val bowEnchants = arrayOf(
		Pair(Enchantment.ARROW_DAMAGE, 2),
		Pair(Enchantment.ARROW_KNOCKBACK, 2),
	)

	private val crossbowEnchants = arrayOf(
		Pair(Enchantment.PIERCING, 4),
		Pair(Enchantment.MULTISHOT, 1),
		Pair(Enchantment.QUICK_CHARGE, 2),
	)

	private val bookEnchants = arrayOf(
		*toolEnchants,
		*swordEnchants,
		*axeEnchants,
		*armorEnchants,
		*tridentEnchants,
		*bowEnchants,
		*crossbowEnchants
	)

	private fun tippedArrowRef(amount: Int, low: Int, high: Int, potionData: PotionData) = ItemReference(amount, low, high) {
		ItemCreator.regular(Material.TIPPED_ARROW).customMeta<PotionMeta> {
			it.basePotionData = potionData
		}
	}
	
	private fun randomEffect(): FireworkEffect {
		val builder = FireworkEffect.builder()
		builder.flicker(Random.nextBoolean())
		builder.trail(Random.nextBoolean())
		builder.withColor(Color.FUCHSIA)
		builder.withFade(Color.AQUA)
		builder.with(FireworkEffect.Type.values().random())
		return builder.build()
	}

	private fun damageRocket(amount: Int, low: Int, high: Int): ItemReference {
		return ItemReference(amount, low, high) { ItemCreator.regular(Material.FIREWORK_ROCKET).customMeta<FireworkMeta> {
			it.power = 0
			it.addEffect(randomEffect())
			it.addEffect(randomEffect())
			it.addEffect(randomEffect())
			it.addEffect(randomEffect())
			it.addEffect(randomEffect())
			it.addEffect(randomEffect())
			it.addEffect(randomEffect())
		} }
	}

	private fun potion(amount: Int, potionData: PotionData) = ItemReference(amount) {
		ItemCreator.regular(Material.SPLASH_POTION).customMeta<PotionMeta> {
			it.basePotionData = potionData
		}
	}

	fun genReferenceItems() = arrayOf(
		/* iron materials */
		ItemReference(324, 18, 64) { ItemCreator.regular(Material.IRON_NUGGET) },
		ItemReference(36, 2, 5) { ItemCreator.regular(Material.IRON_INGOT) },
		ItemReference(36, 2, 5) { ItemCreator.regular(Material.IRON_ORE) },
		ItemReference(36, 2, 5) { ItemCreator.regular(Material.DEEPSLATE_IRON_ORE) },
		ItemReference(8) { ItemCreator.regular(Material.IRON_BLOCK) },
		ItemReference(4) { ItemCreator.regular(Material.RAW_IRON_BLOCK) },

		/* golden equipment */
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_HOE).enchant(toolEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_SHOVEL).enchant(toolEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_PICKAXE).enchant(toolEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_SWORD).enchant(swordEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_AXE).enchant(axeEnchants.random()) },

		ItemReference(3) { ItemCreator.regular(Material.GOLDEN_HELMET).enchant(armorEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_CHESTPLATE).enchant(armorEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_LEGGINGS).enchant(armorEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.GOLDEN_BOOTS).enchant(armorEnchants.random()) },

		/* iron equipment */
		ItemReference(4) { ItemCreator.regular(Material.IRON_HOE).enchant(toolEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.IRON_SHOVEL).enchant(toolEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.IRON_PICKAXE).enchant(toolEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.IRON_SWORD).enchant(swordEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.IRON_AXE).enchant(axeEnchants.random()) },

		ItemReference(3) { ItemCreator.regular(Material.IRON_HELMET).enchant(armorEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.IRON_CHESTPLATE).enchant(armorEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.IRON_LEGGINGS).enchant(armorEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.IRON_BOOTS).enchant(armorEnchants.random()) },
		ItemReference(3) { ItemCreator.regular(Material.TURTLE_HELMET).enchant(armorEnchants.random()) },

		/* goal materials */
		ItemReference(162, 9, 64) { ItemCreator.regular(Material.GOLD_NUGGET) },
		ItemReference(18, 1, 3) { ItemCreator.regular(Material.GOLD_INGOT) },
		ItemReference(18, 1, 3) { ItemCreator.regular(Material.GOLD_ORE) },
		ItemReference(18, 1, 3) { ItemCreator.regular(Material.DEEPSLATE_GOLD_ORE) },
		ItemReference(2) { ItemCreator.regular(Material.GOLD_BLOCK) },
		ItemReference(2) { ItemCreator.regular(Material.RAW_GOLD_BLOCK) },

		/* diamond materials */
		ItemReference(1) { ItemCreator.regular(Material.DIAMOND_BLOCK) },
		ItemReference(14) { ItemCreator.regular(Material.DIAMOND) },
		ItemReference(14) { ItemCreator.regular(Material.DIAMOND_ORE) },
		ItemReference(14) { ItemCreator.regular(Material.DEEPSLATE_DIAMOND_ORE) },

		ItemReference(1) { ItemCreator.regular(Material.DIAMOND_HOE).enchant(toolEnchants.random()) },
		ItemReference(1) { ItemCreator.regular(Material.NETHERITE_HOE).enchant(toolEnchants.random()) },

		ItemReference(36, 4, 4) { ItemCreator.regular(Material.LAPIS_LAZULI) },
		ItemReference(4, 4, 4) { ItemCreator.regular(Material.LAPIS_BLOCK) },

		ItemReference(8) { ItemCreator.regular(Material.ANCIENT_DEBRIS) },
		ItemReference(8) { ItemCreator.regular(Material.NETHERITE_SCRAP) },

		ItemReference(4) { ItemCreator.regular(Material.COBWEB) },
		ItemReference(2) { ItemCreator.regular(Material.ENDER_EYE) },
		ItemReference(4) { ItemCreator.regular(Material.TRIDENT).enchant(tridentEnchants.random()) },

		ItemReference(96, 4, 8) { ItemCreator.regular(Material.COAL) },
		ItemReference(96, 4, 8) { ItemCreator.regular(Material.CHARCOAL) },
		ItemReference(10, 1, 2) { ItemCreator.regular(Material.COAL_BLOCK) },

		ItemReference(80, 2, 9) { ItemCreator.regular(Material.EXPERIENCE_BOTTLE) },

		ItemReference(64, 3, 5) { ItemCreator.regular(Material.JUNGLE_SAPLING) },
		ItemReference(64, 3, 5) { ItemCreator.regular(Material.BONE_MEAL) },
		ItemReference(32, 1, 3) { ItemCreator.regular(Material.BONE) },

		ItemReference(10, 1, 2) { ItemCreator.regular(Material.GLASS) },
		ItemReference(10, 1, 2) { ItemCreator.regular(Material.SAND) },
		ItemReference(32, 2, 3) { ItemCreator.regular(Material.GLASS_BOTTLE) },
		ItemReference(32, 2, 3) { ItemCreator.regular(Material.REDSTONE) },
		ItemReference(32, 2, 3) { ItemCreator.regular(Material.GLOWSTONE_DUST) },
		ItemReference(12, 1, 2) { ItemCreator.regular(Material.GLOWSTONE) },
		ItemReference(32, 2, 3) { ItemCreator.regular(Material.GUNPOWDER) },
		ItemReference(10, 1, 2) { ItemCreator.regular(Material.BLAZE_POWDER) },
		ItemReference(10, 1, 2) { ItemCreator.regular(Material.BLAZE_ROD) },
		ItemReference(20, 1, 2) { ItemCreator.regular(Material.NETHER_WART) },
		ItemReference(8) { ItemCreator.regular(Material.GLISTERING_MELON_SLICE) },
		ItemReference(4) { ItemCreator.regular(Material.MELON) },
		ItemReference(16) { ItemCreator.regular(Material.CARVED_PUMPKIN) },
		ItemReference(4) { ItemCreator.regular(Material.MELON_SLICE) },
		ItemReference(8) { ItemCreator.regular(Material.MAGMA_CREAM) },
		ItemReference(8) { ItemCreator.regular(Material.GOLDEN_CARROT) },
		ItemReference(8) { ItemCreator.regular(Material.FERMENTED_SPIDER_EYE) },
		ItemReference(8) { ItemCreator.regular(Material.SPIDER_EYE) },
		ItemReference(8) { ItemCreator.regular(Material.SUGAR) },
		ItemReference(8) { ItemCreator.regular(Material.GHAST_TEAR) },
		ItemReference(8) { ItemCreator.regular(Material.DRAGON_BREATH) },

		ItemReference(15) { ItemCreator.regular(Material.BOOKSHELF) },
		ItemReference(30, 1, 3) {ItemCreator.regular(Material.BOOK)},
		ItemReference(30, 1, 3) {ItemCreator.regular(Material.LEATHER)},
		ItemReference(60, 3, 9) { ItemCreator.regular(Material.SUGAR_CANE) },
		ItemReference(60, 3, 9) { ItemCreator.regular(Material.PAPER) },
		ItemReference(128, 4, 6) { ItemCreator.regular(Material.OBSIDIAN) },
		ItemReference(2) { ItemCreator.regular(Material.ENCHANTING_TABLE) },
		ItemReference(5) { ItemCreator.regular(Material.GRINDSTONE) },
		ItemReference(12) { ItemCreator.regular(Material.ANVIL) },

		ItemReference(1) { ItemCreator.regular(Material.BEDROCK) },
		ItemReference(1) { ItemCreator.regular(Material.TOTEM_OF_UNDYING) },
		ItemReference(1) { ItemCreator.regular(Material.WITHER_SKELETON_SKULL) },
		ItemReference(1) { ItemCreator.regular(Material.NETHER_STAR) },

		ItemReference(20, 1, 2) { ItemCreator.regular(Material.ENDER_PEARL) },

		ItemReference(3) { ItemCreator.regular(Material.BUCKET) },
		ItemReference(3) { ItemCreator.regular(Material.WATER_BUCKET) },
		ItemReference(9) { ItemCreator.regular(Material.LAVA_BUCKET) },
		ItemReference(3) { ItemCreator.regular(Material.POWDER_SNOW_BUCKET) },

		ItemReference(6, 3, 3) { ItemCreator.regular(Material.MAGMA_CUBE_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.HOGLIN_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.EVOKER_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.ELDER_GUARDIAN_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.BLAZE_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.WITHER_SKELETON_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.VINDICATOR_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.PIGLIN_BRUTE_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.VEX_SPAWN_EGG) },
		ItemReference(24, 3, 3) { ItemCreator.regular(Material.WITCH_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.POLAR_BEAR_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.PANDA_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.HORSE_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.SHULKER_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.GUARDIAN_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.GHAST_SPAWN_EGG) },
		ItemReference(6, 3, 3) { ItemCreator.regular(Material.RAVAGER_SPAWN_EGG) },

		ItemReference(3) { ItemCreator.regular(Material.ENDER_CHEST) },
		ItemReference(3) { ItemCreator.regular(Material.CHEST) },
		ItemReference(8) { ItemCreator.regular(Material.FURNACE) },
		ItemReference(8) { ItemCreator.regular(Material.BLAST_FURNACE) },
		ItemReference(3) { ItemCreator.regular(Material.SMOKER) },
		ItemReference(3) { ItemCreator.regular(Material.CRAFTING_TABLE) },
		ItemReference(3) { ItemCreator.regular(Material.SMITHING_TABLE) },
		ItemReference(3) { ItemCreator.regular(Material.BREWING_STAND) },
		ItemReference(3) { ItemCreator.regular(Material.HOPPER) },

		ItemReference(32, 2, 6) { ItemCreator.regular(Material.TNT) },
		ItemReference(5) { ItemCreator.regular(Material.TNT_MINECART) },
		ItemReference(64, 16, 16) { ItemCreator.regular(Material.RAIL) },

		ItemReference(3) { ItemCreator.regular(Material.SPYGLASS) },
		ItemReference(3) { ItemCreator.regular(Material.SHEARS).enchant(Enchantment.DURABILITY, 1) },
		ItemReference(3) { ItemCreator.regular(Material.FLINT_AND_STEEL) },
		ItemReference(19, 3, 7) { ItemCreator.regular(Material.FIRE_CHARGE) },
		ItemReference(3) { ItemCreator.regular(Material.SADDLE) },
		ItemReference(4) { ItemCreator.regular(Material.SHIELD) },
		ItemReference(3) { ItemCreator.regular(Material.FISHING_ROD).enchant(Enchantment.LUCK, 3).enchant(Enchantment.LURE, 3) },

		ItemReference(32, 2, 4) { ItemCreator.regular(Material.ARROW) },
		ItemReference(32, 2, 4) { ItemCreator.regular(Material.SPECTRAL_ARROW) },
		ItemReference(32, 2, 4) { ItemCreator.regular(Material.FLINT) },
		ItemReference(32, 2, 4) { ItemCreator.regular(Material.STICK) },
		ItemReference(32, 2, 4) { ItemCreator.regular(Material.FEATHER) },

		tippedArrowRef(32, 2, 4, PotionData(PotionType.INSTANT_HEAL, false, false)),
		tippedArrowRef(32, 2, 4, PotionData(PotionType.INSTANT_HEAL, false, true)),
		tippedArrowRef(32, 2, 4, PotionData(PotionType.REGEN, false, true)),
		tippedArrowRef(32, 2, 4, PotionData(PotionType.REGEN, true, false)),
		tippedArrowRef(32, 2, 4, PotionData(PotionType.STRENGTH, false, false)),

		tippedArrowRef(32, 2, 4, PotionData(PotionType.SLOWNESS, false, true)),
		tippedArrowRef(32, 2, 4, PotionData(PotionType.WEAKNESS, false, false)),
		tippedArrowRef(32, 2, 4, PotionData(PotionType.POISON, true, false)),
		tippedArrowRef(32, 2, 4, PotionData(PotionType.INSTANT_DAMAGE, false, true)),

		ItemReference(3) { ItemCreator.regular(Material.BOW).enchant(bowEnchants.random()) },
		ItemReference(4) { ItemCreator.regular(Material.CROSSBOW).enchant(crossbowEnchants.random()) },
		ItemReference(32, 2, 3) { ItemCreator.regular(Material.STRING) },

		damageRocket(48, 8, 13),
		ItemReference(48, 3, 4) { ItemCreator.regular(Material.GUNPOWDER) },
		ItemReference(48, 3, 4) { ItemCreator.regular(Material.PAPER) },
		ItemReference(64, 3, 4) { ItemCreator.regular(Material.RED_DYE) },

		ItemReference(2) { ItemCreator.regular(Material.OAK_BOAT) },
		ItemReference(2) { ItemCreator.regular(Material.SPRUCE_BOAT) },
		ItemReference(2) { ItemCreator.regular(Material.BIRCH_BOAT) },
		ItemReference(2) { ItemCreator.regular(Material.JUNGLE_BOAT) },
		ItemReference(2) { ItemCreator.regular(Material.DARK_OAK_BOAT) },
		ItemReference(2) { ItemCreator.regular(Material.ACACIA_BOAT) },

		ItemReference(33, 8, 16) { ItemCreator.regular(Material.SNOWBALL) },
		ItemReference(33, 8, 16) { ItemCreator.regular(Material.EGG) },

		ItemReference(16, 2, 4) { ItemCreator.regular(Material.SOUL_SAND) },
		ItemReference(128, 16, 64) { ItemCreator.regular(Material.COBBLESTONE) },
		ItemReference(128, 16, 64) { ItemCreator.regular(Material.COBBLED_DEEPSLATE) },
		ItemReference(128, 16, 64) { ItemCreator.regular(Material.BLACKSTONE) },

		ItemReference(16, 1, 2) { ItemCreator.regular(Material.OXEYE_DAISY) },
		ItemReference(16, 1, 2) { ItemCreator.regular(Material.RED_MUSHROOM) },
		ItemReference(22, 1, 3) { ItemCreator.regular(Material.BROWN_MUSHROOM) },
		ItemReference(5) {
			ItemCreator.regular(Material.SUSPICIOUS_STEW).customMeta<SuspiciousStewMeta> {
				it.addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 0), true)
			}
		},

		ItemReference(64, 8, 10) { ItemCreator.regular(Material.OAK_PLANKS) },
		ItemReference(64, 8, 10) { ItemCreator.regular(Material.SPRUCE_PLANKS) },
		ItemReference(64, 8, 10) { ItemCreator.regular(Material.BIRCH_PLANKS) },
		ItemReference(64, 8, 10) { ItemCreator.regular(Material.JUNGLE_PLANKS) },
		ItemReference(64, 8, 10) { ItemCreator.regular(Material.ACACIA_PLANKS) },
		ItemReference(64, 8, 10) { ItemCreator.regular(Material.DARK_OAK_PLANKS) },
		ItemReference(64, 8, 10) { ItemCreator.regular(Material.CRIMSON_PLANKS) },
		ItemReference(64, 8, 10) { ItemCreator.regular(Material.WARPED_PLANKS) },

		ItemReference(32, 2, 6) { ItemCreator.regular(Material.PORKCHOP) },
		ItemReference(32, 2, 6) { ItemCreator.regular(Material.COOKED_PORKCHOP) },
		ItemReference(32, 2, 6) { ItemCreator.regular(Material.BEEF) },
		ItemReference(32, 2, 6) { ItemCreator.regular(Material.COOKED_BEEF) },
		ItemReference(8) { ItemCreator.regular(Material.CAKE) },
		ItemReference(16, 1, 2) { ItemCreator.regular(Material.APPLE) },
		ItemReference(8) { ItemCreator.regular(Material.GOLDEN_APPLE) },

		ItemReference(3) { ItemCreator.regular(Material.LIGHT_BLUE_SHULKER_BOX) },
		ItemReference(26) {
			ItemCreator.regular(Material.ENCHANTED_BOOK).customMeta<EnchantmentStorageMeta> {
				val (enchant, level) = bookEnchants.random()
				it.addStoredEnchant(enchant, level, true)
			}
		},

		potion(2, PotionData(PotionType.INSTANT_DAMAGE, false, true)),
		potion(4, PotionData(PotionType.JUMP, false, true)),
		potion(3, PotionData(PotionType.SPEED, false, true)),
		potion(2, PotionData(PotionType.TURTLE_MASTER, false, true)),
		potion(2, PotionData(PotionType.INVISIBILITY, true, false)),
		potion(2, PotionData(PotionType.SLOW_FALLING, true, false)),
	)
}

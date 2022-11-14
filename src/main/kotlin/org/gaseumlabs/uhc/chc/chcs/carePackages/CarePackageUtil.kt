package org.gaseumlabs.uhc.chc.chcs.carePackages

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.gaseumlabs.uhc.util.ItemUtil
import org.gaseumlabs.uhc.util.Util
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.*
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

	private fun dropFireworkEffect(type: FireworkEffect.Type, colors: Int): FireworkEffect {
		val builder = FireworkEffect.builder()

		builder.flicker(false)
		builder.trail(true)

		builder.with(type)

		/* colors */
		for (i in 0 until colors) {
			builder.withColor(Color.fromRGB(Random.nextInt(0x1000000)))
			builder.withFade(Color.fromRGB(Random.nextInt(0x1000000)))
		}

		return builder.build()
	}

	private fun spawnDropFirework(block: Block, power: Int, effect: FireworkEffect) {
		val firework = block.world.spawnEntity(
			block.location.add(0.5, 1.5, 0.5), EntityType.FIREWORK
		) as Firework

		val meta = firework.fireworkMeta
		meta.power = power
		meta.addEffect(effect)
		firework.fireworkMeta = meta
	}

	fun generateChest(block: Block, color: TextColor, blockType: Material): Inventory {
		/* create the chest */
		block.breakNaturally()
		block.type = blockType

		/* set the name of the chest */
		val chest = block.getState(false) as Container
		chest.customName(Component.text("Care Package", color, TextDecoration.BOLD))

		/* blast a firework right above the chest */
		spawnDropFirework(block, 2, dropFireworkEffect(FireworkEffect.Type.BALL_LARGE, 3))
		spawnDropFirework(block, 1, dropFireworkEffect(FireworkEffect.Type.BALL, 2))
		spawnDropFirework(block, 0, dropFireworkEffect(FireworkEffect.Type.BURST, 1))

		return chest.inventory
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

	fun regenerationStew(): ItemStack {
		val stew = ItemStack(SUSPICIOUS_STEW)

		val meta = stew.itemMeta as SuspiciousStewMeta
		meta.addCustomEffect(PotionEffect(PotionEffectType.REGENERATION, 8 * 20, 0), true)
		stew.itemMeta = meta

		return stew
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
}

package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.ResourceDescription
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ResourceMelon : ResourceDescription() {
	override fun nextInterval(collected: Int): Int {
		return 20 * (/* 120 */30 + 30 * collected)
	}

	override fun maxCurrent(collected: Int): Int {
		return 4
	}

	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (world !== WorldManager.gameWorld) return null

		/* a place on grass without any cover */
		var fallbackBlock: Block? = null

		/* try 40 times */
		for (t in 0 until 40) {
			val angle = Random.nextDouble(0.0, 2.0 * PI)
			val distance = Random.nextDouble(48.0, 80.0)

			val x = (centerX + cos(angle) * distance).toInt()
			val z = (centerZ + sin(angle) * distance).toInt()

			val biome = world.getBlockAt(x, centerY + 30, z).biome
			if (
				biome !== Biome.JUNGLE &&
				biome !== Biome.SPARSE_JUNGLE &&
				biome !== Biome.BAMBOO_JUNGLE
			) continue

			var lastRoofY = 10000

			for (y in centerY + 30 downTo (centerY - 30).coerceAtLeast(62)) {
				val block = world.getBlockAt(x, y, z)

				if (block.type === Material.GRASS_BLOCK) {
					/* has 0 or 1 blocks of clearance above the melon */
					if (lastRoofY - y in 2..3) {
						return listOf(block.getRelative(BlockFace.UP))
					} else {
						fallbackBlock = block.getRelative(BlockFace.UP)
					}
				} else if (!block.isPassable && block.type !== Material.COCOA) {
					lastRoofY = y
				}
			}
		}

		return listOf(fallbackBlock ?: return null)
	}

	override fun setBlock(block: Block) {
		block.setType(Material.MELON, false)
	}
}

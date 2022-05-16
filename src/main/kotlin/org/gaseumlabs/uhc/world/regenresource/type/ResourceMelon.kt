package org.gaseumlabs.uhc.world.regenresource.type

import net.minecraft.world.level.biome.Biomes
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.locateAround
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreader
import org.gaseumlabs.uhc.world.regenresource.ResourceDescription
import kotlin.math.*
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

		val worldHandle = (world as CraftWorld).handle

		/* a place on grass without any cover */
		var fallbackBlock: Block? = null

		val potentialBlocks = locateAround(world, centerX, centerZ, 11, 32.0, 80.0, 8) { x, z ->
			if (!RegenUtil.insideWorldBorder(world, x, z)) return@locateAround null

			val biome = worldHandle.getNoiseBiome(x / 4, centerY / 4, z / 4).unwrapKey().get()

			if (
				biome === Biomes.UHC_JUNGLE ||
				biome === Biomes.UHC_SPARSE_JUNGLE ||
				biome === Biomes.UHC_BAMBOO_JUNGLE
			) {
				x to z
			} else {
				null
			}
		}

		//potentialBlocks.forEach { (x, z)  ->
		//	val fallingBlock = world.spawnFallingBlock(Location(world, x + 0.5, centerY.toDouble(), z + 0.5), Material.LAPIS_BLOCK.createBlockData())
		//	fallingBlock.setGravity(false)
		//	fallingBlock.isGlowing = true
		//	fallingBlock.dropItem = false
		//}

		for ((x, z) in potentialBlocks) {
			val initialBlock = world.getBlockAt(x, centerY, z)

			val melonSurface = surfaceSpreader(initialBlock, 20, 5, 5, ::jungleSurface, ::melonGood)
			if (melonSurface != null) {
				return listOf(melonSurface.getRelative(BlockFace.UP))
			}
		}

		return null
	}

	override fun setBlock(block: Block) {
		block.setType(Material.MELON, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === Material.MELON
	}

	/* placement */

	fun jungleSurface(block: Block): Boolean {
		if (block.type === Material.GRASS_BLOCK) return true
		if (block.type !== Material.DIRT) return false

		return when (block.getRelative(BlockFace.UP).type) {
			Material.OAK_LOG,
			Material.JUNGLE_LOG,
			-> true
			else -> false
		}
	}

	fun melonGood(surfaceBlock: Block): Boolean {
		val above = surfaceBlock.getRelative(BlockFace.UP)
		val ceiling = above.getRelative(BlockFace.UP)

		return when (surfaceBlock.getRelative(BlockFace.UP).type) {
			Material.AIR,
			Material.FERN,
			Material.VINE,
			Material.COCOA,
			Material.OAK_LEAVES,
			Material.JUNGLE_LEAVES,
			Material.GRASS,
			-> true
			else -> false
		} && !ceiling.isPassable && ceiling.type !== Material.COCOA
	}
}

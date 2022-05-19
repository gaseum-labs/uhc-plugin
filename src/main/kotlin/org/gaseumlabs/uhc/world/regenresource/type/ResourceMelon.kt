package org.gaseumlabs.uhc.world.regenresource.type

import net.minecraft.world.level.biome.Biomes
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.locateAround
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderOverworld

class ResourceMelon(
	released: HashMap<PhaseType, Int>,
	current: Int,
	interval: Int,
	prettyName: String,
) : ResourceDescriptionBlock(
	released,
	current,
	interval,
	prettyName
) {
	override fun generateVein(world: World, centerX: Int, centerY: Int, centerZ: Int): List<Block>? {
		if (centerY < SpawnUtil.SURFACE_Y) return null
		if (world !== WorldManager.gameWorld) return null

		val worldHandle = (world as CraftWorld).handle

		val potentialSpots = locateAround(world, centerX, centerZ, 11, 32.0, 80.0, 8) { x, z ->
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

		for ((x, z) in potentialSpots) {
			val melonSurface = surfaceSpreaderOverworld(world, x, z, 5, ::melonGood)
			if (melonSurface != null) {
				return listOf(melonSurface.getRelative(BlockFace.UP))
			}
		}

		return null
	}

	override fun setBlock(block: Block, index: Int) {
		block.setType(Material.MELON, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === Material.MELON
	}

	/* placement */

	private fun melonGood(surfaceBlock: Block): Boolean {
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

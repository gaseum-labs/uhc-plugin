package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.regenresource.RegenUtil
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderOverworld
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionBlock

class ResourceMelon(
	released: HashMap<PhaseType, Int>,
	chunkRadius: Int,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : ResourceDescriptionBlock(
	released,
	chunkRadius,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	override fun eligable(player: Player): Boolean {
		return player.location.y >= 58
	}

	fun hasJungle(bounds: RegenUtil.GenBounds): Boolean {
		val worldHandle = (bounds.world as CraftWorld).handle

		var count = 0

		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.25f), 70, bounds.alongZ(0.25f)))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.25f), 70, bounds.alongZ(0.75f)))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.75f), 70, bounds.alongZ(0.25f)))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.75f), 70, bounds.alongZ(0.75f)))) ++count

		return count >= 3
	}

	override fun generate(genBounds: RegenUtil.GenBounds, fullVein: Boolean): List<Block>? {
		if (!hasJungle(genBounds)) return null

		val melonSurface = surfaceSpreaderOverworld(genBounds.world, genBounds.centerX(), genBounds.centerZ(), 7, ::melonGood)
		if (melonSurface != null) {
			return listOf(melonSurface.getRelative(BlockFace.UP))
		}

		return null
	}

	override fun setBlock(block: Block, index: Int, fullVein: Boolean) {
		block.setType(if (fullVein) MELON else CARVED_PUMPKIN, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === MELON || block.type === CARVED_PUMPKIN
	}

	/* placement */

	private fun melonGood(surfaceBlock: Block): Boolean {
		return when (surfaceBlock.type) {
			GRASS_BLOCK,
			DIRT,
			-> true
			else -> false
		}
	}
}

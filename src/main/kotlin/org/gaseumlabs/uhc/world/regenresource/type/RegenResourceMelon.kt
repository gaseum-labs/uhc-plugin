package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.superSurfaceSpreader

class RegenResourceMelon(
	released: HashMap<PhaseType, Release>,
	worldName: String,
	prettyName: String,
) : RegenResourceBlock(
	released,
	worldName,
	prettyName,
) {
	override fun eligible(player: Player): Boolean {
		return player.location.y >= 58
	}
	override fun onUpdate(vein: VeinBlock) {}

	private fun hasJungle(bounds: RegenUtil.GenBounds): Boolean {
		val worldHandle = (bounds.world as CraftWorld).handle

		var count = 0

		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.25f), 70, bounds.alongZ(0.25f)))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.25f), 70, bounds.alongZ(0.75f)))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.75f), 70, bounds.alongZ(0.25f)))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.75f), 70, bounds.alongZ(0.75f)))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, bounds.alongX(0.5f), 70, bounds.alongZ(0.5f)))) ++count

		return count >= 3
	}

	override fun generate(genBounds: RegenUtil.GenBounds, tier: Int): GenResult? {
		if (!hasJungle(genBounds)) return null

		val surface = superSurfaceSpreader(genBounds, ::melonGood).randomOrNull() ?: return null

		return GenResult(listOf(surface.getRelative(BlockFace.UP)), 1)
	}

	override fun initializeBlock(blocks: List<Block>, tier: Int) {
		blocks[0].setType(if (tier == 0) MELON else CARVED_PUMPKIN, false)
	}

	override fun isModifiedBlock(blocks: List<Block>) =
		blocks[0].type !== MELON || blocks[0].type !== CARVED_PUMPKIN

	/* placement */

	private fun melonGood(surfaceBlock: Block) = when (surfaceBlock.type) {
		GRASS_BLOCK,
		DIRT,
		-> true
		else -> false
	} && surfaceBlock.getRelative(BlockFace.UP).isPassable
}

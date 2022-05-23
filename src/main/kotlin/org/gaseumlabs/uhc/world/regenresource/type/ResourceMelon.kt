package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.Material.CARVED_PUMPKIN
import org.bukkit.Material.JACK_O_LANTERN
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.world.gen.BiomeNo
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

	/* man, implementing this function yet again */
	fun chunkHasJungle(chunk: Chunk): Boolean {
		val worldHandle = (chunk.world as CraftWorld).handle

		var count = 0

		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, chunk.x * 16 + 2, 70, chunk.z * 16 + 2))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, chunk.x * 16 + 2, 70, chunk.z * 16 + 13))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, chunk.x * 16 + 13, 70, chunk.z * 16 + 2))) ++count
		if (BiomeNo.isJungleBiome(BiomeNo.biomeAt(worldHandle, chunk.x * 16 + 13, 70, chunk.z * 16 + 13))) ++count

		return count >= 3
	}

	override fun generateInChunk(chunk: Chunk, fullVein: Boolean): List<Block>? {
		if (!chunkHasJungle(chunk)) return null

		val melonSurface = surfaceSpreaderOverworld(chunk.world, chunk.x * 16 + 8, chunk.z * 16 + 8, 7, ::melonGood)
		if (melonSurface != null) {
			return listOf(melonSurface.getRelative(BlockFace.UP))
		}

		return null
	}

	override fun setBlock(block: Block, index: Int, fullVein: Boolean) {
		block.setType(if (fullVein) Material.MELON else CARVED_PUMPKIN, false)
	}

	override fun isBlock(block: Block): Boolean {
		return block.type === Material.MELON || block.type === CARVED_PUMPKIN
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

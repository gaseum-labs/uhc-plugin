package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.COW
import org.bukkit.entity.EntityType.HORSE
import org.bukkit.entity.EntityType.LLAMA
import org.bukkit.entity.EntityType.DONKEY
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnHorse
import org.gaseumlabs.uhc.world.WorldManager
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.locateAround
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderOverworld
import org.gaseumlabs.uhc.world.regenresource.ResourceDescriptionEntity
import kotlin.random.Random

class ResourceLeather(
	released: HashMap<PhaseType, Int>,
	chunkRadius: Int,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : ResourceDescriptionEntity(
	released,
	chunkRadius,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	override fun eligable(player: Player): Boolean {
		return player.location.y >= 58
	}

	override fun generateInChunk(chunk: Chunk): List<Block>? {
		val surface = surfaceSpreaderOverworld(chunk.world, chunk.x * 16 + 8, chunk.z * 16 + 8, 7, ::cowHorseGood)
		if (surface != null) {
			return listOf(surface.getRelative(UP))
		}

		return null
	}

	override fun setEntity(block: Block): Entity {
		val entityType = when {
			SpawnUtil.mountains(block.biome) && Random.nextBoolean() -> LLAMA
			SpawnUtil.plains(block.biome) && Random.nextBoolean() -> if (Random.nextInt(3) == 0) DONKEY else HORSE
			else -> COW
		}

		val animal = block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), entityType) as Animals
		animal.setAdult()
		return animal
	}

	override fun isEntity(entity: Entity): Boolean {
		return when (entity.type) {
			COW,
			HORSE,
			LLAMA,
			-> true
			else -> false
		}
	}

	private val spawnHorse = SpawnHorse()

	/* placement */
	private fun cowHorseGood(surfaceBlock: Block): Boolean {
		return spawnHorse.allowSpawn(surfaceBlock.getRelative(UP), 0)
	}
}

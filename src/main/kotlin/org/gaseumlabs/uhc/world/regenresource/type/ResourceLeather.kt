package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Chunk
import org.bukkit.Material.GOLDEN_HORSE_ARMOR
import org.bukkit.Material.SADDLE
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.*
import org.bukkit.entity.EntityType.*
import org.bukkit.inventory.*
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnHorse
import org.gaseumlabs.uhc.world.gen.BiomeNo
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
		val biome = BiomeNo.biomeAt(block)
		val entityType = when {
			BiomeNo.isMountainsBiome(biome) && Random.nextBoolean() -> LLAMA
			BiomeNo.isPlainsBiome(biome) && Random.nextBoolean() -> {
				val random = Random.nextInt(1000)
				/* easter egg */
				when {
					random == 0 -> ZOMBIE_HORSE
					random == 1 -> SKELETON_HORSE
					random == 2 -> MULE
					random < 333 -> DONKEY
					else -> HORSE
				}
			}
			else -> COW
		}

		val animal = block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), entityType) as Animals
		animal.setAdult()
		if (animal is Tameable && Random.nextInt(100) == 0) {
			animal.isTamed = true
			if (animal is InventoryHolder && animal.inventory is ArmoredHorseInventory && Random.nextInt(10) == 0) {
				val inventory = animal.inventory as ArmoredHorseInventory
				inventory.saddle = ItemStack(SADDLE)
				inventory.armor = ItemStack(GOLDEN_HORSE_ARMOR)
			}
		}
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

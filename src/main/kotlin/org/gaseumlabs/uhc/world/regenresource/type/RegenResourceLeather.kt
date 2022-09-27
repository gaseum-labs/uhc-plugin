package org.gaseumlabs.uhc.world.regenresource.type

import org.bukkit.Material.GOLDEN_HORSE_ARMOR
import org.bukkit.Material.SADDLE
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.AbstractHorse
import org.bukkit.entity.Animals
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType.*
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.inventory.ArmoredHorseInventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.customSpawning.spawnInfos.SpawnHorse
import org.gaseumlabs.uhc.world.gen.BiomeNo
import org.gaseumlabs.uhc.world.regenresource.*
import org.gaseumlabs.uhc.world.regenresource.RegenUtil.surfaceSpreaderOverworld
import kotlin.random.Random

class RegenResourceLeather(
	released: HashMap<PhaseType, Int>,
	worldName: String,
	chunkSpawnChance: Float,
	prettyName: String,
) : RegenResourceEntity(
	released,
	worldName,
	chunkSpawnChance,
	prettyName,
) {
	override fun eligible(player: Player) = player.location.y >= 58
	override fun onUpdate(vein: VeinEntity) {}

	override fun generateEntity(genBounds: RegenUtil.GenBounds, fullVein: Boolean) =
		surfaceSpreaderOverworld(genBounds.world, genBounds.centerX(), genBounds.centerZ(), 7) {
			spawnHorse.allowSpawn(it.getRelative(UP), 0)
		}?.let {
			EntityGenResult(it.getRelative(UP), if (fullVein) 1 else 0)
		}

	override fun initializeEntity(block: Block, fullVein: Boolean): Entity {
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
		if (fullVein) animal.setAdult() else animal.setBaby()
		if (animal is Tameable && Random.nextInt(100) == 0) {
			animal.isTamed = true
			if (animal is InventoryHolder && animal.inventory is ArmoredHorseInventory && Random.nextInt(100) == 0) {
				val inventory = animal.inventory as ArmoredHorseInventory
				inventory.saddle = ItemStack(SADDLE)
				inventory.armor = ItemStack(GOLDEN_HORSE_ARMOR)
			}
		}
		return animal
	}

	override fun isModifiedEntity(entity: Entity): Boolean {
		if (entity !is AbstractHorse) return false
		return entity.isTamed
	}

	private val spawnHorse = SpawnHorse()
}

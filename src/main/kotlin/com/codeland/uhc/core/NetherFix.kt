package com.codeland.uhc.core;

import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity

object NetherFix {
	fun placeWart(chunk: Chunk, low: Int, high: Int, check: (Block, Block) -> Boolean) {
		val height = high - low + 1
		val total = 16 * 16 * height
		val offset = (Math.random() * total).toInt()

		for (i in 0 until total) {
			val index = (i + offset) % total
			val x = index % 16
			val z = (index / 16) % 16
			val y = (index / (16 * 16)) + low

			val block = chunk.getBlock(x, y, z)
			val under = chunk.getBlock(x, y - 1, z)

			if (check(block, under)) {
				block.type = Material.NETHER_WART

				val data = block.blockData
				if (data is Ageable) {
					data.age = Util.randRange(0, data.maximumAge)
					block.blockData = data
				}

				under.type = Material.SOUL_SAND

				return
			}
		}
	}

	fun replaceSpawn(entity: Entity): Boolean {
		val location = entity.location
		var world = entity.world

		val chance = if (world.getBiome(location.blockX, location.blockY, location.blockZ) == Biome.BASALT_DELTAS) 0.05 else 0.02

		if (entity is LivingEntity && Math.random() < chance) {
			world.spawnEntity(location, EntityType.BLAZE)

			return true
		}

		return false
	}
}

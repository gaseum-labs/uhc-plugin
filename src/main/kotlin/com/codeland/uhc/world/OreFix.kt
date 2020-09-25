package com.codeland.uhc.world

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.impl.OrePlacer
import com.codeland.uhc.world.chunkPlacer.impl.MineralPlacer
import org.bukkit.Chunk
import org.bukkit.Material

object OreFix {
	fun isOre(type: Material): Boolean {
		return when (type) {
			Material.GOLD_ORE -> true
			Material.LAPIS_ORE -> true
			Material.DIAMOND_ORE -> true
			else -> false
		}
	}

	fun removeOres(chunk: Chunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 1..127) {
					val block = chunk.getBlock(x, y, z)
					if (isOre(block.type)) block.setType(Material.STONE, false)
				}
			}
		}
	}

	val gradientLimit = 42
	val highLimit = 32

	val minerals = arrayOf(
		Material.GRANITE,
		Material.DIORITE,
		Material.ANDESITE,
		Material.DIRT
	)

	fun removeMinerals(chunk: Chunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				/* remove minerals in a gradient */
				for (y in (highLimit + 1)..gradientLimit) {
					val chance = Util.invInterp(highLimit.toFloat(), gradientLimit + 1f, y.toFloat())

					if (Math.random() > chance) {
						val block = chunk.getBlock(x, y, z)

						if (minerals.contains(block.type))
							block.setType(Material.STONE, false)
					}
				}

				/* remove all minerals below and at high limit */
				for (y in 1..highLimit) {
					val block = chunk.getBlock(x, y, z)

					if (minerals.contains(block.type))
						block.setType(Material.STONE, false)
				}
			}
		}
	}

	fun reduceLava(chunk: Chunk) {
		for (x in 0..15) {
			for (z in 0..15) {
				for (y in 9 downTo 0) {
					val block = chunk.getBlock(x, y, z)
					val above = chunk.getBlock(x, y + 1, z)

					if (block.type == Material.LAVA && above.type == Material.LAVA) {
						above.setType(Material.AIR, false)
					}
				}
			}
		}
	}

	val mineralPlacer = MineralPlacer(1, 0)
	val    goldPlacer = OrePlacer(3, 3247892, 1, 32, 5, 8, Material.GOLD_ORE)
	val   lapisPlacer = OrePlacer(4,    9837, 1, 32, 3, 8, Material.LAPIS_ORE)
	val diamondPlacer = OrePlacer(5,  572919, 1, 14, 3, 5, Material.DIAMOND_ORE)
}

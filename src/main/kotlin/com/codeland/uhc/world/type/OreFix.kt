package com.codeland.uhc.world.type

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.impl.OrePlacer
import com.codeland.uhc.world.chunkPlacer.impl.MineralPlacer
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block

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

	fun layerHasEmpty(chunk: Chunk, y: Int): Boolean {
		for (x in 0..15) for (z in 0..15) {
			val block = chunk.getBlock(x, y, z)

			if (block.isPassable) return true
		}

		return false
	}

	fun removeLavaLayer(chunk: Chunk, y: Int) {
		for (x in 0..15) for (z in 0..15) {
			val block = chunk.getBlock(x, y, z)

			if (block.type == Material.LAVA) block.setType(Material.CAVE_AIR, false)
		}
	}

	fun edgeGuardBlock(block: Block) {
		if (block.isPassable) block.setType(Material.STONE, false)
	}

	fun edgeGuardLavaLayer(chunk: Chunk, y: Int) {
		for (x in 0..15) {
			edgeGuardBlock(chunk.getBlock(x, y, 0))
			edgeGuardBlock(chunk.getBlock(x, y, 15))
		}

		for (z in 1..14) {
			edgeGuardBlock(chunk.getBlock(0, y, z))
			edgeGuardBlock(chunk.getBlock(15, y, z))
		}
	}

	fun reduceLava(chunk: Chunk) {
		var lowestY = 9

		for (y in 9 downTo 4) if (layerHasEmpty(chunk, y)) lowestY = y

		for (y in lowestY + 2..10) removeLavaLayer(chunk, y)
		for (y in lowestY..lowestY + 1) edgeGuardLavaLayer(chunk, y)
	}

	val mineralPlacer = MineralPlacer(1, 0)
	val    goldPlacer = OrePlacer(3, 3247892, 5, 32, 5, Material.GOLD_ORE)
	val   lapisPlacer = OrePlacer(4,    9837, 5, 32, 4, Material.LAPIS_ORE)
	val diamondPlacer = OrePlacer(5,  572919, 5, 14, 3, Material.DIAMOND_ORE)
}

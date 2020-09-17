package com.codeland.uhc.core

import com.codeland.uhc.chunkPlacer.ChunkPlacer
import com.codeland.uhc.chunkPlacer.OrePlacer
import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.BlockFace
import kotlin.math.abs
import kotlin.math.sin

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

	val    goldPlacer = OrePlacer(3, 3247892, 10, 32, 5, 8, Material.GOLD_ORE)
	val   lapisPlacer = OrePlacer(4,    9837, 10, 32, 3, 8, Material.LAPIS_ORE)
	val diamondPlacer = OrePlacer(5,  572919, 10, 14, 3, 5, Material.DIAMOND_ORE)

	fun addOres(chunk: Chunk, seed: Int) {
		goldPlacer.place(chunk, seed)
		lapisPlacer.place(chunk, seed)
		diamondPlacer.place(chunk, seed)
	}
}

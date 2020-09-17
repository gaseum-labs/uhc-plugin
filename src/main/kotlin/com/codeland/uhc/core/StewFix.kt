package com.codeland.uhc.core

import com.codeland.uhc.chunkPlacer.CaveMushroomPlacer
import com.codeland.uhc.util.Util
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome

object StewFix {
	fun removeOxeye(chunk: Chunk) {
		//63 121
		val world = chunk.world

		val xzOffset = (Math.random() * 16 * 16).toInt()
		var foundOxeye = false

		for (i in 0 until 16 * 16) {
			val x = (i + xzOffset) % 16
			val z = ((i + xzOffset) / 16) % 16

			for (y in 63..121) {
				val block = chunk.getBlock(x, y, z)

				if (block.type == Material.OXEYE_DAISY) {
					if (foundOxeye) block.setType(Material.AIR, false)
					else foundOxeye = true

					break
				}
			}

			for (y in 63..121) {
				val block = chunk.getBlock(x, y, z)

				if (block.lightLevel > 12 && (block.type == Material.BROWN_MUSHROOM || block.type == Material.RED_MUSHROOM))
					block.setType(Material.AIR, false)
			}
		}
	}

	val redMushroomPlacer = CaveMushroomPlacer(10, 92022, Material.RED_MUSHROOM)
	val brownMushroomPlacer = CaveMushroomPlacer(10, 5021, Material.BROWN_MUSHROOM)

	fun addCaveMushrooms(chunk: Chunk, seed: Int) {
		redMushroomPlacer.place(chunk, seed)
		brownMushroomPlacer.place(chunk, seed)
	}
}

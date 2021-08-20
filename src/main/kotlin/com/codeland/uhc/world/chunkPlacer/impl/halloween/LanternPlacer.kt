package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.Axis
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.data.Orientable
import org.bukkit.block.data.type.Lantern
import kotlin.random.Random

class LanternPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk, chunkIndex: Int) {
		randomPosition(chunk, 20, 99) { block, x, y, z ->
			val world = block.world

			if (!block.isPassable) {
				var maxDepth = 0
				for (yOffset in 1..7) {
					val downBlock = chunk.getBlock(x, y - yOffset, z)

					if (downBlock.type == Material.AIR || downBlock.type == Material.CAVE_AIR)
						++maxDepth
					else
						break
				}

				if (maxDepth >= 4) {
					val chainSize = Random.nextInt(1, maxDepth - 1)

					for (yOffset in 1..chainSize) {
						val downBlock = chunk.getBlock(x, y - yOffset, z)
						downBlock.setType(Material.CHAIN, false)

						val chainData = downBlock.blockData as Orientable
						chainData.axis = Axis.Y
						downBlock.blockData = chainData
					}

					val lanternBlock = chunk.getBlock(x, y - chainSize - 1, z)
					lanternBlock.setType(if (Math.random() < 0.5) Material.LANTERN else Material.SOUL_LANTERN, false)

					val lanternData = lanternBlock.blockData as Lantern
					lanternData.isHanging = true
					lanternBlock.blockData = lanternData

					true

				} else {
					false
				}
			} else {
				false
			}
		}
	}
}

package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.world.chunkPlacer.ImmediateChunkPlacer
import org.bukkit.*
import org.bukkit.block.data.Orientable
import org.bukkit.block.data.type.Lantern
import kotlin.random.Random

class LanternPlacer(size: Int) : ImmediateChunkPlacer(size) {
	override fun place(chunk: Chunk) {
		randomPositionBool(chunk, 20, 99) { block ->
			if (!block.isPassable) {
				var maxDepth = 0
				for (yOffset in 1..7) {
					val downBlock = block.getRelative(0, -yOffset, 0)

					if (downBlock.type == Material.AIR || downBlock.type == Material.CAVE_AIR)
						++maxDepth
					else
						break
				}

				if (maxDepth >= 4) {
					val chainSize = Random.nextInt(1, maxDepth - 1)

					for (yOffset in 1..chainSize) {
						val downBlock = block.getRelative(0, -yOffset, 0)
						downBlock.setType(Material.CHAIN, false)

						val chainData = downBlock.blockData as Orientable
						chainData.axis = Axis.Y
						downBlock.blockData = chainData
					}

					val lanternBlock = block.getRelative(0, -chainSize - 1, 0)
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

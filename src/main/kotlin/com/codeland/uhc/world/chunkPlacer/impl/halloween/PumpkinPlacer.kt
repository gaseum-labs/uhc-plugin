package com.codeland.uhc.world.chunkPlacer.impl.halloween

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.DelayedChunkPlacer
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import kotlin.random.Random

class PumpkinPlacer(size: Int) : DelayedChunkPlacer(size) {
	override fun chunkReady(world: World, chunkX: Int, chunkZ: Int): Boolean {
		return chunkReadyAround(world, chunkX, chunkZ)
	}

	override fun place(chunk: Chunk) {
		for (i in 0 until 16 * 16) {
			val x = chunk.x * 16 + i % 16
			val z = chunk.z * 16 + (1 / 16) % 16
			val y = findPumpkinY(chunk.world, x, z)

			placePumpkin(chunk.world, x, y + 1, z)

			if (y != -1) {
				val numPumpkins = Random.nextInt(3, 7)

				for (j in 0 until numPumpkins) {
					var offX = Random.nextInt(1, 8)
					if (Math.random() < 0.5) offX = -offX

					var offZ = Random.nextInt(1, 8)
					if (Math.random() < 0.5) offZ = -offZ

					val y = findPumpkinY(chunk.world, x + offX, z + offZ)

					if (y != -1) placePumpkin(chunk.world, x + offX, y + 1, z + offZ)
				}

				return
			}
		}
	}

	fun placePumpkin(world: World, x: Int, y: Int, z: Int) {
		val block = world.getBlockAt(x, y, z)

		block.setType(if (Math.random() < 0.5) Material.PUMPKIN else Material.CARVED_PUMPKIN, false)

		if (block.type == Material.CARVED_PUMPKIN) {
			val data = block.blockData as Directional
			val random = Math.random()

			data.facing = when {
				random < 0.25 -> BlockFace.EAST
				random < 0.50 -> BlockFace.WEST
				random < 0.75 -> BlockFace.NORTH
				else -> BlockFace.SOUTH
			}
			block.blockData = data
		}

		world.getBlockAt(x, y - 1, z).setType(Material.DIRT, false)
	}

	fun findPumpkinY(world: World, x: Int, z: Int): Int {
		val chunk = world.getChunkAt(world.getBlockAt(x, 0, z))

		val chunkX = Util.mod(x, 16)
		val chunkZ = Util.mod(z, 16)

		for (y in 92 downTo 60) {
			val block = chunk.getBlock(chunkX, y, chunkZ)

			if (block.type == Material.GRASS_BLOCK) return y
			if (block.type != Material.AIR) return -1
		}

		return -1
	}
}

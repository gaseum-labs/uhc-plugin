package com.codeland.uhc.world.chunkPlacerHolder.type

import com.codeland.uhc.util.Util
import com.codeland.uhc.world.chunkPlacer.AbstractChunkPlacer
import com.codeland.uhc.world.chunkPlacer.impl.OrePlacer
import com.codeland.uhc.world.chunkPlacer.impl.MineralPlacer
import com.codeland.uhc.world.chunkPlacerHolder.ChunkPlacerHolder
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import java.time.LocalDateTime
import kotlin.random.Random

class OreFix : ChunkPlacerHolder() {
	companion object {
		val random = Random(LocalDateTime.now().nano)

		const val GRADIENT_LIMIT = 42
		const val HEIGHT_LIMIT = 32

		val mineralPlacer = MineralPlacer(1)

		val goldPlacer = OrePlacer(3, 5, 32, 5, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE)
		val lapisPlacer = OrePlacer(4, 5, 32, 4, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE)
		val diamondPlacer = OrePlacer(5, 5, 14, 3, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)

		val reverseCoalPlacer =     OrePlacer(1, 63,  240, 6, Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE)
		val reverseIronPlacer =     OrePlacer(1, 63,  240, 6, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE)
		val reverseRedstonePlacer = OrePlacer(2, 100, 240, 5, Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE)
		val reverseCopperPlacer =   OrePlacer(2, 100, 240, 4, Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE)
		val reverseGoldPlacer =     OrePlacer(2, 150, 240, 4, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE)
		val reverseLapisPlacer =    OrePlacer(3, 150, 240, 3, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE)
		val reverseDiamondPlacer =  OrePlacer(4, 200, 240, 1, Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE)

		fun isOre(block: Block): Boolean {
			return when (block.type) {
				Material.GOLD_ORE -> true
				Material.LAPIS_ORE -> true
				Material.DIAMOND_ORE -> true
				Material.DEEPSLATE_GOLD_ORE -> true
				Material.DEEPSLATE_LAPIS_ORE -> true
				Material.DEEPSLATE_DIAMOND_ORE -> true
				else -> false
			}
		}

		fun isMineral(block: Block): Boolean {
			return when (block.type) {
				Material.GRANITE -> true
				Material.DIORITE -> true
				Material.ANDESITE ->  true
				Material.TUFF -> true
				else -> false
			}
		}

		fun removeOres(chunk: Chunk) {
			for (x in 0..15) for (z in 0..15) for (y in 1..127) {
				val block = chunk.getBlock(x, y, z)
				if (isOre(block)) block.setType(Material.STONE, false)
			}
		}

		fun removeMinerals(chunk: Chunk) {
			for (x in 0..15) {
				for (z in 0..15) {
					/* remove minerals in a gradient above the height limit */
					for (y in (HEIGHT_LIMIT + 1)..GRADIENT_LIMIT) {
						if (
							random.nextFloat() > Util.invInterp(HEIGHT_LIMIT.toFloat(), GRADIENT_LIMIT + 1f, y.toFloat())
						) {
							val block = chunk.getBlock(x, y, z)
							if (isMineral(block)) block.setType(Material.STONE, false)
						}
					}

					/* remove all minerals below and at height limit */
					for (y in 1..HEIGHT_LIMIT) {
						val block = chunk.getBlock(x, y, z)
						if (isMineral(block)) block.setType(Material.STONE, false)
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

				if (block.type === Material.LAVA) block.setType(Material.CAVE_AIR, false)
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
	}

	override fun list(): Array<AbstractChunkPlacer> = arrayOf(
		mineralPlacer,
		goldPlacer,
		lapisPlacer,
		diamondPlacer
	)
}

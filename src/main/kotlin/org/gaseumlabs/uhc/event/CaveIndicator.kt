package org.gaseumlabs.uhc.event

import org.gaseumlabs.uhc.util.extensions.BlockFaceExtensions.left
import org.gaseumlabs.uhc.util.extensions.BlockFaceExtensions.right
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import kotlin.random.Random

class CaveIndicator : Listener {
	@EventHandler
	fun onBreakBlock(event: BlockBreakEvent) {
		val blockDirection = event.player.facing

		val back = event.block.getRelative(blockDirection)

		if (
			stoneLike(back) &&
			!back.getRelative(blockDirection).isPassable &&
			!back.getRelative(BlockFace.UP).isPassable &&
			!back.getRelative(BlockFace.DOWN).isPassable &&
			!back.getRelative(blockDirection.left()).isPassable &&
			!back.getRelative(blockDirection.right()).isPassable
		) {
			if (Random.nextInt(12) == 0) {
				val caveDirection = findCave(back, blockDirection.oppositeFace) ?: return
				if (caveDirection !== blockDirection) back.setType(faceToMineral(caveDirection), false)
			} else if (replaceable(back)) {
				back.setType(Material.STONE, false)
			}
		}
	}

	private fun faceToMineral(blockFace: BlockFace): Material {
		return when (blockFace) {
			BlockFace.EAST -> Material.DIORITE
			BlockFace.NORTH -> Material.GRANITE
			BlockFace.WEST -> Material.TUFF
			else -> Material.ANDESITE
		}
	}

	private fun findCave(block: Block, ignore: BlockFace): BlockFace? {
		return arrayOf(BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH)
			.filter { it !== ignore }
			.map { direction ->
				var current = block

				for (i in 0 until 24) {
					current = current.getRelative(direction.modX * 2, 0, direction.modZ * 2)
					if (current.isPassable) return@map Pair(direction, i)
				}

				Pair(direction, null)
			}
			.mapNotNull { (direction, distance) ->
				if (distance == null) null else Pair(direction, distance)
			}
			.minByOrNull { (_, distance) ->
				distance
			}
			?.first
	}

	private fun replaceable(block: Block): Boolean {
		return when (block.type) {
			Material.ANDESITE,
			Material.DIORITE,
			Material.GRANITE,
			Material.TUFF,
			-> true
			else -> false
		}
	}

	private fun stoneLike(block: Block): Boolean {
		return when (block.type) {
			Material.ANDESITE,
			Material.DIORITE,
			Material.GRANITE,
			Material.TUFF,
			Material.DEEPSLATE,
			Material.DIRT,
			Material.STONE,
			-> true
			else -> false
		}
	}
}

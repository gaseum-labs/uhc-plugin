package org.gaseumlabs.uhc.lobbyPvp

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.gaseumlabs.uhc.util.BlockPos
import org.gaseumlabs.uhc.util.KeyGen
import org.gaseumlabs.uhc.util.createSuperArrayDataType
import java.util.ArrayList
import java.util.UUID

data class PlatformStorage(
	val owner: UUID,
	val corner0: BlockPos,
	val corner1: BlockPos,
	val name: String
) {
	companion object {
		val key = KeyGen.genKey("platform_definitions")
		val dataType = createSuperArrayDataType<PlatformStorage>()
	}
}

data class Platform(
	val storage: PlatformStorage,
	val width: Int,
	val height: Int,
	val upperLayer: Array<BlockData>,
	val platformLayer: Array<BlockData>,
	val lowerLayer: Array<BlockData>,
	val startPositions: ArrayList<Pair<Int, Int>>,
) {
	val owner by storage::owner
	val name by storage::name

	companion object {
		val airData = Material.AIR.createBlockData()

		fun fromStorage(world: World, storage: PlatformStorage): Platform {
			val (minCorner, maxCorner) = BlockPos.bounds(storage.corner0, storage.corner1)

			val (left, y0, up)  = minCorner
			val (right, y1, down)  = maxCorner

			if (y0 != y1) throw Exception("Platform main layer needs to be 1 block thick")

			val width = right - left + 1
			val height = down - up + 1

			if (width < 16 || height < 16) throw Exception("Min dimensions for arena is 16*16")
			if (width > 48 || height > 48) throw Exception("Max dimensions for arena is 48*48")

			val startPositions = ArrayList<Pair<Int, Int>>()

			val upperLayer = Array(width * height) { i ->
				val z = i / width
				val x = i % width

				val block = world.getBlockAt(left + x, y0 + 1, up + z)
				if (block.isSolid) airData else block.blockData
			}

			val platformBlockDatas = Array(width * height) { i ->
				val z = i / width
				val x = i % width

				if (
					(left + x == storage.corner0.x && up + z == storage.corner0.z) ||
					(left + x == storage.corner1.x && up + z == storage.corner1.z)
				) {
					airData
				} else {
					val block = world.getBlockAt(left + x, y0, up + z)

					if (block.type === Material.EMERALD_BLOCK) startPositions.add(x to z)

					block.blockData
				}
			}

			val lowerLayer = Array(width * height) { i ->
				val z = i / width
				val x = i % width

				val block = world.getBlockAt(left + x, y0 - 1, up + z)
				if (block.isSolid) airData else block.blockData
			}

			if (startPositions.size < 4)
				throw Exception("Need at least 4 starting positions (Emerald block) (Not on corners)")

			return Platform(
				storage,
				width,
				height,
				upperLayer,
				platformBlockDatas,
				lowerLayer,
				startPositions,
			)
		}
	}
}

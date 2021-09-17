package com.codeland.uhc.extensions

import org.bukkit.block.BlockFace

object BlockFaceExtensions {
	fun BlockFace.left(): BlockFace {
		return when (this) {
			BlockFace.NORTH -> BlockFace.WEST
			BlockFace.WEST -> BlockFace.SOUTH
			BlockFace.SOUTH -> BlockFace.EAST
			BlockFace.EAST -> BlockFace.NORTH
			else -> this
		}
	}

	fun BlockFace.right(): BlockFace {
		return when (this) {
			BlockFace.NORTH -> BlockFace.EAST
			BlockFace.EAST -> BlockFace.SOUTH
			BlockFace.SOUTH -> BlockFace.WEST
			BlockFace.WEST -> BlockFace.NORTH
			else -> this
		}
	}
}

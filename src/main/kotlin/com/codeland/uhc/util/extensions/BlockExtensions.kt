package com.codeland.uhc.util.extensions

import org.bukkit.block.Block

object BlockExtensions {
	fun Block.samePlace(other: Block): Boolean {
		return this.x == other.x && this.y == other.y && this.z == other.z
	}
}

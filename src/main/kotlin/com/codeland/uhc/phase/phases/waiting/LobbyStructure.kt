package com.codeland.uhc.phase.phases.waiting

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.type.Bamboo

object LobbyStructure {
	class UBT {
		//val width: Int
		//val height: Int
		//val depth: Int
		//lateinit var blocks: Array<Material>
	}

	fun loadNBT(world: World) {
		val block = world.getBlockAt(0, 80, 0)

		block.blockData.asString
	}
}
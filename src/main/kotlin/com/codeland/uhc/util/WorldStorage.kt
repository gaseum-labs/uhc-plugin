package com.codeland.uhc.util

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Structure
import org.bukkit.block.structure.UsageMode

object WorldStorage {
	fun destroy(world: World, x: Int, z: Int) {
		val infoBlock = world.getBlockAt(x, 0, z)
		infoBlock.setType(Material.BEDROCK, false)
	}

	fun save(world: World, x: Int, z: Int, data: String) {
		val infoBlock = world.getBlockAt(x, 0, z)

		infoBlock.setType(Material.STRUCTURE_BLOCK, false)
		val state = infoBlock.getState(false) as Structure

		state.usageMode = UsageMode.DATA
		state.metadata = data
	}

	fun load(world: World, x: Int, z: Int): String? {
		val infoBlock = world.getBlockAt(x, 0, z)

		if (infoBlock.type !== Material.STRUCTURE_BLOCK) return null
		val state = infoBlock.getState(false) as Structure

		if (state.usageMode !== UsageMode.DATA) return null
		return state.metadata
	}

	fun loadOrPut(world: World, x: Int, z: Int, defaultData: String): String {
		val existing = load(world, x, z)

		return if (existing != null) {
			existing
		} else {
			save(world, x, z, defaultData)
			defaultData
		}
	}
}

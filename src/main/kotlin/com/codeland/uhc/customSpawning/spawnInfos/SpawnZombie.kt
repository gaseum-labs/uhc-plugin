package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.CustomSpawning
import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Drowned
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack

class SpawnZombie : SpawnInfo() {
	override fun allowSpawn(block: Block, spawnCycle: Int): Pair<EntityType, Boolean>? {
		if (block.lightLevel > 7) return null
		if (!spawnFloor(block.getRelative(BlockFace.DOWN))) return null
		if (spawnObstacle(block) || spawnObstacle(block.getRelative(BlockFace.UP))) return null

		return if (block.biome == Biome.DESERT || block.biome == Biome.DESERT_HILLS || block.biome == Biome.DESERT_LAKES)
			reg(EntityType.HUSK)
		else if (isWater(block) && isWater(block.getRelative(BlockFace.UP)))
			reg(EntityType.DROWNED)
		else
			reg(EntityType.ZOMBIE)
	}

	override fun onSpawn(block: Block, spawnCycle: Int, entity: Entity) {
		val zombie = entity as Zombie

		zombie.setAdult()
		zombie.canPickupItems = false
		zombie.equipment?.clear()

		if (zombie is Drowned) {
			if (onCycle(spawnCycle, 4)) zombie.equipment?.setItemInMainHand(ItemStack(Material.TRIDENT))

		} else if (onCycle(spawnCycle, 5)) {
			applyEquipment(zombie.equipment)
		}
	}

	companion object {
		fun applyEquipment(equipment: EntityEquipment?) {
			if (equipment == null) return

			val random = Util.randRange(0, 4)
			when (random) {
				0 -> equipment.boots = ItemUtil.halfDamagedItem(Material.IRON_BOOTS)
				1 -> equipment.leggings = ItemUtil.halfDamagedItem(Material.IRON_LEGGINGS)
				2 -> equipment.chestplate = ItemUtil.halfDamagedItem(Material.IRON_CHESTPLATE)
				3 -> equipment.helmet = ItemUtil.halfDamagedItem(Material.IRON_HELMET)
				4 -> equipment.setItemInMainHand(ItemUtil.halfDamagedItem(Material.IRON_SWORD))
			}
		}
	}
}

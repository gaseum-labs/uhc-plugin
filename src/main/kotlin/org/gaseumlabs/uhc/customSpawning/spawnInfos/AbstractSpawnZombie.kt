package org.gaseumlabs.uhc.customSpawning.spawnInfos;

import org.gaseumlabs.uhc.customSpawning.SpawnInfo
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.gaseumlabs.uhc.util.ItemUtil
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.bukkit.entity.*
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.random.Random

abstract class AbstractSpawnZombie<E : Zombie>(type: Class<E>) : SpawnInfo<E>(type, Vector(0.5, 0.0, 0.5), false) {
	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.lightFilter(block, SpawnUtil.MONSTER_LIGHT_LEVEL) &&
		SpawnUtil.spawnFloor(block.getRelative(DOWN)) &&
		!SpawnUtil.spawnObstacle(block) &&
		!SpawnUtil.spawnObstacle(block.getRelative(UP))
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: E) {
		entity.setAdult()
		entity.canPickupItems = false

		val equipment = entity.equipment ?: return
		equipment.clear()
		giveEquipment(count, equipment)
	}

	open fun giveEquipment(count: Int, equipment: EntityEquipment) {
		if (SpawnUtil.onCycle(count, 3)) giveArmor(equipment)
		if (SpawnUtil.onCycle(count, 5)) giveWeapon(equipment)
	}

	companion object {
		fun giveArmor(equipment: EntityEquipment) {
			when (Random.nextInt(8)) {
				0 -> equipment.setBoots(ItemUtil.randomDamagedItem(Material.IRON_BOOTS), true)
				1 -> equipment.setLeggings(ItemUtil.randomDamagedItem(Material.IRON_LEGGINGS), true)
				2 -> equipment.setChestplate(ItemUtil.randomDamagedItem(Material.IRON_CHESTPLATE), true)
				3 -> equipment.setHelmet(ItemUtil.randomDamagedItem(Material.IRON_HELMET), true)
				4 -> equipment.setBoots(ItemUtil.randomDamagedItem(Material.CHAINMAIL_BOOTS), true)
				5 -> equipment.setLeggings(ItemUtil.randomDamagedItem(Material.CHAINMAIL_LEGGINGS), true)
				6 -> equipment.setChestplate(ItemUtil.randomDamagedItem(Material.CHAINMAIL_CHESTPLATE), true)
				else -> equipment.setHelmet(ItemUtil.randomDamagedItem(Material.CHAINMAIL_HELMET), true)
			}
		}

		fun giveWeapon(equipment: EntityEquipment) {
			equipment.setItemInMainHand(when (Random.nextInt(5)) {
				0 -> ItemUtil.randomDamagedItem(Material.IRON_PICKAXE)
				1 -> ItemUtil.randomDamagedItem(Material.IRON_SWORD)
				2 -> ItemUtil.randomDamagedItem(Material.IRON_SHOVEL)
				3 -> ItemUtil.randomDamagedItem(Material.IRON_HOE)
				else -> ItemUtil.randomDamagedItem(Material.IRON_AXE)
			}, true)
		}
	}
}

class SpawnZombie : AbstractSpawnZombie<Zombie>(Zombie::class.java)

class SpawnHusk : AbstractSpawnZombie<Husk>(Husk::class.java)

class SpawnZombieVillager : AbstractSpawnZombie<ZombieVillager>(ZombieVillager::class.java) {
	override fun giveEquipment(count: Int, equipment: EntityEquipment) {}
}

class SpawnDrowned : AbstractSpawnZombie<Drowned>(Drowned::class.java) {
	override fun giveEquipment(count: Int, equipment: EntityEquipment) {
		if (SpawnUtil.onCycle(count, 4)) {
			equipment.setItemInMainHand(ItemStack(Material.TRIDENT), true)
		}
	}
}

package org.gaseumlabs.uhc.customSpawning.spawnInfos

import org.gaseumlabs.uhc.customSpawning.SpawnInfo
import org.gaseumlabs.uhc.customSpawning.SpawnUtil
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

abstract class SpawnAnimal<E : Animals>(type: Class<E>, offset: Double = 0.5) :
	SpawnInfo<E>(type, Vector(offset, 0.0, offset), false) {

	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return floor(block.getRelative(DOWN)) && box(block) && block.y >= SpawnUtil.SURFACE_Y
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: E) {
		entity.setAdult()
		entity.removeWhenFarAway = true
	}

	abstract fun box(block: Block): Boolean
	abstract fun floor(block: Block): Boolean
}

class SpawnChicken : SpawnAnimal<Chicken>(Chicken::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnParrot : SpawnAnimal<Parrot>(Parrot::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnRabbit : SpawnAnimal<Rabbit>(Rabbit::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnPig : SpawnAnimal<Pig>(Pig::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnPanda : SpawnAnimal<Panda>(Panda::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.wideTallSpawnBox(block)
	override fun floor(block: Block) = SpawnUtil.wideAnimalSpawnFloor(block)
}

class SpawnPolarBear : SpawnAnimal<PolarBear>(PolarBear::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.wideTallSpawnBox(block)
	override fun floor(block: Block) = SpawnUtil.wideAnimalSpawnFloor(block)
}

class SpawnTurtle : SpawnAnimal<Turtle>(Turtle::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.wideSpawnBox(block)
	override fun floor(block: Block) = SpawnUtil.wideAnimalSpawnFloor(block)
}

class SpawnSheep : SpawnAnimal<Sheep>(Sheep::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnGoat : SpawnAnimal<Goat>(Goat::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnWolf : SpawnAnimal<Wolf>(Wolf::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnFox : SpawnAnimal<Fox>(Fox::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: Fox) {
		super.onSpawn(block, count, player, entity)
		entity.canPickupItems = false
		entity.equipment.setItemInMainHand(ItemStack(when (count % 4) {
			0 -> Material.EMERALD
			1 -> Material.FEATHER
			2 -> Material.SUGAR_CANE
			else -> Material.LEATHER
		}), true)
		entity.equipment.itemInMainHandDropChance = 1.0f
	}
}

class SpawnOcelot : SpawnAnimal<Ocelot>(Ocelot::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

/* only used in LeatherRegen */

class SpawnCow : SpawnAnimal<Cow>(Cow::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

class SpawnHorse : SpawnAnimal<Horse>(Horse::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.wideTallSpawnBox(block)
	override fun floor(block: Block) = SpawnUtil.wideAnimalSpawnFloor(block)
}

class SpawnDonkey : SpawnAnimal<Donkey>(Donkey::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.wideTallSpawnBox(block)
	override fun floor(block: Block) = SpawnUtil.wideAnimalSpawnFloor(block)
}

class SpawnLlama : SpawnAnimal<Llama>(Llama::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
	override fun floor(block: Block) = SpawnUtil.animalSpawnFloor(block)
}

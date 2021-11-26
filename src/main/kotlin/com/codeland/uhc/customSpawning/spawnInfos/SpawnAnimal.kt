package com.codeland.uhc.customSpawning.spawnInfos

import com.codeland.uhc.customSpawning.SpawnInfo
import com.codeland.uhc.customSpawning.SpawnUtil
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.entity.*
import org.bukkit.util.Vector

abstract class SpawnAnimal<E : Animals>(type: Class<E>, offset: Double = 0.5) :
	SpawnInfo<E>(type, Vector(offset, 0.0, offset), false) {

	override fun allowSpawn(block: Block, spawnCycle: Int): Boolean {
		return SpawnUtil.animalSpawnFloor(block.getRelative(DOWN)) && box(block)
	}

	override fun onSpawn(block: Block, count: Int, player: Player?, entity: E) {
		entity.setAdult()
	}

	abstract fun box(block: Block): Boolean
}

class SpawnChicken : SpawnAnimal<Chicken>(Chicken::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
}

class SpawnParrot : SpawnAnimal<Parrot>(Parrot::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
}

class SpawnRabbit : SpawnAnimal<Rabbit>(Rabbit::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
}

class SpawnPig : SpawnAnimal<Pig>(Pig::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
}

class SpawnPanda : SpawnAnimal<Panda>(Panda::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.offsetTallSpawnBox(block)
}

class SpawnPolarBear : SpawnAnimal<PolarBear>(PolarBear::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.offsetTallSpawnBox(block)
}

class SpawnTurtle : SpawnAnimal<Turtle>(Turtle::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.offsetShortSpawnBox(block)
}

class SpawnSheep : SpawnAnimal<Sheep>(Sheep::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
}

class SpawnGoat : SpawnAnimal<Goat>(Goat::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
}

class SpawnWolf : SpawnAnimal<Wolf>(Wolf::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
}

class SpawnFox : SpawnAnimal<Fox>(Fox::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
}

class SpawnOcelot : SpawnAnimal<Ocelot>(Ocelot::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnIn(block)
}

/* only used in LeatherRegen */

class SpawnCow : SpawnAnimal<Cow>(Cow::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
}

class SpawnHorse : SpawnAnimal<Horse>(Horse::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.offsetTallSpawnBox(block)
}

class SpawnDonkey : SpawnAnimal<Donkey>(Donkey::class.java, 1.0) {
	override fun box(block: Block) = SpawnUtil.offsetTallSpawnBox(block)
}

class SpawnLlama : SpawnAnimal<Llama>(Llama::class.java) {
	override fun box(block: Block) = SpawnUtil.spawnBox(block)
}

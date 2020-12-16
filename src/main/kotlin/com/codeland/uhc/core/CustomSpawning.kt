package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.math.*

object CustomSpawning {
	class SpawnInfo(val allowSpawn: (Block, Int) -> EntityType?, val onSpawn: (Entity, Int) -> Unit)

	fun spawnSpace(block: Block, xBox: Int, yHeight: Int, zBox: Int): Boolean {
		val xRadius = (xBox - 1) / 2
		val zRadius = (zBox - 1) / 2

		/* standing on a solid block */
		if (block.getRelative(BlockFace.DOWN).isPassable) return false

		/* in a radius around check if all empty */
		for (x in -xRadius..xRadius)
			for (z in -zRadius..zRadius)
				for (y in 0 until yHeight) {
					val block = block.world.getBlockAt(block.x + x, block.y + y, block.z + z)
					if (!block.isPassable || block.isLiquid) return false
				}

		return true
	}

	fun regularAllowSpawn(block: Block, lightLevel: Int): Boolean {
		if (block.lightLevel > lightLevel) return false

		return spawnSpace(block, 1, 2, 1)
	}

	fun zombieAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (block.lightLevel > 7) return null
		if (!block.isPassable || !block.getRelative(BlockFace.UP).isPassable) return null
		if (block.getRelative(BlockFace.DOWN).isPassable) return null

		return if (block.biome == Biome.DESERT || block.biome == Biome.DESERT_HILLS || block.biome == Biome.DESERT_LAKES) EntityType.HUSK
		else if (block.type == Material.WATER && block.getRelative(BlockFace.UP).type == Material.WATER) EntityType.DROWNED
		else EntityType.ZOMBIE
	}

	fun creeperAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (!regularAllowSpawn(block, 7)) return null

		return if (onCycle(spawnCycle, 20)) EntityType.WITCH else EntityType.CREEPER
	}

	fun skeletonAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (block.lightLevel > 7) return null

		return if (onCycle(spawnCycle, 10))
			if (spawnSpace(block, 1, 3, 1)) EntityType.ENDERMAN else null
		else
			if (spawnSpace(block, 1, 2, 1)) EntityType.SKELETON else null
	}

	fun spiderAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (block.lightLevel > 7) return null

		return if (spawnSpace(block, 3, 1, 3)) EntityType.SPIDER else null
	}

	fun onSpawnNothing(entity: Entity, spawnCycle: Int) {}

	fun onSpawnZombie(entity: Entity, spawnCycle: Int) {
		val zombie = entity as Zombie

		zombie.isBaby = false

		if (zombie is Drowned) {
			if (onCycle(spawnCycle, 4)) zombie.equipment?.setItemInMainHand(ItemStack(Material.TRIDENT))
			else zombie.equipment?.setItemInMainHand(null)
		}
	}

	fun onSpawnSpider(entity: Entity, spawnCycle: Int) {
		val spider = entity as Spider

		val passengers = entity.passengers

		if (passengers.isNotEmpty()) entity.removePassenger(passengers[0])
	}

	fun netherDefaultAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return when (block.biome) {
			Biome.CRIMSON_FOREST -> if (onCycle(spawnCycle, 3)) hoglinAllowSpawn(block, spawnCycle) else piglinAllowSpawn(block, spawnCycle)
			Biome.BASALT_DELTAS -> magmaCubeAllowSpawn(block, spawnCycle)
			Biome.SOUL_SAND_VALLEY -> skeletonAllowSpawn(block, spawnCycle)
			Biome.WARPED_FOREST -> endermanAllowSpawn(block, spawnCycle)
			else -> zombiePiglinAllowSpawn(block, spawnCycle)
		}
	}

	fun hoglinAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (spawnSpace(block, 3, 2, 3) && block.getRelative(BlockFace.DOWN).type != Material.NETHER_WART_BLOCK) EntityType.HOGLIN else null
	}

	fun zombiePiglinAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (regularAllowSpawn(block, 11)) EntityType.ZOMBIFIED_PIGLIN else null
	}

	fun netherSpecialSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (spawnCycle % 3 == 0) {
			blazeAllowSpawn(block, spawnCycle)

		} else when (spawnCycle % 6) {
			1 -> ghastAllowSpawn(block, spawnCycle)
			2 -> magmaCubeAllowSpawn(block, spawnCycle)
			4 -> zombiePiglinAllowSpawn(block, spawnCycle)
			else -> endermanAllowSpawn(block, spawnCycle)
		}
	}

	fun ghastAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (spawnSpace(block, 5, 4, 5)) EntityType.GHAST else null
	}

	fun magmaCubeAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (spawnSpace(block, 3, 2, 3)) EntityType.MAGMA_CUBE else null
	}

	fun endermanAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		return if (block.lightLevel <= 7 && spawnSpace(block, 1, 3, 1)) EntityType.ENDERMAN else null
	}

	fun piglinAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (!regularAllowSpawn(block, 11)) return null

		return EntityType.PIGLIN
	}

	fun blazeAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (!spawnSpace(block, 1, 2, 1)) return null

		return EntityType.BLAZE
	}

	fun onNetherDefaultSpawn(entity: Entity, spawnCycle: Int) {
		if (entity is PigZombie) entity.isBaby = false
		else if (entity is Piglin) entity.isBaby = false
		else if (entity is MagmaCube) entity.size = Util.randRange(1, 2)
	}

	fun onPiglinSpawn(entity: Entity, spawnCycle: Int) {
		(entity as Piglin).isBaby = false
	}

	val overworldSpawnInfoList = arrayOf(
		SpawnInfo(::zombieAllowSpawn, ::onSpawnZombie),
		SpawnInfo(::skeletonAllowSpawn, ::onSpawnNothing),
		SpawnInfo(::creeperAllowSpawn, ::onSpawnNothing),
		SpawnInfo(::spiderAllowSpawn, ::onSpawnSpider)
	)

	val netherSpawnInfoList = arrayOf(
		SpawnInfo(::netherDefaultAllowSpawn, ::onNetherDefaultSpawn),
		SpawnInfo(::netherDefaultAllowSpawn, ::onNetherDefaultSpawn),
		SpawnInfo(::netherDefaultAllowSpawn, ::onNetherDefaultSpawn),
		SpawnInfo(::netherDefaultAllowSpawn, ::onNetherDefaultSpawn),
		SpawnInfo(::netherSpecialSpawn,      ::onNetherDefaultSpawn),
		SpawnInfo(::piglinAllowSpawn,        ::onPiglinSpawn),
	)

	fun spawnForPlayer(player: Player) {
		val playerData = GameRunner.uhc.getPlayerData(player.uniqueId)

		val world = player.world
		val infoList = if (world.environment == World.Environment.NETHER) netherSpawnInfoList else overworldSpawnInfoList
		if (playerData.spawnIndex > infoList.lastIndex) playerData.spawnIndex = 0

		if (spawnMob(player, playerData.spawnCycle, infoList[playerData.spawnIndex])) {
			++playerData.spawnIndex

			if (playerData.spawnIndex == infoList.size) {
				++playerData.spawnCycle
				playerData.spawnIndex = 0
			}
		}
	}

	const val MIN_RADIUS = 24
	const val MAX_RADIUS = 86
	const val VERTICAL_RADIUS = 10

	const val PER_PLAYER = 30

	fun spawnMob(player: Player, spawnCycle: Int, spawnInfo: SpawnInfo): Boolean {
		val angle = Math.random() * 2 * PI

		val radius = Util.randRange(MIN_RADIUS, MAX_RADIUS)

		val radiusDistance = MAX_RADIUS - MIN_RADIUS
		val offsetRadius = (Math.random() * radiusDistance).toInt()

		for (j in 0 until radiusDistance step 2) {
			val radius =  (j + offsetRadius) % (radiusDistance) + MIN_RADIUS

			val blockX = (cos(angle) * radius + player.location.x).toInt()
			val blockZ = (sin(angle) * radius + player.location.z).toInt()

			val minY = (player.location.y - VERTICAL_RADIUS).toInt().coerceAtLeast(0).coerceAtMost(255)
			val maxY = (player.location.y + VERTICAL_RADIUS).toInt().coerceAtMost(255).coerceAtLeast(0)

			val distance = maxY - minY
			val offsetY = (Math.random() * distance).toInt()

			val world = player.world

			for (i in 0 until distance) {
				val y = (i + offsetY) % (distance) + minY
				val block = world.getBlockAt(blockX, y, blockZ)

				val entityType = spawnInfo.allowSpawn(block, spawnCycle)

				if (entityType != null) {
					val entity = world.spawnEntity(block.location.add(0.5, 0.0, 0.5), entityType)
					Util.log("spawned ${entityType.name} at ${entity.location.x} ${entity.location.y} ${entity.location.z} for ${player.name}")
					spawnInfo.onSpawn(entity, spawnCycle)
					makePlayerMob(entity, player)

					return true
				}
			}
		}

		return false
	}

	const val SPAWN_TAG = "_UHC_SPAWN"

	fun makePlayerMob(entity: Entity, player: Player) {
		entity.setMetadata(SPAWN_TAG, FixedMetadataValue(UHCPlugin.plugin, player.uniqueId))
	}

	fun isPlayerMob(entity: Entity, player: Player): Boolean {
		if (entity !is Monster) return false

		val meta = entity.getMetadata(SPAWN_TAG)

		return meta.isNotEmpty() && (meta[0].value() as UUID) == player.uniqueId
	}

	var spawnTaskID = -1

	fun startTask() {
		spawnTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			GameRunner.uhc.allCurrentPlayers { uuid ->
				val player = Bukkit.getPlayer(uuid)

				if (player != null) {
					val entities = player.world.entities

					var count = 0
					player.world.entities.forEach { entity ->
						if (isPlayerMob(entity, player)) ++count
					}

					if (count < PER_PLAYER) spawnForPlayer(player)
				}
			}
		}, 0, 5)
	}

	fun endTask() {
		Bukkit.getScheduler().cancelTask(spawnTaskID)
		spawnTaskID = -1
	}

	fun onCycle(spawnCycle: Int, n: Int): Boolean {
		return spawnCycle != 0 && spawnCycle % n == 0
	}
}

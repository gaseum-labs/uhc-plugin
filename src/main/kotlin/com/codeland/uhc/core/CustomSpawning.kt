package com.codeland.uhc.core

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.phase.PhaseType
import com.codeland.uhc.util.ItemUtil
import com.codeland.uhc.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.*
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.math.*

object CustomSpawning {
	class SpawnInfo(val allowSpawn: (Block, Int) -> EntityType?, val onSpawn: (Entity, Int) -> Unit)

	fun isWater(block: Block): Boolean {
		return block.type == Material.WATER ||
			block.type == Material.KELP ||
			block.type == Material.SEAGRASS ||
			block.type == Material.TALL_SEAGRASS ||
			((block.blockData as? Waterlogged)?.isWaterlogged == true)

	}

	fun spawnObstacle(block: Block): Boolean {
		return !block.isPassable || block.type == Material.LAVA
	}

	fun spawnFloor(block: Block): Boolean {
		return !block.isPassable
	}

	fun regularAllowSpawn(block: Block, lightLevel: Int): Boolean {
		if (block.lightLevel > lightLevel) return false

		return spawnSpace(block, 1, 2, 1)
	}

	fun spawnSpace(block: Block, xBox: Int, yHeight: Int, zBox: Int): Boolean {
		val xRadius = (xBox - 1) / 2
		val zRadius = (zBox - 1) / 2

		/* standing on a solid block */
		if (!spawnFloor(block.getRelative(BlockFace.DOWN))) return false

		/* in a radius around check if all empty */
		for (x in -xRadius..xRadius)
			for (z in -zRadius..zRadius)
				for (y in 0 until yHeight) {
					val block = block.world.getBlockAt(block.x + x, block.y + y, block.z + z)
					if (spawnObstacle(block) || isWater(block)) return false
				}

		return true
	}

	fun zombieAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (block.lightLevel > 7) return null
		if (!spawnFloor(block.getRelative(BlockFace.DOWN))) return null
		if (spawnObstacle(block) || spawnObstacle(block.getRelative(BlockFace.UP))) return null

		return if (block.biome == Biome.DESERT || block.biome == Biome.DESERT_HILLS || block.biome == Biome.DESERT_LAKES) EntityType.HUSK
		else if (isWater(block) && isWater(block.getRelative(BlockFace.UP))) EntityType.DROWNED
		else EntityType.ZOMBIE
	}

	fun creeperAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (!regularAllowSpawn(block, 7)) return null

		return if (onCycle(spawnCycle, 40)) EntityType.WITCH else EntityType.CREEPER
	}

	fun skeletonAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (block.lightLevel > 7) return null

		return if (onCycle(spawnCycle, 10)) {
			if (spawnSpace(block, 1, 3, 1)) EntityType.ENDERMAN else null

		} else if (spawnSpace(block, 1, 2, 1)) {
			if (block.biome == Biome.SNOWY_TUNDRA || block.biome == Biome.SNOWY_MOUNTAINS || block.biome == Biome.ICE_SPIKES || block.biome == Biome.FROZEN_RIVER) EntityType.STRAY
			else EntityType.SKELETON

		} else {
			null
		}
	}

	fun spiderAllowSpawn(block: Block, spawnCycle: Int): EntityType? {
		if (block.lightLevel > 7) return null

		return if (spawnSpace(block, 3, 1, 3)) EntityType.SPIDER else null
	}

	fun onSpawnNothing(entity: Entity, spawnCycle: Int) {}

	fun onSpawnZombie(entity: Entity, spawnCycle: Int) {
		val zombie = entity as Zombie

		zombie.isBaby = false
		zombie.canPickupItems = false

		if (zombie is Drowned) {
			if (onCycle(spawnCycle, 4)) zombie.equipment?.setItemInMainHand(ItemStack(Material.TRIDENT))
			else zombie.equipment?.setItemInMainHand(null)

		} else if (onCycle(spawnCycle, 5)) {
			applyEquipment(zombie.equipment)
		}
	}

	fun applyEquipment(equipment: EntityEquipment?) {
		if (equipment == null) return
		equipment.clear()

		val random = Util.randRange(0, 4)
		when (random) {
			0 -> equipment.boots = ItemUtil.halfDamagedItem(Material.IRON_BOOTS)
			1 -> equipment.leggings = ItemUtil.halfDamagedItem(Material.IRON_LEGGINGS)
			2 -> equipment.chestplate = ItemUtil.halfDamagedItem(Material.IRON_CHESTPLATE)
			3 -> equipment.helmet = ItemUtil.halfDamagedItem(Material.IRON_HELMET)
			4 -> equipment.setItemInMainHand(ItemUtil.halfDamagedItem(Material.IRON_SWORD))
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
		return when (spawnCycle % 12) {
			0 -> blazeAllowSpawn(block, spawnCycle)
			1 -> ghastAllowSpawn(block, spawnCycle)
			2 -> magmaCubeAllowSpawn(block, spawnCycle)
			3 -> blazeAllowSpawn(block, spawnCycle)
			4 -> zombiePiglinAllowSpawn(block, spawnCycle)
			5 -> endermanAllowSpawn(block, spawnCycle)
			6 -> blazeAllowSpawn(block, spawnCycle)
			7 -> zombiePiglinAllowSpawn(block, spawnCycle)
			8 -> magmaCubeAllowSpawn(block, spawnCycle)
			9 -> zombiePiglinAllowSpawn(block, spawnCycle)
			10 -> zombiePiglinAllowSpawn(block, spawnCycle)
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

		entity.canPickupItems = false
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

	fun spawnForPlayer(player: Player, playerList: ArrayList<Player>, playerData: PlayerData) {
		val world = player.world
		val infoList = if (world.environment == World.Environment.NETHER) netherSpawnInfoList else overworldSpawnInfoList
		if (playerData.spawnIndex > infoList.lastIndex) playerData.spawnIndex = 0

		if (spawnMob(player, playerData, playerList, playerData.spawnCycle, infoList[playerData.spawnIndex])) {
			++playerData.spawnIndex

			if (playerData.spawnIndex == infoList.size) {
				++playerData.spawnCycle
				playerData.spawnIndex = 0
			}
		}
	}

	const val MIN_RADIUS = 32
	const val MAX_RADIUS = 86
	const val VERTICAL_RADIUS = 10

	const val PER_PLAYER = 25

	fun anotherPlayerInRange(player: Player, playerList: ArrayList<Player>, blockX: Int, blockZ: Int): Boolean {
		for (otherPlayer in playerList) {
			if (player != otherPlayer && player.world == otherPlayer.world) {
				val otherLocation = otherPlayer.location

				if (sqrt((blockX - otherLocation.x).pow(2) + (blockZ - otherLocation.z).pow(2)) < MIN_RADIUS) return true
			}
		}

		return false
	}

	fun spawnMob(player: Player, playerData: PlayerData, playerList: ArrayList<Player>, spawnCycle: Int, spawnInfo: SpawnInfo): Boolean {
		val angle = Math.random() * 2 * PI

		val radius = Util.randRange(MIN_RADIUS, MAX_RADIUS)

		val radiusDistance = MAX_RADIUS - MIN_RADIUS
		val offsetRadius = (Math.random() * radiusDistance).toInt()

		for (j in 0 until radiusDistance step 2) {
			val radius =  (j + offsetRadius) % (radiusDistance) + MIN_RADIUS

			val blockX = (cos(angle) * radius + player.location.x).toInt()
			val blockZ = (sin(angle) * radius + player.location.z).toInt()

			if (!anotherPlayerInRange(player, playerList, blockX, blockZ)) {
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

						spawnInfo.onSpawn(entity, spawnCycle)
						makePlayerMob(entity, player, playerData)

						return true
					}
				}
			}
		}

		return false
	}

	const val SPAWN_TAG = "_UHC_SPAWN"
	data class SpawnTagData(val uuid: UUID, val fraction: Double)

	fun makePlayerMob(entity: Entity, player: Player, data: PlayerData) {
		entity.setMetadata(SPAWN_TAG, FixedMetadataValue(UHCPlugin.plugin, SpawnTagData(player.uniqueId, (1 / data.mobcap).coerceAtLeast(0.0))))
	}

	fun mobPercentage(entity: Entity, player: Player): Double {
		if (entity !is Monster) return 0.0
		/* if an entity becomes persistent it is no longer part of your cap */
		if (!entity.removeWhenFarAway) return 0.0

		val meta = entity.getMetadata(SPAWN_TAG)

		return if (meta.isNotEmpty()) {
			val spawnTagData = meta[0].value() as SpawnTagData

			if (spawnTagData.uuid == player.uniqueId) spawnTagData.fraction.coerceAtLeast(0.0)
			else 0.0

		} else {
			0.0
		}
	}

	var spawnTaskID = -1

	fun calcPlayerMobs(player: Player): Pair<Int, Double> {
		var playerMobCount = 0
		var playerMobCapacity = 0.0

		player.world.entities.forEach { entity ->
			val capacity = mobPercentage(entity, player)

			if (capacity != 0.0) {
				++playerMobCount
				playerMobCapacity += capacity
			}
		}

		return Pair(playerMobCount, playerMobCapacity)
	}

	fun startSpawning() {
		spawnTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHCPlugin.plugin, {
			/* list of online players to collect from first pass */
			val playerList = ArrayList<Player>()
			val dataList = ArrayList<PlayerData>()

			/* collect all online players */
			GameRunner.uhc.allCurrentPlayers { uuid ->
				val player = Bukkit.getPlayer(uuid)

				if (player != null) {
					val data = GameRunner.uhc.getPlayerData(uuid)

					playerList.add(player)
					dataList.add(data)
				}
			}

			/* first reset all mobcaps */
			for (i in playerList.indices) {
				dataList[i].mobcap = PER_PLAYER.toDouble()
			}

			/* calculate caps for all players relative to each other */
			for (i in playerList.indices) {
				val player = playerList[i]
				val data = dataList[i]

				val totalArea = VERTICAL_RADIUS * 2 * PI * MAX_RADIUS * MAX_RADIUS

				for (j in 0 until i) {
					val otherPlayer = playerList[j]
					val otherData = dataList[j]

					val location1 = player.location
					val location2 = otherPlayer.location

					if (location1.world == location2.world) {
						val horzDistance = sqrt((location1.x - location2.x).pow(2) + (location1.z - location2.z).pow(2))
						val vertDistance = abs(location1.y - location2.y)

						val intersection = Util.circleIntersection(MAX_RADIUS.toDouble(), horzDistance) * Util.levelIntersection(VERTICAL_RADIUS.toDouble(), vertDistance)
						val percentIntersected = intersection / totalArea

						data.mobcap -= PER_PLAYER * percentIntersected / 2
						otherData.mobcap -= PER_PLAYER * percentIntersected / 2

						if (data.mobcap < 1) data.mobcap = 1.0
						if (otherData.mobcap < 1) otherData.mobcap = 1.0
					}
				}
			}

			/* once we have the caps spawn the mobs */
			for (i in playerList.indices) {
				val player = playerList[i]
				val data = dataList[i]

				if (player.world.getBlockAt(player.location).biome == Biome.SOUL_SAND_VALLEY) data.mobcap *= 0.5
				if (GameRunner.uhc.isPhase(PhaseType.ENDGAME)) data.mobcap *= 0.5

				var playerMobCapacity = calcPlayerMobs(player)

				if (playerMobCapacity.second < 1.0) {
					spawnForPlayer(player, playerList, data)
				}
			}
		}, 0, 20)
	}

	fun stopSpawning() {
		Bukkit.getScheduler().cancelTask(spawnTaskID)
		spawnTaskID = -1
	}

	fun onCycle(spawnCycle: Int, n: Int): Boolean {
		return spawnCycle != 0 && spawnCycle % n == 0
	}

	fun onCycleOffset(spawnCycle: Int, n: Int, o: Int): Boolean {
		return spawnCycle != 0 && spawnCycle % n == o
	}
}

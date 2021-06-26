package com.codeland.uhc.customSpawning

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.command.Commands
import com.codeland.uhc.core.GameRunner
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.core.UHC
import com.codeland.uhc.quirk.QuirkType
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
import kotlin.collections.ArrayList
import kotlin.math.*

object CustomSpawning {
	const val MIN_RADIUS = 32
	const val MAX_RADIUS = 86
	const val VERTICAL_RADIUS = 10
	const val PER_PLAYER = 25

	const val SPAWN_TAG = "_UHC_SPAWN"
	data class SpawnTagData(val uuid: UUID, val fraction: Double)

	val overworldSpawnInfoList = arrayOf(
		SpawnInfoType.ZOMBIE.spawnInfo,
		SpawnInfoType.SKELETON.spawnInfo,
		SpawnInfoType.CREEPER.spawnInfo,
		SpawnInfoType.SPIDER.spawnInfo
	)

	val netherSpawnInfoList = arrayOf(
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_DEFAULT.spawnInfo,
		SpawnInfoType.NETHER_SPECIAL.spawnInfo,
		SpawnInfoType.PIGLIN.spawnInfo
	)

	fun spawnForPlayer(player: Player, playerList: ArrayList<Player>, playerData: PlayerData) {
		val usingInfo = if (player.world.environment == World.Environment.NORMAL) {
			val supplementalList = ArrayList<SpawnInfo>()
			UHC.quirks.forEach { quirk ->
				if (quirk.enabled.get() && quirk.spawnInfos != null) quirk.spawnInfos.forEach { spawnInfo -> supplementalList.add(spawnInfo) }
			}

			/* wrap spawn index */
			if (playerData.spawnIndex >= overworldSpawnInfoList.size + supplementalList.size) {
				playerData.spawnIndex = 0
				++playerData.spawnCycle
			}

			if (playerData.spawnIndex >= overworldSpawnInfoList.size)
				supplementalList[playerData.spawnIndex - overworldSpawnInfoList.size]
			else
				overworldSpawnInfoList[playerData.spawnIndex]

		} else {
			/* wrap spawn index */
			if (playerData.spawnIndex >= netherSpawnInfoList.size) {
				playerData.spawnIndex = 0
				++playerData.spawnCycle
			}

			netherSpawnInfoList[playerData.spawnIndex]
		}

		if (spawnMob(player, playerData, playerList, playerData.spawnCycle, usingInfo)) ++playerData.spawnIndex
	}

	fun anotherPlayerInRange(player: Player, playerList: ArrayList<Player>, blockX: Int, blockZ: Int): Boolean {
		for (otherPlayer in playerList) {
			if (player != otherPlayer && player.world == otherPlayer.world) {
				val otherLocation = otherPlayer.location

				if (sqrt((blockX - otherLocation.x).pow(2) + (blockZ - otherLocation.z).pow(2)) < MIN_RADIUS) return true
			}
		}

		return false
	}

	fun lineOfSightCheck(s: Block, e: Block): Boolean {
		val sX = s.x + 0.5f
		val eX = e.x + 0.5f
		val sY = s.y + 1.5f
		val eY = e.y + 1.5f
		val sZ = s.z + 0.5f
		val eZ = e.z + 0.5f

		val distance = sqrt((sX - eX) * (sX - eX) + (sY - eY) * (sY - eY) + (sZ - eZ) * (sZ - eZ))
		if (distance > 60) return false

		val numChecks = distance.toInt() / 2

		for (i in 1 until numChecks) {
			val along = i / numChecks.toFloat()

			val x = floor(Util.interp(sX, eX, along)).toInt()
			val y = floor(Util.interp(sY, eY, along)).toInt()
			val z = floor(Util.interp(sZ, eZ, along)).toInt()

			if (!s.world.getBlockAt(x, y, z).isPassable) return false
		}

		return true
	}

	fun spawnMob(player: Player, playerData: PlayerData, playerList: ArrayList<Player>, spawnCycle: Int, spawnInfo: SpawnInfo): Boolean {
		val angle = Math.random() * 2 * PI

		val radiusDistance = MAX_RADIUS - MIN_RADIUS
		val offsetRadius = (Math.random() * radiusDistance).toInt()

		val world = player.world
		val borderRadius = world.worldBorder.size / 2

		for (j in 0 until radiusDistance step 2) {
			val radius = (j + offsetRadius) % (radiusDistance) + MIN_RADIUS

			val blockX = floor(cos(angle) * radius + player.location.x).toInt()
			val blockZ = floor(sin(angle) * radius + player.location.z).toInt()

			if (
				abs(blockX) <= borderRadius &&
				abs(blockZ) <= borderRadius &&
				!anotherPlayerInRange(player, playerList, blockX, blockZ)
			) {
				val minY = (player.location.y - VERTICAL_RADIUS).toInt().coerceAtLeast(0).coerceAtMost(255)
				val maxY = (player.location.y + VERTICAL_RADIUS).toInt().coerceAtMost(255).coerceAtLeast(0)

				val distance = maxY - minY
				val offsetY = (Math.random() * distance).toInt()

				for (i in 0 until distance) {
					val y = (i + offsetY) % (distance) + minY
					val block = world.getBlockAt(blockX, y, blockZ)

					val spawnResult = spawnInfo.allowSpawn(block, spawnCycle)

					if (spawnResult != null) {
						val (entityType, lineOfSight) = spawnResult

						/* line of sight check */
						if (lineOfSight && !lineOfSightCheck(block, player.location.block)) {
							return false
						}

						val entity = world.spawnEntity(block.location.add(0.5, 0.0, 0.5), entityType)

						spawnInfo.onSpawn(block, spawnCycle, entity)
						makePlayerMob(entity, player, playerData)

						return true
					}
				}
			}
		}

		return false
	}

	fun playerOnSurface(player: Player): Boolean {
		val world = player.location.world

		if (world.environment != World.Environment.NORMAL) return false
		if (!world.isDayTime) return false

		return player.location.y > 57
	}

	fun makePlayerMob(entity: Entity, player: Player, data: PlayerData) {
		var mobFraction = (1 / data.mobcap).coerceAtLeast(0.0)

		if (entity.location.block.biome === Biome.SOUL_SAND_VALLEY || playerOnSurface(player)) mobFraction *= 2

		entity.setMetadata(SPAWN_TAG, FixedMetadataValue(UHCPlugin.plugin, SpawnTagData(player.uniqueId, mobFraction)))
	}

	fun mobPercentage(entity: Entity, player: Player): Double {
		if (entity !is LivingEntity) return 0.0
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

	fun spawnTick(currentTick: Int) {
		if (currentTick % 20 == 0) {
			/* list of online players to collect from first pass */
			val playerList = ArrayList<Player>()
			val dataList = ArrayList<PlayerData>()

			/* collect all players to spawn mobs around */
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if (playerData.participating) {
					val player = Bukkit.getPlayer(uuid)

					if (player != null) {
						val data = PlayerData.getPlayerData(uuid)

						playerList.add(player)
						dataList.add(data)
					}
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

						val intersection =
							Util.circleIntersection(MAX_RADIUS.toDouble(), horzDistance) * Util.levelIntersection(
								VERTICAL_RADIUS.toDouble(),
								vertDistance
							)
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

				var playerMobCapacity = calcPlayerMobs(player)

				if (playerMobCapacity.second < 1.0) {
					spawnForPlayer(player, playerList, data)
				}
			}
		}
	}
}

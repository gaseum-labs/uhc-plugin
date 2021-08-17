package com.codeland.uhc.customSpawning

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

object CustomSpawning {
	fun getSpawnInfo(
		type: CustomSpawningType,
		player: Player,
		spawningData: CustomSpawningType.SpawningPlayerData,
		game: Game
	): SpawnInfo? {
		return when (player.world.name) {
			WorldManager.GAME_WORLD_NAME -> {
				if (type.gameSpawnInfoList.isEmpty()) return null

				val supplementalList = ArrayList<SpawnInfo>()
				if (type === CustomSpawningType.HOSTILE) {
					game.quirks.filterNotNull().forEach { quirk ->
						if (quirk.spawnInfos != null) quirk.spawnInfos.forEach { spawnInfo ->
							supplementalList.add(
								spawnInfo
							)
						}
					}
				}

				/* wrap spawn index */
				if (spawningData.index >= type.gameSpawnInfoList.size + supplementalList.size) {
					spawningData.index = 0
					++spawningData.cycle
				}

				if (spawningData.index >= type.gameSpawnInfoList.size)
					supplementalList[spawningData.index - type.gameSpawnInfoList.size]
				else
					type.gameSpawnInfoList[spawningData.index]
			}
			WorldManager.NETHER_WORLD_NAME -> {
				if (type.netherSpawnInfoList.isEmpty()) return null

				/* wrap spawn index */
				if (spawningData.index >= type.netherSpawnInfoList.size) {
					spawningData.index = 0
					++spawningData.cycle
				}

				type.netherSpawnInfoList[spawningData.index]
			}
			else -> {
				null
			}
		}
	}

	fun anotherPlayerInRange(type: CustomSpawningType, player: Player, playerList: ArrayList<Player>, blockX: Int, blockZ: Int): Boolean {
		for (otherPlayer in playerList) {
			if (player != otherPlayer && player.world == otherPlayer.world) {
				val otherLocation = otherPlayer.location

				if (sqrt((blockX - otherLocation.x).pow(2) + (blockZ - otherLocation.z).pow(2)) < type.minRadius) {
					return true
				}
			}
		}

		return false
	}

	fun clearLineOfSight(s: Block, e: Block): Boolean {
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

	fun getSpawnBlock(
		type: CustomSpawningType,
		player: Player,
		spawningData: CustomSpawningType.SpawningPlayerData,
		playerList: ArrayList<Player>,
		spawnInfo: SpawnInfo
	): Pair<EntityType, Block>? {
		val angle = Math.random() * 2 * PI

		val radiusDistance = type.maxRadius - type.minRadius
		val offsetRadius = (Math.random() * radiusDistance).toInt()

		val world = player.world
		val borderRadius = world.worldBorder.size / 2

		for (j in 0 until radiusDistance step 2) {
			val radius = (j + offsetRadius) % (radiusDistance) + type.minRadius

			val blockX = floor(cos(angle) * radius + player.location.x).toInt()
			val blockZ = floor(sin(angle) * radius + player.location.z).toInt()

			if (
				abs(blockX) <= borderRadius &&
				abs(blockZ) <= borderRadius &&
				!anotherPlayerInRange(type, player, playerList, blockX, blockZ)
			) {
				val minY = (player.location.y - type.verticalRadius).toInt().coerceAtLeast(0).coerceAtMost(255)
				val maxY = (player.location.y + type.verticalRadius).toInt().coerceAtMost(255).coerceAtLeast(0)

				val distance = maxY - minY
				val offsetY = (Math.random() * distance).toInt()

				for (i in 0 until distance) {
					val y = (i + offsetY) % (distance) + minY
					val block = world.getBlockAt(blockX, y, blockZ)

					val spawnResult = spawnInfo.allowSpawn(block, spawningData.cycle)

					if (spawnResult != null) {
						val (entityType, lineOfSight) = spawnResult

						/* line of sight check */
						if (lineOfSight && !clearLineOfSight(block, player.location.block)) {
							return null
						}

						return Pair(entityType, block)
					}
				}
			}
		}

		return null
	}

	fun playerOnSurface(player: Player): Boolean {
		val world = player.location.world

		if (world.name != WorldManager.GAME_WORLD_NAME) return false
		if (!world.isDayTime) return false

		return player.location.y > 57
	}

	fun makePlayerMob(type: CustomSpawningType, entity: LivingEntity, player: Player) {
		entity.setMetadata(type.spawnTag, FixedMetadataValue(UHCPlugin.plugin, player.uniqueId))
		entity.removeWhenFarAway = true
	}

	fun isPlayerMob(type: CustomSpawningType, entity: Entity, player: Player): Int {
		if (entity !is LivingEntity) return 0
		/* if an entity becomes persistent it is no longer part of your cap */
		if (!entity.removeWhenFarAway) return 0

		val meta = entity.getMetadata(type.spawnTag)

		return if (meta.isNotEmpty() && meta[0].value() as UUID == player.uniqueId) 1 else 0
	}

	fun calcPlayerMobs(type: CustomSpawningType, player: Player): Int {
		return player.world.entities.fold(0) { acc, entity -> acc + isPlayerMob(type, entity, player) }
	}

	fun spawnTick(type: CustomSpawningType, currentTick: Int, game: Game) {
		if (currentTick % type.tryTime == type.ordinal) {
			/* list of online players to collect from first pass */
			val playerList = ArrayList<Player>()
			val spawningDataList = ArrayList<CustomSpawningType.SpawningPlayerData>()

			/* collect all players to spawn mobs around */
			PlayerData.playerDataList.forEach { (uuid, playerData) ->
				if (playerData.participating) {
					val player = Bukkit.getPlayer(uuid)
					if (player != null && player.gameMode != GameMode.SPECTATOR) {
						playerList.add(player)
						spawningDataList.add(PlayerData.getPlayerData(uuid).spawningData[type.ordinal])
					}
				}
			}

			/* first reset all mobcaps */
			spawningDataList.forEachIndexed { i, data ->
				data.cap = type.mobcap.toDouble()

				val player = playerList[i]

				when (player.world.environment) {
					World.Environment.NORMAL -> {
						if (type === CustomSpawningType.HOSTILE && playerOnSurface(player)) data.cap /= 2
					}
					World.Environment.NETHER -> {
						if (player.location.block.y <= SpawnInfo.NETHER_CAVE_Y) data.cap *= 2
						if (player.location.block.biome === Biome.SOUL_SAND_VALLEY) data.cap /= 2
					}
					else -> {}
				}
			}

			/* calculate caps for all players relative to each other */
			for (i in playerList.indices) {
				val player = playerList[i]
				val data = spawningDataList[i]

				val totalArea = (type.verticalRadius * 2 + 1) * PI * type.maxRadius * type.maxRadius

				for (j in 0 until i) {
					val otherPlayer = playerList[j]
					val otherData = spawningDataList[j]

					val location1 = player.location
					val location2 = otherPlayer.location

					if (location1.world === location2.world) {
						val horzDistance = sqrt((location1.x - location2.x).pow(2) + (location1.z - location2.z).pow(2))
						val vertDistance = abs(location1.y - location2.y)

						val intersection = Util.circleIntersection(
								type.maxRadius.toDouble(),
								horzDistance
							) * Util.levelIntersection(
								type.verticalRadius.toDouble(),
								vertDistance
							)
						val percentIntersected = intersection / totalArea

						data.cap -= type.mobcap * percentIntersected / 2
						otherData.cap -= type.mobcap * percentIntersected / 2

						if (data.cap < 1) data.cap = 1.0
						if (otherData.cap < 1) otherData.cap = 1.0
					}
				}
			}

			/* once we have the caps spawn the mobs */
			for (i in playerList.indices) {
				val player = playerList[i]
				val data = spawningDataList[i]

				val playerMobCount = calcPlayerMobs(type, player)

				if (playerMobCount < data.getMobCap()) {
					val spawnInfo = getSpawnInfo(type, player, data, game) ?: continue

					val (entityType, block) = getSpawnBlock(type, player, data, playerList, spawnInfo) ?: continue

					val entity = player.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), entityType) as LivingEntity
					makePlayerMob(type, entity, player)
					spawnInfo.onSpawn(block, data.cycle, entity)

					++data.index
				}
			}
		}
	}
}

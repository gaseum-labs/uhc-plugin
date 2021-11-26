package com.codeland.uhc.customSpawning

import com.codeland.uhc.UHCPlugin
import com.codeland.uhc.core.Game
import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.util.Util
import com.codeland.uhc.world.WorldManager
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.util.RayTraceResult
import java.util.*
import kotlin.math.*
import kotlin.random.Random

object CustomSpawning {
	fun getSpawnEntry(
		type: CustomSpawningType,
		player: Player,
		spawningData: SpawningPlayerData,
		game: Game,
	): SpawnEntry? {
		return when (player.world) {
			WorldManager.gameWorld -> {
				if (type.overworldEntries.isEmpty()) return null

				val supplementalList = ArrayList<SpawnEntry>()
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
				if (spawningData.index >= type.overworldEntries.size + supplementalList.size) {
					spawningData.index = 0
					++spawningData.cycle
				}

				if (spawningData.index >= type.overworldEntries.size)
					supplementalList[spawningData.index - type.overworldEntries.size]
				else
					type.overworldEntries[spawningData.index]
			}
			WorldManager.netherWorld -> {
				if (type.netherEntries.isEmpty()) return null

				/* wrap spawn index */
				if (spawningData.index >= type.netherEntries.size) {
					spawningData.index = 0
					++spawningData.cycle
				}

				type.netherEntries[spawningData.index]
			}
			else -> {
				null
			}
		}
	}

	private fun spawnXZValid(
		type: CustomSpawningType,
		player: Player,
		spawnPlayers: List<Pair<Player, SpawningPlayerData>>,
		x: Int,
		z: Int,
	): Boolean {
		val cx = x + 0.5
		val cz = z + 0.5

		val borderRadius = player.world.worldBorder.size / 2
		if (abs(cx) > borderRadius || abs(cz) > borderRadius) return false

		return spawnPlayers.none { (otherPlayer, _) ->
			player !== otherPlayer &&
			player.world === otherPlayer.world &&
			(cx - otherPlayer.location.x).pow(2) + (cz - otherPlayer.location.z).pow(2) < type.minRadius * type.minRadius
		}
	}

	fun clearLineOfSight(s: Block, e: Block): Boolean {
		fun rayCastStartEnd(start: Location, end: Location): RayTraceResult? {
			val vector = end.subtract(start).toVector()
			return start.world.rayTraceBlocks(start, vector, vector.length(), FluidCollisionMode.ALWAYS, true)
		}

		return rayCastStartEnd(s.location.add(0.5, 0.5, 0.5), e.location.add(0.5, 0.5, 0.5)) == null ||
		rayCastStartEnd(s.location.add(0.5, 1.5, 0.5), e.location.add(0.5, 1.5, 0.5)) == null
	}

	fun getSpawnBlock(
		type: CustomSpawningType,
		player: Player,
		spawningData: SpawningPlayerData,
		spawnPlayers: List<Pair<Player, SpawningPlayerData>>,
		spawnEntry: SpawnEntry,
	): Pair<SpawnInfo<*>, Block>? {
		val minY = (player.location.y - type.verticalRadius).toInt().coerceAtLeast(0).coerceAtMost(255)
		val maxY = (player.location.y + type.verticalRadius).toInt().coerceAtMost(255).coerceAtLeast(0)

		val ys = Array(maxY - minY) { minY + it }
		ys.shuffle()

		for (t in 0 until 32) {
			val radius = sqrt(Random.nextDouble(type.minRadius.toDouble(), type.maxRadius.toDouble()) * type.maxRadius)
			val angle = Random.nextDouble(0.0, 2.0 * PI)

			val x = (player.location.blockX + cos(angle) * radius).roundToInt()
			val z = (player.location.blockZ + sin(angle) * radius).roundToInt()

			if (spawnXZValid(type, player, spawnPlayers, x, z)) for (y in ys) {
				val block = player.world.getBlockAt(x, y, z)
				val spawnInfo = spawnEntry.getSpawnInfo(block, spawningData.cycle)

				if (spawnInfo.allowSpawn(block, spawningData.cycle)) {
					return if (spawnInfo.lineOfSight && !clearLineOfSight(block, player.location.block)) {
						return null
					} else {
						Pair(spawnInfo, block)
					}
				}
			}
		}

		return null
	}

	private fun makePlayerMob(type: CustomSpawningType, entity: LivingEntity, player: Player) {
		entity.setMetadata(type.spawnTag, FixedMetadataValue(UHCPlugin.plugin, player.uniqueId))
		entity.removeWhenFarAway = true
	}

	private fun isPlayerMob(type: CustomSpawningType, entity: Entity, player: Player): Int {
		if (entity !is LivingEntity) return 0
		/* if an entity becomes persistent it is no longer part of your cap */
		if (!entity.removeWhenFarAway) return 0

		val meta = entity.getMetadata(type.spawnTag)

		return if (meta.isNotEmpty() && meta[0].value() as UUID == player.uniqueId) 1 else 0
	}

	fun calcPlayerMobs(type: CustomSpawningType, player: Player): Int {
		return player.world.entities.fold(0) { acc, entity -> acc + isPlayerMob(type, entity, player) }
	}

	private fun collectSpawnPlayers(type: CustomSpawningType): List<Pair<Player, SpawningPlayerData>> {
		return PlayerData.playerDataList
			.filter { (_, playerData) -> playerData.participating }
			.mapNotNull { (uuid, playerData) ->
				val player = Bukkit.getPlayer(uuid)
				if (player != null && player.gameMode != GameMode.SPECTATOR) {
					Pair(player, playerData.spawningData[type.ordinal])
				} else {
					null
				}
			}
	}

	private fun calculateCaps(type: CustomSpawningType, spawnPlayers: List<Pair<Player, SpawningPlayerData>>) {
		val totalArea = (type.verticalRadius * 2 + 1) * PI * type.maxRadius * type.maxRadius

		/* first reset all mobcaps */
		spawnPlayers.forEach { (player, data) -> data.cap = type.getCap(player) }

		spawnPlayers.forEachIndexed { i, (player, data) ->
			for (j in i + 1 until spawnPlayers.size) {
				val (otherPlayer, otherData) = spawnPlayers[j]

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

					data.cap -= type.getCap(otherPlayer) * percentIntersected / 2
					otherData.cap -= type.getCap(player) * percentIntersected / 2

					if (data.cap < 0.0) data.cap = 0.0
					if (otherData.cap < 0.0) otherData.cap = 0.0
				}
			}
		}
	}

	/**
	 * @return whether more spawning should be attempted after this
	 */
	private fun attemptSpawn(
		type: CustomSpawningType,
		game: Game,
		player: Player,
		data: SpawningPlayerData,
		spawnPlayers: List<Pair<Player, SpawningPlayerData>>,
	): Boolean {
		val playerMobCount = calcPlayerMobs(type, player)

		if (playerMobCount < data.intCap()) {
			val spawnEntry = getSpawnEntry(type, player, data, game) ?: return false
			/* no valid spawn spots found this time, keep trying */
			val (spawnInfo, block) = getSpawnBlock(type, player, data, spawnPlayers, spawnEntry) ?: return true

			val count = data.counts.getOrPut(spawnInfo.type) { Count(0) }
			makePlayerMob(type, spawnInfo.spawn(block, count.count, player), player)
			
			++count.count
			++data.index
		}

		/* if either spawn was successful, or mobcap is full */
		return false
	}

	fun spawnTick(type: CustomSpawningType, currentTick: Int, game: Game) {
		/* main spawn try tick */
		if (currentTick % type.tryTime == type.ordinal.coerceAtMost(type.tryTime - 1)) {
			val spawnPlayers = collectSpawnPlayers(type)
			calculateCaps(type, spawnPlayers)

			spawnPlayers.forEach { (player, data) ->
				data.isAttempting = attemptSpawn(type, game, player, data, spawnPlayers)
			}

			/* retry tick */
		} else if (currentTick % 19 == 0) {
			val spawnPlayers = collectSpawnPlayers(type)

			spawnPlayers.forEach { (player, data) ->
				if (data.isAttempting) {
					data.isAttempting = attemptSpawn(type, game, player, data, spawnPlayers)
				}
			}
		}
	}
}

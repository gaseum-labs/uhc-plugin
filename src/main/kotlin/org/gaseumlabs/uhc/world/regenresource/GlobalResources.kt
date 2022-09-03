package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataType
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.core.phase.PhaseType.BATTLEGROUND
import org.gaseumlabs.uhc.core.phase.PhaseType.ENDGAME
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.extensions.ArrayListExtensions.inPlaceReplace
import org.gaseumlabs.uhc.util.extensions.ArrayListExtensions.mapUHC
import org.gaseumlabs.uhc.world.WorldManager
import kotlin.random.Random

class GlobalResources {
	data class TeamVeinData(
		var collected: HashMap<PhaseType, Int>,
	)

	data class ResourceData(
		val teamVeinData: HashMap<Team, TeamVeinData>,
		var veins: ArrayList<Vein>,
		var round: Int,
		var nextTick: Int,
	)

	/* when next to create resources, in 5 to 7 seconds */
	private fun nextTick(currentTick: Int): Int {
		return currentTick + Random.nextInt(
			5 * 20,
			7 * 20,
		)
	}

	val resourceData = Array(RegenResource.values().size) {
		ResourceData(
			HashMap(),
			ArrayList(),
			0,
			nextTick(0),
		)
	}

	fun getVeinList(regenResource: RegenResource): ArrayList<Vein> {
		return resourceData[regenResource.ordinal].veins
	}

	fun getTeamVeinData(team: Team, regenResource: RegenResource): TeamVeinData {
		return resourceData[regenResource.ordinal].teamVeinData.getOrPut(team) {
			TeamVeinData(
				PhaseType.values().associateWith { 0 } as HashMap<PhaseType, Int>
			)
		}
	}

	fun releasedCurrently(game: Game, resource: ResourceDescription): Int {
		val phaseType = game.phase.phaseType
		val result = resource.released[phaseType] ?: 0
		return if (result == -1) 10000000 else result
	}

	private fun inSomeWayModified(type: ResourceDescription, vein: Vein): Boolean {
		return if (type is ResourceDescriptionBlock) {
			(vein as VeinBlock).blocks.any { block -> !type.isBlock(block) }
		} else {
			!(vein as VeinEntity).isLoaded()
		}
	}

	private fun markChunkRound(chunk: Chunk, regenResource: RegenResource, roundNum: Int) {
		chunk.persistentDataContainer.set(regenResource.chunkKey, PersistentDataType.INTEGER, roundNum)
	}

	private fun getChunkRound(chunk: Chunk, regenResource: RegenResource): Int? {
		return chunk.persistentDataContainer.get(regenResource.chunkKey, PersistentDataType.INTEGER)
	}

	fun tick(game: Game, currentTick: Int) {
		for (i in RegenResource.values().indices) {
			val resourceData = resourceData[i]
			val regenResource = RegenResource.values()[i]

			if (currentTick >= resourceData.nextTick) {
				if (
					(game.phase.phaseType === BATTLEGROUND || game.phase.phaseType === ENDGAME) &&
					regenResource.description.worldName != WorldManager.NETHER_WORLD_NAME
				) {
					updateBattleground(game, resourceData, regenResource)
				} else {
					update(game, resourceData, regenResource)
				}

				resourceData.nextTick = nextTick(currentTick)
			}
		}
	}

	private fun update(game: Game, resourceData: ResourceData, regenResource: RegenResource) {
		val world = Bukkit.getWorld(regenResource.description.worldName)!!
		val description = regenResource.description

		++resourceData.round

		val players = PlayerData.playerDataList.mapNotNull { (uuid, playerData) ->
			if (!playerData.alive) return@mapNotNull null

			val player = Bukkit.getPlayer(uuid) ?: return@mapNotNull null
			if (player.world !== world) return@mapNotNull null

			val team = game.teams.playersTeam(uuid) ?: return@mapNotNull null

			player to if (
				getTeamVeinData(team, regenResource).collected[game.phase.phaseType]!! <
				releasedCurrently(game, description)
			) 0 else 1
		}.sortedBy { (_, sort) -> sort }

		/* find new chunks around players to generate in */
		players.forEach { (player, quotaReached) ->
			val chunk = player.chunk

			val bounds = Bounds(
				chunk.x - description.chunkRadius,
				chunk.z - description.chunkRadius,
				description.chunkRadius * 2 + 1,
				description.chunkRadius * 2 + 1
			)

			for (z in bounds.yRange()) {
				for (x in bounds.xRange()) {
					val testChunk = world.getChunkAt(x, z)
					val oldChunkRound = getChunkRound(testChunk, regenResource)

					/* this chunk hasn't been visited already this round */
					/* this chunk wasn't part of the set last round */
					if (
						oldChunkRound != resourceData.round &&
						oldChunkRound != resourceData.round - 1
					) {
						if (Random.nextFloat() < description.chunkSpawnChance) {
							val generatedList = description.generateInChunk(testChunk, quotaReached == 0)
							if (generatedList != null) {
								generateInChunk(testChunk, generatedList, resourceData, description, quotaReached == 0)
							}
						}
					}

					markChunkRound(testChunk, regenResource, resourceData.round)
				}
			}
		}

		/* delete veins that have moved out of the around players */
		resourceData.veins.removeIf { vein ->
			if (
				getChunkRound(world.getChunkAt(vein.x, vein.z), regenResource) != resourceData.round ||
				inSomeWayModified(regenResource.description, vein)
			) {
				vein.erase()
				true
			} else {
				false
			}
		}
	}

	fun generateInChunk(
		chunk: Chunk,
		generatedList: List<Block>,
		resourceData: ResourceData,
		description: ResourceDescription,
		full: Boolean,
	) {
		if (description is ResourceDescriptionBlock) {
			val originalData = generatedList.map { it.blockData }
			generatedList.forEachIndexed { j, block ->
				description.setBlock(
					block,
					j,
					full
				)
			}

			resourceData.veins.add(VeinBlock(
				originalData, generatedList, chunk.x, chunk.z,
			))
		} else if (description is ResourceDescriptionEntity) {
			resourceData.veins.add(VeinEntity(
				description.setEntity(generatedList[0], full), chunk.x, chunk.z,
			))
		}
	}

	fun updateBattleground(game: Game, resourceData: ResourceData, regenResource: RegenResource) {
		val world = Bukkit.getWorld(regenResource.description.worldName)!!
		val chunkRadius = ((world.worldBorder.size / 2) / 16).toInt()
		val description = regenResource.description

		/* don't attempt to generate if none can exist */
		val maxCurrent = description.released[game.phase.phaseType] ?: 0
		if (maxCurrent <= 0) return

		val players = PlayerData.playerDataList.mapNotNull { (uuid, playerData) ->
			if (!playerData.alive) return@mapNotNull null

			val player = Bukkit.getPlayer(uuid) ?: return@mapNotNull null
			if (player.world !== world) return@mapNotNull null

			player
		}
		if (players.isEmpty()) return

		/* tries to spawn */
		var generatedList: List<Block>? = null
		var generatedChunk: Chunk? = null
		for (t in 0 until chunkRadius) {
			val chunkX = Random.nextInt(-chunkRadius, chunkRadius + 1)
			val chunkZ = Random.nextInt(-chunkRadius, chunkRadius + 1)

			val chunk = world.getChunkAt(chunkX, chunkZ)
			generatedList = description.generateInChunk(chunk, true)

			if (generatedList != null) {
				generatedChunk = chunk
				break
			}
		}
		if (generatedList == null || generatedChunk == null) return

		var couldNotDelete = false

		/* another is going to spawn, so make room for it */
		if (resourceData.veins.size >= maxCurrent) {
			val minDistances = resourceData.veins.mapUHC { vein ->
				vein to players.minOf { player ->
					vein.centerLocation().distance(player.location)
				}
			}
			minDistances.sortBy { (_, distance) -> distance }

			val numDeletes = (resourceData.veins.size - maxCurrent) + 1
			for (i in 0 until numDeletes) {
				val (lastVein, distance) = minDistances.last()

				/* all veins are protected within 24 blocks of a player */
				if (distance < 24.0) {
					couldNotDelete = true
					break
				} else {
					lastVein.erase()
					minDistances.removeLast()
				}
			}

			resourceData.veins.inPlaceReplace(minDistances) { (vein) -> vein }
		}

		if (!couldNotDelete) {
			generateInChunk(generatedChunk, generatedList, resourceData, description, true)
		}
	}
}
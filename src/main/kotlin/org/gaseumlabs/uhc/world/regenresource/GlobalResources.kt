package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.*
import org.bukkit.persistence.PersistentDataType
import org.gaseumlabs.uhc.UHCPlugin
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.world.regenresource.RegenResource.LEATHER
import kotlin.random.Random

class GlobalResources {
	data class TeamVeinData(
		var collected: HashMap<PhaseType, Int>,
	)

	data class ResourceData(
		val teamVeinData: HashMap<Team, TeamVeinData>,
		val veins: ArrayList<Vein>,
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

	private fun eraseVein(type: ResourceDescription, vein: Vein) {
		if (type is ResourceDescriptionBlock) {
			(vein as VeinBlock).blocks.forEachIndexed { i, block -> block.blockData = vein.originalBlocks[i] }
		} else {
			(vein as VeinEntity).entity.remove()
		}
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
				update(game, resourceData, regenResource)
				resourceData.nextTick = nextTick(currentTick)
			}
		}
	}

	private fun update(game: Game, resourceData: ResourceData, regenResource: RegenResource) {
		val world = Bukkit.getWorld(regenResource.description.worldName)!!
		val description = regenResource.description

		++resourceData.round

		val players = game.teams.teams().flatMap { team ->
			team.members.mapNotNull { uuid ->
				val player = Bukkit.getPlayer(uuid)

				/* eligible to spawn */
				if (
					player != null &&
					player.world === world &&
					description.eligable(player)
				) {
					/* have they reached quota? */
					player to if (
						getTeamVeinData(team, regenResource).collected[game.phase.phaseType]!! <
						releasedCurrently(game, description)
					) 0 else 1
				} else {
					null
				}
			}
			/* players who have resources left get prioritized */
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
								if (description is ResourceDescriptionBlock) {
									val originalData = generatedList.map { it.blockData }
									generatedList.forEachIndexed { j, block ->
										description.setBlock(
											block,
											j,
											quotaReached == 0
										)
									}

									resourceData.veins.add(VeinBlock(
										originalData, generatedList, x, z,
									))
								} else if (description is ResourceDescriptionEntity) {
									resourceData.veins.add(VeinEntity(
										description.setEntity(generatedList[0], quotaReached == 0), x, z,
									))
								}
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
				eraseVein(regenResource.description, vein)
				true
			} else {
				false
			}
		}
	}
}
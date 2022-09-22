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
import org.gaseumlabs.uhc.util.Util.randomFirstMatchIndex
import org.gaseumlabs.uhc.world.WorldManager
import kotlin.random.Random

class GlobalResources {
	companion object {
		val PROTECT_RADIUS = 24.0
		val STALE_TIME = 20 * 5 * 60
	}

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
					updateBattleground(game, resourceData, regenResource.description, currentTick)
				} else {
					update(game, resourceData, regenResource, currentTick)
				}

				resourceData.nextTick = nextTick(currentTick)
			}
		}
	}

	private fun update(
		game: Game,
		resourceData: ResourceData,
		regenResource: RegenResource,
		currentTick: Int,
	) {
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
							val generatedList = description.generate(RegenUtil.GenBounds.fromChunk(testChunk), quotaReached == 0)
							if (generatedList != null) {
								generateVein(x, z, -1, currentTick, generatedList, resourceData.veins, description, quotaReached == 0)
							}
						}
					}

					markChunkRound(testChunk, regenResource, resourceData.round)
				}
			}
		}

		/* delete veins that have moved out of the around players */
		/* if environment modifies the vein, leave it physically, not counted in the list */
		resourceData.veins.removeIf { vein ->
			if (getChunkRound(world.getChunkAt(vein.x, vein.z), regenResource) != resourceData.round) {
				vein.erase()
				return@removeIf true
			}
			inSomeWayModified(regenResource.description, vein)
		}
	}

	fun generateVein(
		x: Int,
		z: Int,
		partition: Int,
		timestamp: Int,
		generatedList: List<Block>,
		veins: ArrayList<Vein>,
		description: ResourceDescription,
		full: Boolean,
	): Vein {
		val vein = when (description) {
			is ResourceDescriptionBlock -> {
				val originalData = generatedList.map { it.blockData }
				generatedList.forEachIndexed { j, block ->
					description.setBlock(
						block,
						j,
						full
					)
				}
				VeinBlock(originalData, generatedList, x, z, partition, timestamp)
			}
			is ResourceDescriptionEntity -> VeinEntity(
				description.setEntity(generatedList[0], full), x, z, partition, timestamp
			)
			else -> throw Error("Unknown vein type for $description")
		}
		veins.add(vein)
		return vein
	}

	fun veinProtected(playerLocations: List<Location>, vein: Vein): Boolean {
		val location = vein.centerLocation()
		return playerLocations.any { location.distance(it) <= PROTECT_RADIUS }
	}

	/**
	 * @return an empty partition to fill, or null if all partitions are filled
	 */
	fun findPartitionToFill(
		numPartitions: Int, veins: ArrayList<Vein>
	): Int? {
		val partitionsFilled = Array(numPartitions) { false }
		veins.forEach { vein ->
			if (vein.partition != -1) partitionsFilled[vein.partition] = true
		}
		return partitionsFilled.randomFirstMatchIndex { !it }
	}

	/**
	 * veins that are too old,
	 * these should be replaced because they might be in an unreachable location
	 */
	fun findStaleVeins(
		playerLocations: List<Location>,
		veins: ArrayList<Vein>,
		currentTick: Int
	): List<Vein> {
		val list = ArrayList<Vein>()

		/* delete the oldest pre-battlground vein */
		veins.filter { vein -> vein.partition == -1 }
			.sortedBy { it.timestamp }
			.find { !veinProtected(playerLocations, it) }
			?.let { list.add(it) }
		/* and all stale battleground veins */
		list.addAll(veins.filter { vein ->
			vein.partition != -1 &&
			currentTick - vein.timestamp > STALE_TIME &&
			!veinProtected(playerLocations, vein)
		})

		return list
	}

	fun deleteVein(index: Int, veins: ArrayList<Vein>): Vein {
		val deleteVein = veins[index]
		deleteVein.erase()
		return veins.removeAt(index)
	}

	fun deleteVeins(deleteVeins: List<Vein>, veins: ArrayList<Vein>) {
		veins.removeAll(deleteVeins)
		deleteVeins.forEach { vein -> vein.erase() }
	}

	fun createVein(
		resourcePartition: ResourcePartition,
		world: World,
		partition: Int,
		currentTick: Int,
		radius: Int,
		veins: ArrayList<Vein>,
		description: ResourceDescription,
	): Vein? {
		val generatedBounds = resourcePartition.selectFor(world, partition, radius)
		val generatedList = description.generate(generatedBounds, true) ?: return null
		return generateVein(0, 0, partition, currentTick, generatedList, veins, description, true)
	}

	fun updateBattleground(
		game: Game,
		resourceData: ResourceData,
		description: ResourceDescription,
		currentTick: Int,
	) {
		val world = Bukkit.getWorld(description.worldName)!!

		val playerLocations = PlayerData.playerDataList.filter { (_, playerData) -> playerData.alive }
			.mapNotNull { (uuid) -> Bukkit.getPlayer(uuid)?.eyeLocation }
			.filter { location -> location.world === world }
		if (playerLocations.isEmpty()) return

		val numPartitions = description.released[game.phase.phaseType] ?: return
		val resourcePartition = ResourcePartition.partitionOfSize(numPartitions) ?: return

		/* delete all stale veins, replace ones that were partitioned */
		val deletedVeins = findStaleVeins(playerLocations, resourceData.veins, currentTick)
		deleteVeins(deletedVeins, resourceData.veins)
		deletedVeins.forEach { deletedVein ->
			if (deletedVein.partition != -1) createVein(
				resourcePartition,
				world,
				deletedVein.partition,
				currentTick,
				game.config.battlegroundRadius,
				resourceData.veins,
				description
			)
		}

		/* always try to add an extra vein */
		findPartitionToFill(numPartitions, resourceData.veins)?.let { partitionToFill ->
			createVein(
				resourcePartition,
				world,
				partitionToFill,
				currentTick,
				game.config.battlegroundRadius,
				resourceData.veins,
				description
			)
		}
	}
}
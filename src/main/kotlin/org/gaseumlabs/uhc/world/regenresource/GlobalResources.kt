package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.core.phase.PhaseType.*
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.StaticMap
import org.gaseumlabs.uhc.util.Util.randomFirstMatchIndex
import org.gaseumlabs.uhc.util.Util.trueThrough
import org.gaseumlabs.uhc.util.createStaticMap
import org.gaseumlabs.uhc.world.WorldManager
import kotlin.random.Random

class GlobalResources {
	companion object {
		const val PROTECT_RADIUS = 24.0
		const val STALE_TIME = 20 * 5 * 60

		const val RESOURCE_KEY = "uhc_resource_block"

		val rowRanges = arrayOf(
			-2..2,
			-3..3,
			-4..4,
			-4..4,
			-4..4,
			-4..4,
			-4..4,
			-3..3,
			-2..2,
		)

		val resourcesList = createStaticMap(
			ResourceId.melon,
			ResourceId.sugarCane,
			ResourceId.leather,
			ResourceId.blaze,
			ResourceId.netherWart,
			ResourceId.diamond,
			ResourceId.gold,
			ResourceId.emerald,
			ResourceId.ancientDebris,
			ResourceId.upperFish,
			ResourceId.lowerFish,
		) { it.id } as StaticMap<RegenResource<Vein>>

		fun markCollected(game: Game, team: Team, regenResource: RegenResource<*>, value: Int) {
			val collected = game.globalResources.getTeamVeinData(team, regenResource).collected
			collected[game.phase.phaseType] = collected.getOrPut(game.phase.phaseType) { 0 } + value
		}

		fun markCollected(game: Game, player: Player, regenResource: RegenResource<*>, value: Int) {
			markCollected(game, game.teams.playersTeam(player.uniqueId) ?: return, regenResource, value)
		}
	}

	data class TeamVeinData(
		var collected: HashMap<PhaseType, Int>,
	)

	data class ResourceData<V : Vein>(
		val teamVeinData: HashMap<Team, TeamVeinData>,
		var veins: ArrayList<V>,
		var round: Int,
		var nextTick: Int,
	)

	/* when next to create resources, in 5 to 7 seconds */
	private fun nextTick(phaseType: PhaseType, currentTick: Int): Int {
		return currentTick + if (phaseType === BATTLEGROUND || phaseType === ENDGAME) Random.nextInt(
			8 * 20,
			12 * 20,
		) else Random.nextInt(
			4 * 20,
			6 * 20,
		)
	}

	private val resourceData = resourcesList.map {
		ResourceData(
			HashMap(),
			ArrayList(),
			0,
			nextTick(GRACE, 0),
		)
	}

	fun getVeinList(regenResource: RegenResource<*>): ArrayList<Vein> {
		return resourceData[regenResource.id].veins
	}

	fun getTeamVeinData(team: Team, regenResource: RegenResource<*>): TeamVeinData {
		return resourceData[regenResource.id].teamVeinData.getOrPut(team) {
			TeamVeinData(
				PhaseType.values().associateWith { 0 } as HashMap<PhaseType, Int>
			)
		}
	}

	/**
	 * @return null if the game is in a battleground phase or does not release this phase
	 */
	fun releasedCurrently(game: Game, collected: Int, resource: RegenResource<*>): Tier? {
		val phaseType = game.phase.phaseType
		val release = resource.released[phaseType]
		if (release !is ReleaseChunked) return null

		val tier = release.getTier(collected)
		if (tier.isNone()) return null

		return tier
	}

	private fun markChunkRound(chunk: Chunk, regenResource: RegenResource<*>, roundNum: Int) {
		chunk.persistentDataContainer.set(regenResource.chunkKey, PersistentDataType.INTEGER, roundNum)
	}

	private fun getChunkRound(chunk: Chunk, regenResource: RegenResource<*>): Int? {
		return chunk.persistentDataContainer.get(regenResource.chunkKey, PersistentDataType.INTEGER)
	}

	fun tick(game: Game, currentTick: Int) {
		for (i in resourcesList.indices()) {
			val resourceData = resourceData[i]
			val regenResource = resourcesList[i]

			if (currentTick >= resourceData.nextTick) {
				if (
					(game.phase.phaseType === BATTLEGROUND || game.phase.phaseType === ENDGAME) &&
					regenResource.worldName != WorldManager.NETHER_WORLD_NAME
				) {
					updateBattleground(game, resourceData, regenResource, currentTick)
				} else {
					update(game, resourceData, regenResource, currentTick)
				}

				resourceData.nextTick = nextTick(game.phase.phaseType, currentTick)
			}

			if (currentTick % 20 == 0) {
				resourceData.veins.forEach { regenResource.onUpdate(it) }
			}
		}
	}

	private fun <V : Vein>update(
		game: Game,
		resourceData: ResourceData<V>,
		regenResource: RegenResource<V>,
		currentTick: Int,
	) {
		val world = Bukkit.getWorld(regenResource.worldName)!!

		++resourceData.round

		val players = (PlayerData.playerDataList.mapNotNull { (uuid, playerData) ->
			if (!playerData.alive) return@mapNotNull null

			val player = Bukkit.getPlayer(uuid) ?: return@mapNotNull null
			if (player.world !== world) return@mapNotNull null

			val team = game.teams.playersTeam(uuid) ?: return@mapNotNull null

			player to releasedCurrently(
				game,
				getTeamVeinData(team, regenResource).collected[game.phase.phaseType]!!,
				regenResource
			)
		}
			/* only players that have this resource released */
			.filter { (_, tier) -> tier != null } as List<Pair<Player, Tier>>)
			/* try to drop for higher tier players first */
			.sortedBy { (_, tier) -> tier.tier }

		/* find new chunks around players to generate in */
		players.forEach { (player, tier) ->
			val center = player.chunk
			for (i in 0 ..8) {
				val z = center.z + i - 4
				for (ox in rowRanges[i]) {
					val x = center.x + ox
					val testChunk = world.getChunkAt(x, z)
					val oldChunkRound = getChunkRound(testChunk, regenResource)

					/* this chunk hasn't been visited already this round */
					/* this chunk wasn't part of the set last round */
					if (
						regenResource.eligible(player) &&
						oldChunkRound != resourceData.round &&
						oldChunkRound != resourceData.round - 1 &&
						Random.nextFloat() < tier.spawnChance
					) regenResource.generate(
						RegenUtil.GenBounds.fromChunk(testChunk),
						tier.tier
					)?.let { (list, value) -> resourceData.veins.add(regenResource.createVein(
						x, z, -1, currentTick, value, list, tier.tier
					)) }

					markChunkRound(testChunk, regenResource, resourceData.round)
				}
			}
		}

		/* delete veins that have moved out of the around players */
		/* if environment modifies the vein, leave it physically, not counted in the list */
		resourceData.veins.removeIf { vein ->
			trueThrough(getChunkRound(world.getChunkAt(vein.x, vein.z), regenResource) != resourceData.round) {
				vein.erase()
			} || regenResource.isModified(vein)
		}
	}

	fun veinProtected(playerLocations: List<Location>, vein: Vein): Boolean {
		val location = vein.centerLocation()
		return playerLocations.any { location.distance(it) <= PROTECT_RADIUS }
	}

	/**
	 * @return an empty partition to fill, or null if all partitions are filled
	 */
	fun <V : Vein> findPartitionToFill(
		numPartitions: Int, veins: ArrayList<V>
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
	fun <V : Vein> findStaleVeins(
		playerLocations: List<Location>,
		veins: ArrayList<V>,
		currentTick: Int
	): List<V> {
		val list = ArrayList<V>()

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

	fun <V : Vein> deleteVeins(deleteVeins: List<V>, veins: ArrayList<V>) {
		veins.removeAll(deleteVeins.toSet())
		deleteVeins.forEach { vein -> vein.erase() }
	}

	fun <V : Vein> createVeinBattleground(
		resourcePartition: ResourcePartition,
		world: World,
		partition: Int,
		currentTick: Int,
		radius: Int,
		veins: ArrayList<V>,
		regenResource: RegenResource<V>,
	) {
		val generatedBounds = resourcePartition.selectFor(world, partition, radius)
		val (list, value) = regenResource.generate(generatedBounds, 0) ?: return
		veins.add(regenResource.createVein(0, 0, partition, currentTick, value, list, 0))
	}

	fun <V: Vein> updateBattleground(
		game: Game,
		resourceData: ResourceData<V>,
		regenResource: RegenResource<V>,
		currentTick: Int,
	) {
		val world = Bukkit.getWorld(regenResource.worldName)!!

		val playerLocations = PlayerData.playerDataList.filter { (_, playerData) -> playerData.alive }
			.mapNotNull { (uuid) -> Bukkit.getPlayer(uuid)?.eyeLocation }
			.filter { location -> location.world === world }
		if (playerLocations.isEmpty()) return

		val resourcePartition = (regenResource.released[game.phase.phaseType] as? ReleaseBattleground)?.partition ?: return

		/* delete all stale veins, replace ones that were partitioned */
		val deletedVeins = findStaleVeins(playerLocations, resourceData.veins, currentTick)
		deleteVeins(deletedVeins, resourceData.veins)
		deletedVeins.forEach { deletedVein ->
			if (deletedVein.partition != -1) createVeinBattleground(
				resourcePartition,
				world,
				deletedVein.partition,
				currentTick,
				game.preset.battlegroundRadius,
				resourceData.veins,
				regenResource,
			)
		}

		/* always try to add an extra vein */
		findPartitionToFill(resourcePartition.size, resourceData.veins)?.let { partitionToFill ->
			createVeinBattleground(
				resourcePartition,
				world,
				partitionToFill,
				currentTick,
				game.preset.battlegroundRadius,
				resourceData.veins,
				regenResource,
			)
		}
	}
}
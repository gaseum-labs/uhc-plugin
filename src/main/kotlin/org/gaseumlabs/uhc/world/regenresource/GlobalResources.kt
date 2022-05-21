package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Bukkit
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.phase.PhaseType
import org.gaseumlabs.uhc.team.Team
import kotlin.random.Random

class GlobalResources(initialTick: Int) {
	data class TeamVeinData(
		var collected: HashMap<PhaseType, Int>,
	)

	data class ResourceData(
		var positionSet: PositionSet,
		val teamVeinData: HashMap<Team, TeamVeinData>,
		val veins: ArrayList<Vein>,

		var updatePostionSetNextTick: Int,
		var clearVeinsNextTick: Int,
	)

	/* when next to create resources, in 10 to 15 seconds */
	fun nextTick(currentTick: Int): Int {
		return currentTick + Random.nextInt(
			10 * 20,
			15 * 20,
		)
	}

	val resourceData = Array(RegenResource.values().size) {
		ResourceData(
			PositionSetBuilder().build(),
			HashMap(),
			ArrayList(),

			nextTick(initialTick),
			nextTick(initialTick),
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

	fun eraseVein(type: ResourceDescription, vein: Vein) {
		if (type is ResourceDescriptionBlock) {
			(vein as VeinBlock).blocks.forEachIndexed { i, block -> block.blockData = vein.originalBlocks[i] }
		} else {
			(vein as VeinEntity).entity.remove()
		}
	}

	fun inSomeWayModified(type: ResourceDescription, vein: Vein): Boolean {
		return if (type is ResourceDescriptionBlock) {
			(vein as VeinBlock).blocks.any { block -> !type.isBlock(block) }
		} else {
			!(vein as VeinEntity).isLoaded()
		}
	}

	fun tick(game: Game, currentTick: Int) {
		for (i in RegenResource.values().indices) {
			val resourceData = resourceData[i]
			val regenResource = RegenResource.values()[i]

			if (currentTick >= resourceData.clearVeinsNextTick) {
				clearVeins(game, resourceData, regenResource)
				resourceData.clearVeinsNextTick = nextTick(currentTick)
			}

			if (currentTick >= resourceData.updatePostionSetNextTick) {
				updatePositionSet(game, resourceData, regenResource)
				resourceData.updatePostionSetNextTick = nextTick(currentTick)
			}
		}
	}

	fun clearVeins(game: Game, resourceData: ResourceData, regenResource: RegenResource) {
		resourceData.veins.removeIf { vein ->
			if (!resourceData.positionSet.inSet(vein.x, vein.z) || inSomeWayModified(regenResource.description, vein)) {
				eraseVein(regenResource.description, vein)
				true
			} else {
				false
			}
		}
	}

	fun updatePositionSet(game: Game, resourceData: ResourceData, regenResource: RegenResource) {
		val world = Bukkit.getWorld(regenResource.description.worldName)!!
		val description = regenResource.description

		val players = game.teams.teams().flatMap { team ->
			team.members.mapNotNull { uuid ->
				val player = Bukkit.getPlayer(uuid)
				if (player != null && player.world === world && description.eligable(player)) {
					player to team
				} else {
					null
				}
			}
		}

		val builder = PositionSetBuilder()

		/* only add players to the position set if their team has yet more to find */
		players.forEach { (player, team) ->
			if (
				getTeamVeinData(team, regenResource).collected[game.phase.phaseType]!!
				< releasedCurrently(game, description)
			) {
				val chunk = player.chunk
				builder.addBounds(
					chunk.x - description.chunkRadius,
					chunk.z - description.chunkRadius,
					description.chunkRadius * 2 + 1,
					description.chunkRadius * 2 + 1
				)
			}
		}

		val newPositionSet = builder.build()

		/* generate new resources in new chunks */
		newPositionSet.newIndicesVs(resourceData.positionSet) { x, y ->
			if (Random.nextFloat() >= description.chunkSpawnChance) return@newIndicesVs

			val generatedList = description.generateInChunk(world.getChunkAt(x, y))

			if (generatedList != null) {
				if (description is ResourceDescriptionBlock) {
					val originalData = generatedList.map { it.blockData }
					generatedList.forEachIndexed { j, block -> description.setBlock(block, j) }

					resourceData.veins.add(VeinBlock(
						originalData,
						generatedList,
						x,
						y,
					))
				} else if (description is ResourceDescriptionEntity) {
					resourceData.veins.add(VeinEntity(
						description.setEntity(generatedList[0]),
						x,
						y,
					))
				}
			}
		}

		resourceData.positionSet = newPositionSet
	}
}
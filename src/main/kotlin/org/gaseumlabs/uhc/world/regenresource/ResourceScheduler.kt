package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.core.phase.phases.Grace
import org.gaseumlabs.uhc.core.phase.phases.Shrink
import org.gaseumlabs.uhc.team.Team
import org.gaseumlabs.uhc.util.Action
import org.gaseumlabs.uhc.util.Util
import kotlin.math.PI
import kotlin.random.Random

class ResourceScheduler(val game: Game) {
	companion object {
		const val FIND_RADIUS = 32.0
	}

	data class VeinData(
		var collected: Int,
		var numGenerates: Int,
		var nextTime: Int,
		var current: ArrayList<Vein>,
	)

	val resourceDescriptions: Array<ResourceDescription> = Array(RegenResource.values().size) { i ->
		RegenResource.values()[i].description
	}

	val veinDataList: HashMap<Team, Array<VeinData>> = HashMap()
	val foundVeins: ArrayList<Pair<ResourceDescription, Vein>> = ArrayList()

	private fun generateVeinDataEntry(ticks: Int): Array<VeinData> {
		return Array(resourceDescriptions.size) { i ->
			VeinData(0, 0, ticks + resourceDescriptions[i].interval, ArrayList())
		}
	}

	private fun eraseVein(type: ResourceDescription, vein: Vein) {
		if (type is ResourceDescriptionBlock) {
			(vein as VeinBlock).blocks.forEachIndexed { i, block -> block.blockData = vein.originalBlocks[i] }
		} else {
			(vein as VeinEntity).entity.remove()
		}
	}

	private fun availableBlockFaces(player: Player, block: Block): List<BlockFace> {
		val relative = player.eyeLocation.subtract(block.location.toCenterLocation())

		val xFace = if (relative.x > 0.0) BlockFace.EAST else BlockFace.WEST
		val yFace = if (relative.y > 0.0) BlockFace.UP else BlockFace.DOWN
		val zFace = if (relative.z > 0.0) BlockFace.SOUTH else BlockFace.NORTH

		return listOfNotNull(
			if (block.getRelative(xFace).isPassable) xFace else null,
			if (block.getRelative(yFace).isPassable) yFace else null,
			if (block.getRelative(zFace).isPassable) zFace else null
		)
	}

	private fun playerLookingAt(playerEyes: Location, at: Location): Boolean {
		val inverseLookIn = playerEyes.direction.multiply(-1)
		val atToPlayer = playerEyes.subtract(at).toVector()
		return inverseLookIn.angle(atToPlayer) <= PI.toFloat() / 7.0f
	}

	private fun playerFinds(player: Player, vein: Vein): Boolean {
		when (vein) {
			is VeinBlock -> {
				val centerBlock = vein.centerBlock()
				if (player.world !== centerBlock.world) return false

				val playerLocation = player.eyeLocation
				if (playerLocation.distance(centerBlock.location.toCenterLocation()) > FIND_RADIUS) return false

				return vein.blocks.any { block ->
					val blockLocation = block.location.toCenterLocation()

					playerLookingAt(playerLocation, blockLocation) && availableBlockFaces(player, block).any { face ->
						val origin = blockLocation.add(Vector(face.modX, face.modY, face.modZ))

						player.hasLineOfSight(origin)
					}
				}
			}
			is VeinEntity -> {
				if (player.world !== vein.entity.world) return false
				val playerLocation = player.eyeLocation

				if (playerLocation.distance(vein.entity.location) > FIND_RADIUS) return false
				if (!playerLookingAt(playerLocation, vein.entity.location)) return false

				return player.hasLineOfSight(vein.entity)
			}
			else -> {
				return false
			}
		}
	}

	/**
	 *  when a vein is destroyed in some way not by player breaking/killing
	 */
	private fun inSomeWayModified(type: ResourceDescription, vein: Vein): Boolean {
		return if (type is ResourceDescriptionBlock) {
			(vein as VeinBlock).blocks.any { block -> !type.isBlock(block) }
		} else {
			!(vein as VeinEntity).isLoaded()
		}
	}

	fun getVeinData(team: Team, regenResource: RegenResource): VeinData {
		return veinDataList[team]!![regenResource.ordinal]
	}

	fun releasedCurrently(resource: ResourceDescription): Int {
		/* how long grace + shrink takes in ticks */
		val totalLength = (game.config.graceTime.get() + game.config.shrinkTime.get()) * 20.0f

		val along = when (game.phase) {
			is Grace ->
				(game.config.graceTime.get() * 20 - game.phase.remainingTicks) / totalLength
			is Shrink ->
				(game.config.graceTime.get() * 20 + (game.config.shrinkTime.get() * 20 - game.phase.remainingTicks)) / totalLength
			else -> 1.0f
		}

		return Util.interp(
			resource.initialReleased.toFloat(),
			resource.maxReleased.toFloat(),
			along
		).toInt()
	}

	fun tick(ticks: Int) {
		//TODO distributed timing

		/* don't need to be precise on timing */
		if (ticks % 20 != 7) return

		/* veinDatas for all active teams during this tick */
		val currentTeams = game.teams.teams().map { team ->
			veinDataList.getOrPut(team) { generateVeinDataEntry(ticks) } to
			team.members.mapNotNull { Bukkit.getPlayer(it) }
		}

		currentTeams.forEach { (veinDatas, teamPlayers) ->
			for (i in veinDatas.indices) {
				val veinData = veinDatas[i]
				val veinType = resourceDescriptions[i]

				veinData.current.removeAll { vein ->
					if (inSomeWayModified(veinType, vein)) return@removeAll true

					/* when found, the vein remains until forcibly removed by mining */
					if (currentTeams.any { (_, otherPlayers) ->
							otherPlayers.any { player ->
								val res = playerFinds(player, vein)
								if (res) Action.sendGameMessage(player, "found vein $vein")
								res
							}
						}) {
						foundVeins.add(veinType to vein)

					} else {
						false
					}
				}

				/* remove the oldest veins to make room for new ones */
				while (veinData.current.size > veinType.maxCurrent) {
					eraseVein(veinType, veinData.current.removeFirst())
				}

				/* gradually remove veins if you have met your collected */
				if (veinData.current.isNotEmpty() && veinData.collected >= releasedCurrently(veinType)) {
					eraseVein(veinType, veinData.current.removeFirst())
				}

				/* attempt generations */
				if (
					teamPlayers.isNotEmpty() &&
					ticks >= veinData.nextTime &&
					veinData.collected < releasedCurrently(veinType)
				) {
					/* attempt to generate this vein for all the players on the team */
					/* only one will generate, but priority is given to 1 player on a cycle */
					var generatedList: List<Block>? = null

					for (j in teamPlayers.indices) {
						val generateAround = teamPlayers[(j + veinData.numGenerates) % teamPlayers.size].location.block

						generatedList = veinType.generateVein(generateAround.world,
							generateAround.x,
							generateAround.y,
							generateAround.z)
						if (generatedList != null) break
					}

					/* dividing by the number of team players esentially multiplies the rate */
					/* this results in the rates being the same per player on any size team */
					var addedTime =
						(veinType.interval / teamPlayers.size) + Random.nextInt(-20, 20)

					/* if this generation failed, try again sooner than usual */
					if (generatedList == null) {
						Util.log("failed to place $veinType")
						addedTime /= 2
					} else {
						Util.log("placed $veinType")
						if (veinType is ResourceDescriptionBlock) {
							val originalData = generatedList.map { it.blockData }
							generatedList.forEach { veinType.setBlock(it) }

							veinData.current.add(VeinBlock(
								originalData,
								generatedList,
								ticks,
							))
						} else {
							veinData.current.add(VeinEntity(
								(veinType as ResourceDescriptionEntity).setEntity(generatedList[0]),
								ticks
							))
						}

						++veinData.numGenerates
					}

					veinData.nextTime = ticks + addedTime
				}
			}
		}
	}
}

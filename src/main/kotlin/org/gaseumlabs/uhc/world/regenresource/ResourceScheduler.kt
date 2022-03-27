package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.gaseumlabs.uhc.core.Game
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
		var current: ArrayList<Vein>
	)

	val resourceDescriptions: Array<ResourceDescription> = Array(RegenResource.values().size) { i ->
		RegenResource.values()[i].description
	}

	val veinDataList: HashMap<Team, Array<VeinData>> = HashMap()
	val foundVeins: ArrayList<Pair<ResourceDescription, Vein>> = ArrayList()

	private fun generateVeinDataEntry(ticks: Int): Array<VeinData> {
		return Array(resourceDescriptions.size) { i ->
			VeinData(0, 0, ticks + resourceDescriptions[i].nextInterval(0), ArrayList())
		}
	}

	private fun eraseVein(type: ResourceDescription, vein: Vein) {
		vein.blocks.forEachIndexed { i, block -> block.blockData = vein.originalBlocks[i] }
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

	private fun playerFinds(player: Player, vein: Vein): Boolean {
		val centerBlock = vein.centerBlock()
		if (player.world !== centerBlock.world) return false

		if (player.location.distance(centerBlock.location.toCenterLocation()) > FIND_RADIUS) return false

		val playerLocation = player.eyeLocation
		val inverseLookIn =  playerLocation.direction.multiply(-1)

		return vein.blocks.any { block ->
			val blockLocation = block.location.toCenterLocation()

			availableBlockFaces(player, block).any { face ->
				val origin = blockLocation.add(Vector(face.modX, face.modY, face.modZ))
				val blockToPlayer = playerLocation.subtract(origin).toVector()

				/* player actually has to be looking at the vein */
				if (inverseLookIn.angle(blockToPlayer) > PI.toFloat() / 8.0f) return false

				block.world.rayTrace(
					origin,
					blockToPlayer,
					FIND_RADIUS,
					FluidCollisionMode.NEVER,
					true,
					1.0
				) { it.type === EntityType.PLAYER }?.hitEntity != null
			}
		}
	}

	/**
	 *  when a vein is destroyed in some way not by player breaking
	 */
	private fun inSomeWayModified(type: ResourceDescription, vein: Vein): Boolean {
		return vein.blocks.any { block -> !type.isBlock(block) }
	}

	fun getVeinData(team: Team, regenResource: RegenResource): VeinData {
		return veinDataList[team]!![regenResource.ordinal]
	}

	fun tick(ticks: Int) {
		//TODO distributed timing

		/* don't need to be precise on timing */
		if (ticks % 20 != 7) return

		/* veinDatas for all active teams during this tick */
		val currentTeams = game.teams.teams().map { team ->
			Triple(
				team,
				veinDataList.getOrPut(team) { generateVeinDataEntry(ticks) },
				team.members.mapNotNull { Bukkit.getPlayer(it) },
			)
		}

		currentTeams.forEach { (team, veinDatas, teamPlayers) ->
			for (i in veinDatas.indices) {
				val veinData = veinDatas[i]
				val veinType = resourceDescriptions[i]

				veinData.current.removeAll { vein ->
					if (inSomeWayModified(veinType, vein)) return@removeAll true

					/* when found, the vein remains until forcibly removed by mining */
					if (currentTeams.any { (_, _, otherPlayers) -> otherPlayers.any { player ->
							val res = playerFinds(player, vein)
							if (res) Action.sendGameMessage(player, "found vein $vein")
							res
					} }) {
						foundVeins.add(veinType to vein)

					} else {
						false
					}
				}

				/* remove the oldest veins to make room for new ones */
				while (veinData.current.size > veinType.maxCurrent(veinData.collected)) {
					eraseVein(veinType, veinData.current.removeFirst())
				}

				/* attempt generations */
				if (teamPlayers.isNotEmpty() && ticks >= veinData.nextTime) {
					/* attempt to generate this vein for all the players on the team */
					/* only one will generate, but priority is given to 1 player on a cycle */
					var generatedList: List<Block>? = null

					for (j in teamPlayers.indices) {
						val generateAround = teamPlayers[(j + veinData.numGenerates) % teamPlayers.size].location.block

						generatedList = veinType.generateVein(generateAround.world, generateAround.x, generateAround.y, generateAround.z)
						if (generatedList != null) break
					}

					/* dividing by the number of team players esentially multiplies the rate */
					/* this results in the rates being the same per player on any size team */
					var addedTime = veinType.nextInterval(veinData.collected) / teamPlayers.size + Random.nextInt(-20, 20)

					/* if this generation failed, try again sooner than usual */
					if (generatedList == null) {
						Util.log("failed to place $veinType")
						addedTime /= 2
					} else {
						Util.log("placed $veinType")
						val originalData = generatedList.map { it.blockData }
						generatedList.forEach { veinType.setBlock(it) }

						veinData.current.add(Vein(
							originalData,
							generatedList,
							ticks,
						))

						++veinData.numGenerates
					}

					veinData.nextTime = ticks + addedTime
				}
			}
		}
	}
}

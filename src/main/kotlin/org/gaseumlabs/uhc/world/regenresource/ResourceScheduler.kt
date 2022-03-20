package org.gaseumlabs.uhc.world.regenresource

import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.gaseumlabs.uhc.core.Game
import org.gaseumlabs.uhc.team.Team
import kotlin.random.Random

class ResourceScheduler(
	val game: Game,
	val resourceDescriptions: Array<ResourceDescription>
) {
	companion object {
		const val FIND_RADIUS = 32.0
	}

	data class VeinData(
		var collected: Int,
		var numGenerates: Int,
		var nextTime: Int,
		var current: ArrayList<Vein>
	)

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

	private fun playerFinds(player: Player, vein: Vein): Boolean {
		val centerBlock = vein.centerBlock()

		if (player.world !== centerBlock.world) return false
		if (player.location.distance(centerBlock.location.toCenterLocation()) > FIND_RADIUS) return false

		return vein.blocks.any { block ->
			val blockLocation = block.location.toCenterLocation()
			val playerLocation = player.eyeLocation

			block.world.rayTrace(
				blockLocation,
				playerLocation.subtract(blockLocation).toVector(),
				FIND_RADIUS,
				FluidCollisionMode.NEVER,
				true,
				1.0
			) { it === player }
				?.hitEntity != null
		}
	}

	fun getVeinData(team: Team, resourceDescription: ResourceDescription): VeinData {
		return veinDataList[team]!![resourceDescriptions.indexOf(resourceDescription)]
	}

	fun tick(ticks: Int) {
		/* don't need to be precise on timing */
		if (ticks % 20 != 7) return

		val currentTeams = game.teams.teams().map { team ->
			team to veinDataList.getOrPut(team) { generateVeinDataEntry(ticks) }
		}

		val playersList = currentTeams.map { (team, _) ->
			team.members.mapNotNull { Bukkit.getPlayer(it) }
		}

		currentTeams.forEach { (team, veinDatas) ->
			for (i in veinDatas.indices) {
				val veinData = veinDatas[i]
				val teamPlayers = playersList[i]
				val veinType = resourceDescriptions[i]

				veinData.current.removeAll { vein ->
					/* when found, the vein remains until forcibly removed by mining */
					if (playersList.any { list -> list.any { playerFinds(it, vein) } }) {
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
						addedTime /= 2
					} else {
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

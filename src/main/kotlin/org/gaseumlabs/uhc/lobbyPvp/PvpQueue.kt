package org.gaseumlabs.uhc.lobbyPvp

import org.gaseumlabs.uhc.core.PlayerData
import org.gaseumlabs.uhc.gui.GuiManager
import org.gaseumlabs.uhc.gui.gui.QueueGUI
import org.gaseumlabs.uhc.lobbyPvp.arena.GapSlapArena
import org.gaseumlabs.uhc.lobbyPvp.arena.PvpArena
import org.gaseumlabs.uhc.util.PropertyGroup
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min
import java.util.UUID

object PvpQueue {
	const val TYPE_NONE = 0
	const val TYPE_1V1 = 1
	const val TYPE_2V2 = 2
	const val TYPE_GAP = 3

	const val QUEUE_TIME = 10
	const val QUEUE_EXTEND_TIME = 20

	data class QueueElement(val uuid: UUID, var type: Int, var time: Int)

	fun queueName(type: Int): String {
		return when (type) {
			TYPE_1V1 -> "1v1"
			TYPE_2V2 -> "2v2"
			TYPE_GAP -> "Gap Slap"
			else -> "???"
		}
	}

	private val propGroup = PropertyGroup { GuiManager.update(QueueGUI::class) }
	var enabled by propGroup.delegate(true, onChange = {
		if (!it) queue.removeIf { entry ->
			PlayerData.get(entry.uuid).inLobbyPvpQueue = TYPE_NONE
			true
		}
	})

	private val queue = ArrayList<QueueElement>()

	fun add(uuid: UUID, type: Int) {
		val existingEntry = queue.find { it.uuid == uuid }

		if (existingEntry == null) {
			queue.add(QueueElement(uuid, type, 0))

		} else {
			existingEntry.type = type
		}
	}

	fun remove(uuid: UUID) {
		queue.removeIf { it.uuid == uuid }
	}

	/**
	 * for displaying to players
	 * @return null if the player is not in queue
	 */
	fun queueTime(uuid: UUID): Int? {
		return queue.find { it.uuid == uuid }?.time
	}

	fun size(type: Int): Int {
		return queue.count { it.type == type }
	}

	data class QueuePair(
		val player1: UUID, val player2: UUID,
		val index1: Int, val index2: Int,
		val priority1: Int, val priority2: Int,
	)

	fun perSecond() {
		/* 1v1 queue */
		val queuePairs = ArrayList<QueuePair>((queue.size * queue.size) / 2)

		for (i in 1 until queue.size) {
			for (j in 0 until i) {
				val element1 = queue[i]
				val element2 = queue[j]

				if (element1.type == TYPE_1V1 && element2.type == TYPE_1V1) {
					val greatestTime = max(element1.time, element2.time)
					val leastTime = min(element1.time, element2.time)

					if (greatestTime >= QUEUE_TIME) {
						val playerData1 = PlayerData.get(element1.uuid)
						val playerData2 = PlayerData.get(element2.uuid)

						/* last played each other before */
						if (playerData1.lastPlayed == element2.uuid || playerData2.lastPlayed == element1.uuid) {
							if (greatestTime >= QUEUE_EXTEND_TIME && leastTime >= QUEUE_TIME) {
								queuePairs.add(QueuePair(
									element1.uuid, element2.uuid,
									i, j,
									0, greatestTime
								))
							}
							/* fresh opponent */
						} else {
							queuePairs.add(QueuePair(
								element1.uuid, element2.uuid,
								i, j,
								1, greatestTime
							))
						}
					}
				}
			}
		}

		queuePairs.sortWith(object : Comparator<QueuePair> {
			override fun compare(o1: QueuePair, o2: QueuePair): Int {
				if (o1.priority1 > o2.priority1) return 1
				if (o1.priority1 < o2.priority1) return -1
				if (o1.priority2 > o2.priority2) return 1
				if (o1.priority2 < o2.priority2) return -1
				return 0
			}
		})

		while (true) {
			val pairFound = queuePairs.lastOrNull()

			if (pairFound == null) {
				break

			} else {
				/* remove all traces of players added from remaining pairs */
				queuePairs.removeIf {
					it.player1 == pairFound.player1 ||
					it.player1 == pairFound.player2 ||
					it.player2 == pairFound.player1 ||
					it.player2 == pairFound.player2
				}

				/* remove them from the queue */
				PlayerData.get(pairFound.player1).inLobbyPvpQueue = TYPE_NONE
				PlayerData.get(pairFound.player2).inLobbyPvpQueue = TYPE_NONE

				/* create pvp game */
				ArenaManager.addNewArena(
					PvpArena(
						arrayListOf(arrayListOf(pairFound.player1), arrayListOf(pairFound.player2)),
						ArenaManager.nextCoords(),
						TYPE_1V1
					)
				)
			}
		}

		/* 2v2 queue */
		val availablePlayers = ArrayList(queue.filter { it.type == TYPE_2V2 }.sortedBy { it.time })

		/* try to create games in sections of 4 */
		while (true) {
			if (availablePlayers.size < 4) {
				break

			} else {
				val group = Array(4) { availablePlayers[availablePlayers.lastIndex - it].uuid }
				group.shuffle()

				/* remove them remaining available */
				availablePlayers.removeIf { element -> group.any { it == element.uuid } }

				/* remove them from the queue */
				group.forEach { PlayerData.get(it).inLobbyPvpQueue = TYPE_NONE }

				/* create pvp game */
				ArenaManager.addNewArena(
					PvpArena(
						arrayListOf(arrayListOf(group[0], group[1]), arrayListOf(group[2], group[3])),
						ArenaManager.nextCoords(),
						TYPE_2V2,
					)
				)
			}
		}

		/* gap slap queue */
		val availableGapSlappers =
			ArrayList(queue.filter { it.type == TYPE_GAP }.sortedBy { it.time })

		while (true) {
			if (availableGapSlappers.size < 2) {
				break
			} else if (availableGapSlappers.size < 4) {
				/* only make a game if all players have been waiting for 10 seconds */
				if (availableGapSlappers.any { it.time < QUEUE_TIME }) break

				startGapSlapArena(Array(availableGapSlappers.size) { availableGapSlappers.removeLast().uuid })
			} else {
				startGapSlapArena(Array(4) { availableGapSlappers.removeLast().uuid })
			}
		}

		/* time increase */
		queue.forEach { ++it.time }
	}

	fun startGapSlapArena(group: Array<UUID>) {
		val playerDatas = group.map { PlayerData.get(it) }

		/* remove them from the queue */
		playerDatas.forEach { it.inLobbyPvpQueue = TYPE_NONE }

		val platformUUID = determinePlatform(playerDatas) ?: return
		val platform = GapSlapArena.submittedPlatforms[platformUUID] ?: return

		playerDatas.forEach { it.addRecentPlatform(platformUUID) }

		ArenaManager.addNewArena(
			GapSlapArena(
				group.map { arrayListOf(it) } as ArrayList<ArrayList<UUID>>,
				ArenaManager.nextCoords(),
				platform,
			)
		)
	}

	fun determinePlatform(playerDatas: List<PlayerData>): UUID? {
		if (GapSlapArena.submittedPlatforms.isEmpty()) return null

		class KeyPair(val uuid: UUID, var negativeWeight: Int)

		val negativeWeights = GapSlapArena.submittedPlatforms.keys.associateWith { uuid -> KeyPair(uuid, 0) }

		playerDatas.forEach { playerData ->
			playerData.recentPlatforms.forEachIndexed { i, uuid ->
				val keyPair = negativeWeights[uuid]
				if (keyPair != null) keyPair.negativeWeight += 5 - i
			}
		}

		val keyPair = negativeWeights.values.maxByOrNull { it.negativeWeight } ?: return null

		return keyPair.uuid
	}
}

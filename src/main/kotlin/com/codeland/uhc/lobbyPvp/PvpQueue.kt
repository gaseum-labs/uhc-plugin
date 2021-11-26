package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.core.PlayerData
import com.codeland.uhc.lobbyPvp.arena.PvpArena
import com.codeland.uhc.util.UHCProperty
import java.util.*
import kotlin.math.max
import kotlin.math.min

object PvpQueue {
	const val QUEUE_TIME = 10
	const val QUEUE_EXTEND_TIME = 20

	data class QueueElement(val uuid: UUID, var type: Int, var time: Int)

	val enabled = UHCProperty(true) { set ->
		if (!set) queue.removeIf {
			PlayerData.getPlayerData(it.uuid).inLobbyPvpQueue.unsafeSet(0)
			true
		}

		set
	}

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

				if (element1.type == PvpArena.TYPE_1V1 && element2.type == PvpArena.TYPE_1V1) {
					val greatestTime = max(element1.time, element2.time)
					val leastTime = min(element1.time, element2.time)

					if (greatestTime >= QUEUE_TIME) {
						val playerData1 = PlayerData.getPlayerData(element1.uuid)
						val playerData2 = PlayerData.getPlayerData(element2.uuid)

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
				PlayerData.getPlayerData(pairFound.player1).inLobbyPvpQueue.set(0)
				PlayerData.getPlayerData(pairFound.player2).inLobbyPvpQueue.set(0)

				/* create pvp game */
				ArenaManager.addArena(
					PvpArena(
						arrayListOf(arrayListOf(pairFound.player1), arrayListOf(pairFound.player2)), PvpArena.TYPE_1V1
					)
				)
			}
		}

		/* 2v2 queue */
		val availablePlayers = ArrayList(queue.filter { it.type == PvpArena.TYPE_2V2 }.sortedBy { it.time })

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
				group.forEach { PlayerData.getPlayerData(it).inLobbyPvpQueue.set(0) }

				/* create pvp game */
				ArenaManager.addArena(
					PvpArena(
						arrayListOf(arrayListOf(group[0], group[1]), arrayListOf(group[2], group[3])), PvpArena.TYPE_2V2
					)
				)
			}
		}

		/* time increase */
		queue.forEach { ++it.time }
	}
}

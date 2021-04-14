package com.codeland.uhc.lobbyPvp

import com.codeland.uhc.core.PlayerData
import java.util.*
import kotlin.collections.ArrayList

object PvpQueue {
	data class QueueElement(val uuid: UUID, var time: Int, var remove: Boolean)

	private val queue = ArrayList<QueueElement>()

	fun add(uuid: UUID) {
		queue.add(QueueElement(uuid, 0, false))
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

	fun size(): Int {
		return queue.size
	}

	fun perSecond() {
		queue.forEach { element ->
			if (!element.remove) {
				val pvpData = PlayerData.getLobbyPvp(element.uuid)

				for (element2 in queue) {
					val requiredTime = if (element2.uuid == pvpData.lastPlayed) 20 else 10

					if (!element2.remove && element.uuid != element2.uuid && element.time >= requiredTime) {
						PvpGameManager.addGame(arrayOf(element.uuid, element2.uuid))

						element.remove = true
						element2.remove = true

						break
					}
				}
			}
		}

		queue.removeIf { element ->
			if (element.remove) {
				true
			} else {
				++element.time
				false
			}
		}
	}
}

package com.codeland.uhc.lobbyPvp

import java.util.*
import kotlin.collections.ArrayList

class Loadouts(
	val uuids: ArrayList<UUID> = ArrayList(),
	val loadouts: ArrayList<Array<Array<Int>>> = ArrayList()
) {
	companion object {
		const val NUM_SLOTS = 3
		const val LOADOUT_SIZE = 9 * 4

		fun defaultArray() = Array(NUM_SLOTS) { LoadoutItems.defaultLoadout() }
	}

	fun getLoadouts(uuid: UUID): Array<Array<Int>> {
		val index = uuids.indexOfFirst { it == uuid }

		return if (index == -1) {
			val slots = defaultArray()

			uuids.add(uuid)
			loadouts.add(slots)

			slots

		} else {
			loadouts[index]
		}
	}
}

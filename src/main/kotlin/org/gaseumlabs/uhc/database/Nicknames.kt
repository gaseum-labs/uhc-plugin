package org.gaseumlabs.uhc.database

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Nicknames(private val map: HashMap<UUID, ArrayList<String>>) {
	fun addNick(uuid: UUID, nickname: String): Boolean {
		val list = map.getOrPut(uuid) { ArrayList() }

		if (list.contains(nickname)) return false

		list.add(nickname)
		return true
	}

	fun removeNick(uuid: UUID, nickname: String): Boolean {
		return map[uuid]?.remove(nickname) ?: false
	}

	fun getNicks(uuid: UUID): ArrayList<String> {
		return map[uuid] ?: ArrayList()
	}

	fun allNicks(): Iterable<Map.Entry<UUID, ArrayList<String>>> {
		return map.asIterable()
	}
}
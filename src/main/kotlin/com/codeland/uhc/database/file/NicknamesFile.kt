package com.codeland.uhc.database.file

import com.codeland.uhc.database.DatabaseFile
import java.sql.CallableStatement
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NicknamesFile : DatabaseFile<NicknamesFile.Nicknames, NicknamesFile.NicknameEntry>() {
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

	class NicknameEntry(val uuid: UUID, val nickname: String)

	override fun query(): String {
		return "SELECT uuid, nickname FROM Nickname"
	}

	override fun parseResults(results: ResultSet): Nicknames {
		val map = HashMap<UUID, ArrayList<String>>()

		while (results.next()) {
			val uuid = UUID.fromString(results.getString(1))
			val name = results.getNString(2)

			map.getOrPut(uuid) { ArrayList() }.add(name)
		}

		return Nicknames(map)
	}

	override fun defaultData(): Nicknames {
		return Nicknames(HashMap())
	}

	override fun pushProcedure(): String {
		return "EXECUTE updateNickname ?, ?;"
	}

	override fun removeProcedure(): String {
		return "EXECUTE removeNickname ?, ?;"
	}

	override fun giveParams(statement: CallableStatement, entry: NicknameEntry) {
		statement.setString(1, entry.uuid.toString())
		statement.setString(2, entry.nickname)
	}
}

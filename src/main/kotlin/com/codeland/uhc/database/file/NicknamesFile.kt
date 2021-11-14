package com.codeland.uhc.database.file

import com.codeland.uhc.database.DatabaseFile
import java.sql.CallableStatement
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NicknamesFile : DatabaseFile<NicknamesFile.Nicknames, NicknamesFile.NicknameEntry>() {
	class Nicknames(val map: HashMap<UUID, ArrayList<String>>)

	class NicknameEntry(val uuid: UUID, val nickname: String)

	override fun query(): String {
		//language=sql
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

	override fun pushParams(statement: CallableStatement, entry: NicknameEntry) {
		statement.setString(1, entry.uuid.toString())
		statement.setString(2, entry.nickname)
	}

	override fun removeQuery(entry: NicknameEntry): String {
		//language=sql
		return "DELETE FROM Nickname WHERE uuid = ${sqlString(entry.uuid)} AND nickname = ${sqlString(entry.nickname)};"
	}
}

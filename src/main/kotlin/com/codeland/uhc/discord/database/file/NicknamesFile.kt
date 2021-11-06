package com.codeland.uhc.discord.database.file

import com.codeland.uhc.discord.database.DatabaseFile
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
			val uuid = UUID.fromString(results.getString(0))
			val name = results.getNString(1)

			map.getOrPut(uuid) { ArrayList() }.add(name)
		}

		return Nicknames(map)
	}

	override fun defaultData(): Nicknames {
		return Nicknames(HashMap())
	}

	override fun pushQuery(entry: NicknameEntry): String {
		//language=sql
		return "EXECUTE updateNickname ${sqlString(entry.uuid)}, ${sqlString(entry.nickname)};"
	}

	override fun removeQuery(entry: NicknameEntry): String {
		//language=sql
		return "DELETE FROM Nickname WHERE uuid = ${sqlString(entry.uuid)} AND nickname = ${sqlString(entry.nickname)};"
	}
}

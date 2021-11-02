package com.codeland.uhc.discord.sql.file

import com.codeland.uhc.discord.sql.DatabaseFile
import com.codeland.uhc.lobbyPvp.Loadout
import com.codeland.uhc.lobbyPvp.Loadouts
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NicknamesFile : DatabaseFile<NicknamesFile.Nicknames, NicknamesFile.NicknameEntry>() {
	class Nicknames(val map: HashMap<UUID, ArrayList<String>>)

	class NicknameEntry(val uuid: UUID, val nickname: String)

	override fun query(): String {
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
		return """
			DECLARE @uuid UNIQUEIDENTIFIER = '${entry.uuid}';
			DECLARE @nickname VARCHAR(MAX) = '${entry.nickname}';

			INSERT INTO Nickname (uuid, nickname) VALUES (@uuid, @nickname); 
		""".trimIndent()
	}
}

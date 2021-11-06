package com.codeland.uhc.discord.sql.file

import com.codeland.uhc.discord.sql.DatabaseFile
import java.sql.ResultSet
import java.util.*
import kotlin.collections.HashMap

class LinkDataFile : DatabaseFile<LinkDataFile.LinkData, LinkDataFile.LinkEntry>() {
	class LinkData(
		val minecraftToDiscord: HashMap<UUID, Long>,
		val discordToMinecraft: HashMap<Long, UUID>,
	)

	class LinkEntry(val uuid: UUID, val name: String?, val discordId: Long?)

	override fun query(): String {
		//language=sql
		return "SELECT uuid, discordId FROM Player;"
	}

	override fun parseResults(results: ResultSet): LinkData {
		val map0 = HashMap<UUID, Long>()
		val map1 = HashMap<Long, UUID>()

		while (results.next()) {
			val uuid = UUID.fromString(results.getString(0))
			val discordId = results.getLong(1)

			map0[uuid] = discordId
			map1[discordId] = uuid
		}

		return LinkData(map0, map1)
	}

	override fun defaultData(): LinkData {
		return LinkData(HashMap(), HashMap())
	}

	override fun pushQuery(entry: LinkEntry): String {
		//language=sql
		return "EXECUTE updatePlayer ${sqlString(entry.uuid)}, ${sqlNullString(entry.name)}, ${sqlNull(entry.discordId)};"
	}
}

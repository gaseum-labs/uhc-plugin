package com.codeland.uhc.discord.sql.file

import com.codeland.uhc.discord.sql.DatabaseFile
import java.sql.ResultSet
import java.util.*

class LinkDataFile : DatabaseFile<LinkDataFile.LinkData, LinkDataFile.LinkEntry>() {
	class LinkData(
		val minecraftToDiscord: Map<UUID, Long>,
		val discordToMinecraft: Map<Long, UUID>,
	)

	class LinkEntry(val uuid: UUID, val name: String?, val discordId: Long?)

	override fun query(): String {
		return "SELECT uuid, discordId FROM Player"
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
		return LinkData(emptyMap(), emptyMap())
	}

	override fun pushQuery(entry: LinkEntry): String {
		return """
			DECLARE @uuid UNIQUEIDENTIFIER = '${entry.uuid}';
			DECLARE @name VARCHAR(MAX) = ${if (entry.name == null) "NULL" else "'${entry.name}'"};
			DECLARE @discordId BIGINT = ${if (entry.discordId == null) "NULL" else "${entry.discordId}"};
			
			IF EXISTS (SELECT uuid FROM Player WHERE uuid = @uuid) BEGIN
			    IF @name IS NOT NULL BEGIN
			        UPDATE Player SET name = @name WHERE uuid = @uuid;
			    END
			    IF @discordId IS NOT NULL BEGIN
			        UPDATE Player SET discordId = @discordId WHERE uuid = @uuid;
			    END
			END
			ELSE BEGIN
			    INSERT INTO Player (uuid, name, discordId) VALUES (@uuid, ISNULL(@name, 'Unknown'), @discordId); 
			END
		""".trimIndent()
	}
}

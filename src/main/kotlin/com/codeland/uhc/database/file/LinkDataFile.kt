package com.codeland.uhc.database.file

import com.codeland.uhc.database.DatabaseFile
import com.codeland.uhc.util.extensions.ResultSetExtensions.getLongNull
import com.codeland.uhc.util.extensions.ResultSetExtensions.setLongNull
import java.sql.CallableStatement
import java.sql.ResultSet
import java.util.*
import kotlin.collections.HashMap

class LinkDataFile : DatabaseFile<LinkDataFile.LinkData, LinkDataFile.LinkEntry>() {
	class LinkData(
		private val discordToMinecraft: HashMap<Long, UUID>,
		private val inverseMap: HashMap<UUID, Long>,
	) {
		/**
		 * @return if this link was allowed
		 * will be rejected if the uuid is already taken by some other discordId
		 */
		fun addLink(uuid: UUID, discordId: Long): Boolean {
			/* this uuid is already taken */
			if (discordToMinecraft.containsValue(uuid)) return false

			val oldUuid = discordToMinecraft[discordId]
			discordToMinecraft[discordId] = uuid

			if (oldUuid != null) inverseMap.remove(oldUuid)
			inverseMap[uuid] = discordId

			return true
		}

		/**
		 * @return if the discordId was linked
		 */
		fun revokeLink(discordId: Long): Boolean {
			val oldUuid = discordToMinecraft[discordId]

			return if (oldUuid != null) {
				discordToMinecraft.remove(discordId)
				inverseMap.remove(oldUuid)
				true

			} else {
				false
			}
		}

		fun getUuid(discordId: Long): UUID? {
			return discordToMinecraft[discordId]
		}

		fun getDiscordId(uuid: UUID): Long? {
			return inverseMap[uuid]
		}

		fun isLinked(uuid: UUID): Boolean {
			return getDiscordId(uuid) != null
		}

		/* for debug purposes */
		fun maps(): Pair<HashMap<Long, UUID>, HashMap<UUID, Long>> {
			return Pair(discordToMinecraft, inverseMap)
		}
	}

	class LinkEntry(val uuid: UUID, val name: String?, val discordId: Long?)

	override fun query(): String {
		//language=sql
		return "SELECT uuid, discordId FROM Player;"
	}

	override fun parseResults(results: ResultSet): LinkData {
		val map0 = HashMap<Long, UUID>()
		val map1 = HashMap<UUID, Long>()

		while (results.next()) {
			val uuid = UUID.fromString(results.getString(1))
			val discordId = results.getLongNull(2)

			if (discordId != null) {
				map0[discordId] = uuid
				map1[uuid] = discordId
			}
		}

		return LinkData(map0, map1)
	}

	override fun defaultData(): LinkData {
		return LinkData(HashMap(), HashMap())
	}

	override fun pushProcedure(): String {
		return "EXECUTE updatePlayer ?, ?, ?;"
	}

	override fun pushParams(statement: CallableStatement, entry: LinkEntry) {
		statement.setString(1, entry.uuid.toString())
		statement.setString(2, entry.name)
		statement.setLongNull(3, entry.discordId)
	}

	override fun removeQuery(entry: LinkEntry): String? {
		return null
	}
}

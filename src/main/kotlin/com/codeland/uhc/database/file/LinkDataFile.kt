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
		enum class LinkStatus {
			ALREADY_LINKED,
			TAKEN,
			SUCCESSFUL
		}

		/**
		 * @return if the link was successful
		 *
		 * can reject if the uuid has already been taken
		 * or if the discordId is already linked to the uuid
		 */
		fun addLink(discordId: Long, uuid: UUID): LinkStatus {
			val existingLinker = inverseMap[uuid]
			if (existingLinker == discordId) return LinkStatus.ALREADY_LINKED
			else if (existingLinker != null) return LinkStatus.TAKEN

			val oldUuid = discordToMinecraft[discordId]
			discordToMinecraft[discordId] = uuid

			if (oldUuid != null) inverseMap.remove(oldUuid)
			inverseMap[uuid] = discordId

			return LinkStatus.SUCCESSFUL
		}

		/**
		 * @return if the discordId was linked
		 */
		fun revokeLink(discordId: Long): UUID? {
			val oldUuid = discordToMinecraft.remove(discordId) ?: return null
			inverseMap.remove(oldUuid)
			return oldUuid
		}

		/**
		 * @return if the uuid was linked
		 */
		fun revokeLink(uuid: UUID): Long? {
			val oldDiscordId = inverseMap.remove(uuid) ?: return null
			discordToMinecraft.remove(oldDiscordId)
			return oldDiscordId
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

	class LinkEntry(val uuid: UUID?, val name: String?, val discordId: Long?)

	override fun query(): String {
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
		return "EXECUTE updateLink ?, ?, ?;"
	}

	override fun pushParams(statement: CallableStatement, entry: LinkEntry) {
		statement.setLongNull(1, entry.discordId)
		statement.setString(2, entry.uuid?.toString())
		statement.setString(3, entry.name)
	}

	override fun removeQuery(entry: LinkEntry): String? {
		return null
	}
}

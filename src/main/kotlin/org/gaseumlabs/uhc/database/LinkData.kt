package org.gaseumlabs.uhc.database

import java.util.*
import kotlin.collections.HashMap

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
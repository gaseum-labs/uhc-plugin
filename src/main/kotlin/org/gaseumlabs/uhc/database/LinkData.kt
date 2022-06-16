package org.gaseumlabs.uhc.database

import org.bukkit.Bukkit
import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.database.LinkData.Status.*
import org.gaseumlabs.uhc.util.Util
import org.gaseumlabs.uhc.util.Util.void
import java.util.*
import kotlin.collections.HashMap

class LinkData {
	private val linkStatuses = HashMap<UUID, LinkStatus>()

	enum class Status {
		UNKNOWN,
		UNLINKED,
		LINKED,
	}

	class LinkStatus(
		var status: Status,
		var discordId: Long,
	)

	private fun defaultLinkStatus(): LinkStatus {
		return LinkStatus(UNKNOWN, 0)
	}

	private fun internalGet(uuid: UUID): LinkStatus {
		return linkStatuses.getOrPut(uuid) { defaultLinkStatus() }
	}

	fun updateLink(uuid: UUID, discordId: Long?) {
		if (discordId == null) {
			internalGet(uuid).status = UNLINKED
		} else {
			val linkStatus = internalGet(uuid)
			linkStatus.status = LINKED
			linkStatus.discordId = discordId
		}
	}

	fun isLinked(uuid: UUID): Boolean {
		return internalGet(uuid).status == LINKED
	}

	fun isUnlinked(uuid: UUID): Boolean {
		return internalGet(uuid).status == UNLINKED
	}

	fun getDiscordId(uuid: UUID): Long? {
		val linkStatus = internalGet(uuid)
		return if (linkStatus.status == LINKED) linkStatus.discordId else null
	}

	fun playersIndividualLink(uuid: UUID) {
		UHC.dataManager.getSingleDiscordId(uuid).thenAccept { discordId ->
			updateLink(uuid, discordId)
		}.exceptionally { ex ->
			Util.warn("Bad request? $ex").void()
		}
	}

	fun massPlayersLink() {
		val playerList = Bukkit.getOnlinePlayers().map { it.uniqueId }
		if (playerList.isEmpty()) return
		
		UHC.dataManager.getMassDiscordIds(playerList).thenAccept { linkMap ->
			for ((uuid, discordId) in linkMap.entries) {
				updateLink(uuid, discordId)
			}
		}.exceptionally { ex -> Util.warn("Bad request? $ex").void() }
	}

	/* for debug purposes */
	fun maps(): HashMap<UUID, LinkStatus> {
		return linkStatuses
	}
}
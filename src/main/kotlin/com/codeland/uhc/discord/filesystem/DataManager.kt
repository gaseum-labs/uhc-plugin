package com.codeland.uhc.discord.filesystem

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild

class DataManager(
	val jda: JDA,
	val guildId: Long,
	var ids: IdsFile.Companion.Ids,
	var linkData: LinkDataFile.Companion.LinkData,
	var nicknamesFile: NicknamesFile.Companion.Nicknames
) {
	companion object {
		const val CATEGORY_NAME = "bot"
		const val DATA_CHANNEL_NAME = "data"

		const val IDS_HEADER = "ids"
		const val LINK_DATA_HEADER = "link data"
		const val NICKNAMES_HEADER = "nicknames"

		val idsFile = IdsFile(IDS_HEADER, DATA_CHANNEL_NAME)
		val linkDataFile = LinkDataFile(LINK_DATA_HEADER, DATA_CHANNEL_NAME)
		val nicknamesFile = NicknamesFile(NICKNAMES_HEADER, DATA_CHANNEL_NAME)

		fun createDataManager(jda: JDA, guildId: Long): DataManager? {
			val guild = jda.getGuildById(guildId) ?: return null
			val category = getBotCategory(guild) ?: return null

			val ids = idsFile.load(category) ?: return null
			val linkData = linkDataFile.load(category) ?: return null
			val nicknames = nicknamesFile.load(category) ?: return null

			return DataManager(jda, guildId, ids, linkData, nicknames)
		}

		fun getBotCategory(guild: Guild): Category? {
			val categories = guild.getCategoriesByName(CATEGORY_NAME, true)

			return when {
				categories.isEmpty() -> guild.createCategory(CATEGORY_NAME).complete()
				categories.size > 1 -> null
				else -> categories.first()
			}
		}
	}
}

package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.filesystem.file.IdsFile
import com.codeland.uhc.discord.filesystem.file.LinkDataFile
import com.codeland.uhc.discord.filesystem.file.NicknamesFile
import net.dv8tion.jda.api.JDA

class DataManager(
	val jda: JDA,
	val guildId: Long,
	var ids: IdsFile.Companion.Ids,
	var linkData: LinkDataFile.Companion.LinkData,
	var nicknames: NicknamesFile.Companion.Nicknames
) {
	companion object {
		fun createDataManager(jda: JDA, guildId: Long): DataManager? {
			val guild = jda.getGuildById(guildId) ?: return null
			val category = DiscordFilesystem.getBotCategory(guild) ?: return null

			val ids = DiscordFilesystem.idsFile.load(category) ?: return null
			val linkData = DiscordFilesystem.linkDataFile.load(category) ?: return null
			val nicknames = DiscordFilesystem.nicknamesFile.load(category) ?: return null

			return DataManager(jda, guildId, ids, linkData, nicknames)
		}
	}
}

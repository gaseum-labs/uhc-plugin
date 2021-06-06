package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.filesystem.file.IdsFile
import com.codeland.uhc.discord.filesystem.file.LinkDataFile
import com.codeland.uhc.discord.filesystem.file.NicknamesFile
import com.codeland.uhc.util.Util
import net.dv8tion.jda.api.JDA
import org.bukkit.ChatColor

class DataManager(
	var ids: IdsFile.Companion.Ids,
	var linkData: LinkDataFile.Companion.LinkData,
	var nicknames: NicknamesFile.Companion.Nicknames
) {
	companion object {
		fun Any?.void() = null

		fun createDataManager(jda: JDA, guildId: Long, onError: (String) -> Unit): DataManager? {
			val guild = jda.getGuildById(guildId) ?: return onError("Guild by id $guildId could not be found").void()
			val category = DiscordFilesystem.getBotCategory(guild) ?: return onError("Bot Category could not be created").void()

			fun printErr(msg: String) = Util.log("${ChatColor.RED}${msg}")

			val ids = DiscordFilesystem.idsFile.load(category, ::printErr)
			val linkData = DiscordFilesystem.linkDataFile.load(category, ::printErr)
			val nicknames = DiscordFilesystem.nicknamesFile.load(category, ::printErr)

			return DataManager(ids, linkData, nicknames)
		}
	}
}

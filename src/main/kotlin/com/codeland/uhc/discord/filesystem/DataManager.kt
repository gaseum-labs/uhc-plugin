package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.filesystem.file.IdsFile
import com.codeland.uhc.discord.filesystem.file.LinkDataFile
import com.codeland.uhc.discord.filesystem.file.NicknamesFile
import com.codeland.uhc.lobbyPvp.Loadouts
import com.codeland.uhc.util.Util
import net.dv8tion.jda.api.JDA
import org.bukkit.ChatColor
import java.lang.Exception
import java.util.concurrent.CompletableFuture

object DataManager {
	var ids = IdsFile.Companion.Ids()
	var linkData = LinkDataFile.Companion.LinkData()
	var nicknames = NicknamesFile.Companion.Nicknames()
	var loadouts = Loadouts()

	fun Any?.void() = null
	fun Any?.bool(b: Boolean) = b

	fun createDataManager(jda: JDA, guildId: Long): CompletableFuture<DataManager> {
		val future = CompletableFuture<DataManager>()

		val guild = jda.getGuildById(guildId)
		if (guild == null) {
			future.completeExceptionally(Exception("Guild by id $guildId could not be found"))
			return future
		}

		val category = DiscordFilesystem.getBotCategory(guild)
		if (category == null) {
			future.completeExceptionally(Exception("Bot Category could not be created"))
			return future
		}

		fun printErr(name: String) = { msg: String -> println("${ChatColor.RED}Error loading $name file | $msg") }

		DiscordFilesystem.idsFile.load(category, printErr("ids")).thenAccept { ids ->
			this.ids = ids
		}

		DiscordFilesystem.linkDataFile.load(category, printErr("link data")).thenAccept { linkData ->
			this.linkData = linkData
		}

		DiscordFilesystem.nicknamesFile.load(category, printErr("nicknames")).thenAccept { nicknames ->
			this.nicknames = nicknames
		}

		DiscordFilesystem.loadoutsFile.load(category, printErr("loadouts")).thenAccept { loadouts ->
			this.loadouts = loadouts
		}

		return future
	}
}

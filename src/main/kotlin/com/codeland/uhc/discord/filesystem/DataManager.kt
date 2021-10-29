package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.filesystem.file.IdsFile
import com.codeland.uhc.discord.filesystem.file.LinkDataFile
import com.codeland.uhc.discord.filesystem.file.NicknamesFile
import com.codeland.uhc.lobbyPvp.Loadouts
import net.dv8tion.jda.api.JDA
import org.bukkit.ChatColor
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

		fun printErr(name: String) = { ex: Throwable -> println("${ChatColor.RED}Error loading $name file | ${ex.message}").void() }

		DiscordFilesystem.idsFile.load(category).thenAccept { ids = it }.exceptionally(printErr("ids"))
		DiscordFilesystem.linkDataFile.load(category).thenAccept { linkData = it }.exceptionally(printErr("link data"))
		DiscordFilesystem.nicknamesFile.load(category).thenAccept { nicknames = it }.exceptionally(printErr("nicknames"))
		DiscordFilesystem.loadoutsFile.load(category).thenAccept { loadouts = it }.exceptionally(printErr("loadouts"))

		return future
	}
}

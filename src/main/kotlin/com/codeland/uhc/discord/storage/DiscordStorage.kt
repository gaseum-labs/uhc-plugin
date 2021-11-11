package com.codeland.uhc.discord.storage

import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Util.void
import net.dv8tion.jda.api.entities.Guild

enum class DiscordStorage(val entry: DiscordStorageEntry<*>) {
	ADMIN_ROLE(DiscordStorageEntryLong("adminRole", 0)),
	COLOR0(DiscordStorageEntryHex("color0", 0xffffff)),
	COLOR1(DiscordStorageEntryHex("color1", 0xffffff)),
	SPLASH_TEXT(DiscordStorageEntryString("splashText", "..."));

	companion object {
		fun <T> sBy(storage: DiscordStorage): DiscordStorageEntry<T> = storage.entry as DiscordStorageEntry<T>

		var adminRole: Long? by sBy(ADMIN_ROLE)
		var color0: Int? by sBy(COLOR0)
		var color1: Int? by sBy(COLOR1)
		var splashText: String? by sBy(SPLASH_TEXT)

		fun load(guild: Guild) {
			Channels.getCategoryChannel(
				guild,
				Channels.DATA_CATEGORY_NAME,
				Channels.DATA_CHANNEL_NAME
			).thenAccept { (_, channel) ->
				Channels.allChannelMessages(channel) { message ->
					val text = message.contentRaw
					if (text.startsWith("```") && text.endsWith("```")) {
						val codeBlock = text.substring(3, text.length - 3).trim()

						values().any { (entry) ->
							if (entry.isThis(codeBlock)) {
								when (val r = entry.setData(codeBlock)) {
									is Bad -> channel.sendMessage("Error while reading data for ${entry.name}\n${r.error}").queue()
								}
								true
							} else {
								false
							}
						}
					}
				}

				values().forEach { (entry) ->
					if (entry.value == null) {
						channel.sendMessage("```${entry.name}=PLEASE INPUT DATA```").queue()
					} else {
						println("${entry.name} | ${entry.value}")
					}
				}
			}.exceptionally { ex: Throwable ->
				println("Error while reading data\n${ex.message}").void()
			}
		}
	}

	private operator fun component1(): DiscordStorageEntry<*> {
		return entry
	}
}

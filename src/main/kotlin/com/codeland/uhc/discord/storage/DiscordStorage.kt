package com.codeland.uhc.discord.storage

import com.codeland.uhc.discord.Channels
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Util.void
import net.dv8tion.jda.api.entities.Guild

enum class DiscordStorage(val entry: StorageEntry<*>) {
	ADMIN_ROLE(StorageEntryLong("adminRole")),
	COLOR0(StorageEntryHex("color0")),
	COLOR1(StorageEntryHex("color1")),
	SPLASH_TEXT(StorageEntryString("splashText"));

	companion object {
		fun <T> sBy(storage: DiscordStorage): StorageEntry<T> = storage.entry as StorageEntry<T>

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
				val builder = StringBuilder()

				Channels.allChannelMessages(channel) { message ->
					val text = message.contentRaw
					if (text.startsWith("```") && text.endsWith("```")) {
						builder.append(text.substring(3, text.length - 3) + ' ')
					}
				}

				StorageEntry.massAssign(
					values().map { it.entry } as ArrayList<StorageEntry<*>>,
					StorageEntry.getParams(builder.toString())
				).forEach { error ->
					channel.sendMessage(error).queue()
				}

				values().map { it.entry }.forEach { entry ->
					if (entry.value == null) {
						channel.sendMessage("```${entry.name}=\"NO VALUE SUPPLIED\"```").queue()
					}
				}

			}.exceptionally { ex: Throwable ->
				println("Error while reading data\n${ex.message}").void()
			}
		}
	}

	private operator fun component1(): StorageEntry<*> {
		return entry
	}
}

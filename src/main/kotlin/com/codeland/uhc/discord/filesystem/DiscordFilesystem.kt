package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.util.Bad
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import java.io.InputStream
import java.util.concurrent.CompletableFuture

object DiscordFilesystem {
	const val CATEGORY_NAME = "bot"
	const val DATA_CHANNEL_NAME = "data"

	fun getBotCategory(guild: Guild): Category? {
		val categories = guild.getCategoriesByName(CATEGORY_NAME, true)

		return when {
			categories.isEmpty() -> guild.createCategory(CATEGORY_NAME).complete()
			categories.size > 1 -> null
			else -> categories.first()
		}
	}

	fun getChannel(category: Category, name: String): TextChannel? {
		return category.textChannels.find { it.name == name } ?: category.createTextChannel(name).complete()
	}

	fun messageStream(message: Message): CompletableFuture<InputStream> {
		val attachment = message.attachments.firstOrNull()

		return if (attachment == null) {
			val future = CompletableFuture<InputStream>()
			future.completeExceptionally(Exception("Message does not have an attachment"))
			future

		} else {
			attachment.retrieveInputStream()
		}
	}

	fun isDataChannel(channel: TextChannel): Boolean {
		return channel.parent?.name == CATEGORY_NAME && channel.name == DATA_CHANNEL_NAME
	}

	fun isSummaryStagingChannel(bot: MixerBot, channel: TextChannel): Boolean {
		return channel.idLong == bot.dataManager.ids.summaryStagingChannel
	}

	fun <B> fieldError(name: String, type: String): Bad<B> {
		return Bad("No value for \"${name}\" <${type}> found")
	}
}

package com.codeland.uhc.discord

import net.dv8tion.jda.api.entities.*
import java.io.InputStream
import java.util.concurrent.CompletableFuture

object Channels {
	const val DATA_CATEGORY_NAME = "data"
	const val SUMMARIES_CATEGORY_NAME = "summaries"
	const val VOICE_CATEGORY_NAME = "voice"

	const val DATA_CHANNEL_NAME = "data"
	const val SUMMARY_STAGING_CHANNEL_NAME = "summary-staging"
	const val SUMMARIES_CHANNEL_NAME = "summaries"
	const val GENERAL_VOICE_CHANNEL_NAME = "general"

	fun getCategory(guild: Guild, name: String): CompletableFuture<Category> {
		val category = guild.categoryCache.find { it.name == name }

		return if (category != null) {
			CompletableFuture.completedFuture(category)

		} else {
			val future = CompletableFuture<Category>()
			guild.createCategory(name).queue(future::complete)
			future
		}
	}

	fun getChannel(category: Category, name: String): CompletableFuture<TextChannel> {
		val channel = category.textChannels.find { it.name == name }

		return if (channel != null) {
			CompletableFuture.completedFuture(channel)

		} else {
			val future = CompletableFuture<TextChannel>()
			category.createTextChannel(name).queue(future::complete)
			future
		}
	}

	fun getVoiceChannel(category: Category, name: String): CompletableFuture<VoiceChannel> {
		val channel = category.voiceChannels.find { it.name == name }

		return if (channel != null) {
			CompletableFuture.completedFuture(channel)

		} else {
			val future = CompletableFuture<VoiceChannel>()
			category.createVoiceChannel(name).queue(future::complete)
			future
		}
	}

	fun getCategoryChannel(guild: Guild, categoryName: String, channelName: String): CompletableFuture<Pair<Category, TextChannel>> {
		return getCategory(guild, categoryName).thenApply { category ->
			Pair(category, getChannel(category, channelName).get())
		}
	}

	fun getCategoryVoiceChannel(guild: Guild, categoryName: String, channelName: String): CompletableFuture<Pair<Category, VoiceChannel>> {
		return getCategory(guild, categoryName).thenApply { category ->
			Pair(category, getVoiceChannel(category, channelName).get())
		}
	}

	fun messageStream(message: Message): CompletableFuture<InputStream> {
		return message.attachments.firstOrNull()?.retrieveInputStream() ?:
			CompletableFuture.failedFuture(Exception("Message does not have an attachment"))
	}

	fun messageStreamOptional(message: Message): CompletableFuture<InputStream?> {
		return message.attachments.firstOrNull()?.retrieveInputStream() ?:
		CompletableFuture.completedFuture(null)
	}

	fun allChannelMessages(channel: TextChannel, onMessage: (Message) -> Unit) {
		val history = channel.history

		while (true) {
			val gotten = history.retrievePast(100).complete()
			if (gotten.isEmpty()) break

			gotten.forEach { onMessage(it) }
		}
	}
}

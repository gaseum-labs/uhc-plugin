package com.codeland.uhc.discord

import com.codeland.uhc.core.UHC
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import java.io.InputStream
import java.util.concurrent.CompletableFuture

object DiscordFilesystem {
	const val CATEGORY_NAME = "bot"

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

	fun isSummaryStagingChannel(channel: TextChannel): Boolean {
		return channel.idLong == UHC.dataManager.ids.summaryStagingChannel
	}
}

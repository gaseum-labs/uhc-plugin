package com.codeland.uhc.discord.filesystem

import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Message

abstract class DiscordFile <D> (val header: String, val channelName: String) {
	var cachedMessageId: Long? = null

	fun load(category: Category): D? {
		val message = getMessage(category)

		return if (message == null) {
			/* when there is no message to load create default */
			DiscordFilesystem.getChannel(category, channelName)
				?.sendMessage(defaultContents())

			null

		} else {
			fromContents(DiscordFilesystem.messageData(message))
		}
	}

	fun save(category: Category, data: D) {
		val contents = DiscordFilesystem.createMessageContent(header, writeContents(data))

		val message = getMessage(category)

		if (message != null) {
			message.editMessage(contents).complete()

		} else {
			DiscordFilesystem.getChannel(category, channelName)
				?.sendMessage(contents)?.complete()
		}
	}

	private fun getMessage(category: Category): Message? {
		val messageId = cachedMessageId
		val channel = DiscordFilesystem.getChannel(category, channelName) ?: return null

		/* we've already found the message before */
		if (messageId != null) try {
			return channel.retrieveMessageById(messageId).complete()
		} catch (ex: Exception) {}

		/* find the message for the first time (or if cache fails) */
		val message = DiscordFilesystem.findSingleMessage(channel, header) ?: return null

		/* save found message to cache */
		cachedMessageId = message.idLong

		return message
	}

	protected abstract fun fromContents(contents: String): D?
	protected abstract fun writeContents(data: D): String
	protected abstract fun defaultContents(): String
}

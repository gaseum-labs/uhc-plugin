package com.codeland.uhc.discord.filesystem

import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse

object DiscordFilesystem {
	fun getChannel(category: Category, name: String): TextChannel? {
		return category.textChannels.find { it.name == name } ?: category.createTextChannel(name).complete()
	}

	fun messageData(message: Message): String {
		val content = message.contentRaw

		/* the part of the message after the beginning code block */
		return content.substring(content.indexOf("```", 3) + 3)
	}

	fun createMessageContent(header: String, content: String): String {
		return "```${header}```${content}"
	}

	fun messageHeader(message: Message): String? {
		/* data messages are sent by this bot or UHC server bot */
		if (!message.author.isBot) return null

		val content = message.contentRaw

		/* find the opening and closing header backticks */
		/* at the beginning of the message */
		if (!content.startsWith("```")) return null
		val headerEnding = content.indexOf("```", 3)
		if (headerEnding == -1) return null

		/* message header is within backticks */
		return content.substring(3, headerEnding)
	}

	fun findMessages(channel: TextChannel, names: Array<String>): Array<Message?> {
		val foundMessages = arrayOfNulls<Message>(names.size)
		val history = channel.history

		/* look for messages in the channel */
		/* until all messages of the given names are found */
		/* or until the end of the channel is reached */
		while (true) {
			val section = history.retrievePast(100).complete()
			if (section.isEmpty()) return foundMessages

			/* match messages to the names provided */
			/* place them in foundMessages */
			section.forEach { message ->
				val header = messageHeader(message)
				var allFound = true

				for (i in foundMessages.indices) {
					if (foundMessages[i] == null) {
						if (names[i] == header)
							foundMessages[i] = message
						else
							allFound = false
					}
				}

				if (allFound) return foundMessages
			}
		}
	}

	fun findSingleMessage(channel: TextChannel, header: String): Message? {
		val history = channel.history

		while (true) {
			val section = history.retrievePast(100).complete()
			if (section.isEmpty()) return null

			section.forEach { message ->
				val messageHeader = messageHeader(message)
				if (header == messageHeader) return message
			}
		}
	}
}

package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.filesystem.file.IdsFile
import com.codeland.uhc.discord.filesystem.file.LinkDataFile
import com.codeland.uhc.discord.filesystem.file.LoadoutsFile
import com.codeland.uhc.discord.filesystem.file.NicknamesFile
import com.codeland.uhc.lobbyPvp.Loadouts
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import java.io.InputStream
import java.util.concurrent.CompletableFuture

object DiscordFilesystem {
	const val CATEGORY_NAME = "bot"
	const val DATA_CHANNEL_NAME = "data"

	const val IDS_HEADER = "ids"
	const val LINK_DATA_HEADER = "linkdata"
	const val NICKNAMES_HEADER = "nicknames"
	const val LOADOUTS_HEADER = "loadouts"

	val idsFile = IdsFile(IDS_HEADER, DATA_CHANNEL_NAME)
	val linkDataFile = LinkDataFile(LINK_DATA_HEADER, DATA_CHANNEL_NAME)
	val nicknamesFile = NicknamesFile(NICKNAMES_HEADER, DATA_CHANNEL_NAME)
	val loadoutsFile = LoadoutsFile(LOADOUTS_HEADER, DATA_CHANNEL_NAME)

	val files: Array<DiscordFile<*>> = arrayOf(
		idsFile, linkDataFile, nicknamesFile, loadoutsFile
	)

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

	fun messageHeader(message: Message): String? {
		val filename = message.attachments.firstOrNull()?.fileName ?: return null

		val dotIndex = filename.lastIndexOf('.')
		if (dotIndex == -1) return null

		return filename.substring(0, dotIndex)
	}

	fun filename(header: String): String {
		return "$header.json"
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

	fun updateMessage(header: String, stream: InputStream, onError: (String) -> Unit): Boolean {
		files.forEach { file ->
			if (file.header == header) {
				val data = file.fromStream(stream, onError) ?: return false

				when (header) {
					IDS_HEADER -> DataManager.ids = data as IdsFile.Companion.Ids
					LINK_DATA_HEADER -> DataManager.linkData = data as LinkDataFile.Companion.LinkData
					NICKNAMES_HEADER -> DataManager.nicknames = data as NicknamesFile.Companion.Nicknames
					LOADOUTS_HEADER -> DataManager.loadouts = data as Loadouts
				}

				return true
			}
		}

		return false
	}
}

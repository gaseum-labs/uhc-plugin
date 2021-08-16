package com.codeland.uhc.discord.filesystem

import com.codeland.uhc.discord.filesystem.DataManager.void
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import java.io.InputStream
import java.util.concurrent.CompletableFuture

abstract class DiscordFile <D> (val header: String, val channelName: String) {
	var cachedMessageId: Long? = null

	fun load(category: Category, onError: (String) -> Unit): CompletableFuture<D> {
		val future = CompletableFuture<D>()

		val message = getMessage(category)

		if (message == null) {
			/* when there is no message to load create default */
			DiscordFilesystem.getChannel(category, channelName)
				?.sendFile(write(defaultData()), DiscordFilesystem.filename(header))
				?.complete()

			onError("No message for $header was found, creating dummy")

			future.complete(defaultData())

		} else {
			DiscordFilesystem.messageStream(message).thenAccept { stream ->
				val data = fromStream(stream, onError)
				stream.close()

				if (data == null)
					future.completeExceptionally(Exception("Data was parsed incorrectly"))
				else
					future.complete(data)

			}.exceptionally {
				future.completeExceptionally(it).void()
			}
		}

		return future
	}

	fun save(guild: Guild, data: D) {
		val category = DiscordFilesystem.getBotCategory(guild)
		if (category != null) save(category, data)
	}

	fun save(category: Category, data: D) {
		val message = getMessage(category)

		if (message != null) {
			message.delete().queue()
			message.channel.sendFile(write(data), DiscordFilesystem.filename(header)).queue()

		} else {
			DiscordFilesystem.getChannel(category, channelName)
				?.sendFile(write(data), DiscordFilesystem.filename(header))
				?.queue()
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

	abstract fun fromStream(stream: InputStream, onError: (String) -> Unit): D?
	abstract fun write(data: D): ByteArray
	abstract fun defaultData(): D
}

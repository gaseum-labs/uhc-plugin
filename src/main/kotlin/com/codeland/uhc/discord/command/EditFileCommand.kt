package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MixerCommand
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.io.BufferedReader
import java.io.InputStreamReader

class EditFileCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		val message = event.message

		/* message has a file to replace the discord file contents with */
		if (message.attachments.isEmpty()) return false

		val reference = message.referencedMessage

		/* message is replying to one of the bot's own messages */
		return reference != null &&
			reference.author.idLong == bot.jda.selfUser.idLong
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val reference = event.message.referencedMessage ?: return
		val attachment = event.message.attachments.firstOrNull() ?: return

		val header = DiscordFilesystem.messageHeader(reference) ?:
			return errorMessage(event, "This is not a discord file message")

		attachment.retrieveInputStream().thenAccept { stream ->
			val reader = BufferedReader(InputStreamReader(stream))

			val charBuffer = CharArray(2000)
			reader.read(charBuffer)

			reference.editMessage(DiscordFilesystem.createMessageContent(header, String(charBuffer))).complete()

			/* update the internal data associated with the edited message */
			DiscordFilesystem.updateMessage(bot.dataManager, reference)

			event.message.delete().complete()

		}.exceptionally {
			errorMessage(event, "Something went wrong with the connection").void()
		}
	}
}

package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.util.Bad
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class EditFileCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return optionalDebugFilter(content, bot)
			&& replyingToDataFilter(event, true, DiscordFilesystem::isDataChannel)
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val reference = event.message.referencedMessage ?: return

		val header = DiscordFilesystem.messageHeader(reference) ?:
			return errorMessage(event, "This is not a discord file message")

		DiscordFilesystem.messageStream(event.message).thenAccept { stream ->
			/* set the internal data associated with the discord file */
			when (val r = DiscordFilesystem.updateMessage(header, stream)) {
				is Bad -> return@thenAccept errorMessage(event, r.error)
			}

			stream.close()

			/* delete old message, sent message will be treated as new one */
			reference.delete().queue()

		}.exceptionally { ex ->
			errorMessage(event, ex.message).void()
		}
	}
}

package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.stats.Summary
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class EditSummaryCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return optionalDebugFilter(content, bot)
			&& replyingToDataFilter(event, true, DiscordFilesystem::isSummaryStagingChannel)
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		DiscordFilesystem.messageStream(event.message).thenAccept { stream ->
			when(val r = Summary.readSummary(stream)) {
				/* replace the old summary */
				is Good -> event.message.referencedMessage?.delete()?.queue()
				/* old summary stands, new one is deleted and error is told */
				is Bad -> errorMessage(event, r.error)
			}

			stream.close()

		}.exceptionally { ex ->
			errorMessage(event, ex.message).void()
		}
	}
}

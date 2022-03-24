package org.gaseumlabs.uhc.discord.command.commands

import org.gaseumlabs.uhc.core.stats.Summary
import org.gaseumlabs.uhc.discord.Channels
import org.gaseumlabs.uhc.discord.MixerBot
import org.gaseumlabs.uhc.discord.command.MixerCommand
import org.gaseumlabs.uhc.util.Bad
import org.gaseumlabs.uhc.util.Good
import org.gaseumlabs.uhc.util.Util.void
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class EditSummaryCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: MessageReceivedEvent, bot: MixerBot): Boolean {
		return replyingToDataFilter(event, true) { it.name == Channels.SUMMARY_STAGING_CHANNEL_NAME }
	}

	override fun onCommand(content: String, event: MessageReceivedEvent, bot: MixerBot) {
		Channels.messageStream(event.message).thenAccept { stream ->
			when (val r = Summary.readSummary(stream)) {
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

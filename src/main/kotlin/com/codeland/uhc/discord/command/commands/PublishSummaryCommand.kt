package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.stats.Summary
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.Channels
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Util.void
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class PublishSummaryCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, "publish")
			&& replyingToDataFilter(event, false) { it.name == Channels.SUMMARY_STAGING_CHANNEL_NAME }
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val parts = content.trim().split(' ').filter { it.isNotEmpty() }
		if (parts.size != 3) return errorMessage(event, "Usage: ${prefix}publish [season] [game]")

		val season = parts[1].toIntOrNull() ?: return errorMessage(event, "Could not parse season")
		val game = parts[2].toIntOrNull() ?: return errorMessage(event, "Could not parse game")

		val referenced = event.message.referencedMessage ?: return

		Channels.messageStream(referenced).thenAccept { stream ->
			when (val r = Summary.readSummary(stream)) {
				is Good -> bot.SummaryManager.sendFinalSummary(season, game, r.value, event)
				is Bad -> errorMessage(event, r.error)
			}

			stream.close()

		}.exceptionally { ex ->
			errorMessage(event, ex.message).void()
		}
	}
}

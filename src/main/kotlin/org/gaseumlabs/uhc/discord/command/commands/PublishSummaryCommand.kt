package org.gaseumlabs.uhc.discord.command.commands

import org.gaseumlabs.uhc.core.UHC
import org.gaseumlabs.uhc.core.stats.Summary
import org.gaseumlabs.uhc.discord.Channels
import org.gaseumlabs.uhc.discord.MixerBot
import org.gaseumlabs.uhc.discord.command.MixerCommand
import org.gaseumlabs.uhc.discord.storage.*
import org.gaseumlabs.uhc.util.Bad
import org.gaseumlabs.uhc.util.Good
import org.gaseumlabs.uhc.util.Util.void
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

class PublishSummaryCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: MessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, "publish")
		&& replyingToDataFilter(event, false) { it.name == Channels.SUMMARY_STAGING_CHANNEL_NAME }
	}

	override fun onCommand(content: String, event: MessageReceivedEvent, bot: MixerBot) {
		val seasonEntry = StorageEntryInt("season")
		val gameEntry = StorageEntryInt("game")
		val modeEntry = StorageEntryString("mode")

		StorageEntry.massAssign(arrayListOf(seasonEntry, gameEntry, modeEntry),
			StorageEntry.getParams(afterKeyword(content, "publish")))

		val season = seasonEntry.value ?: return errorMessage(event, "Please specify a value for [season]")
		val game = gameEntry.value ?: return errorMessage(event, "Please specify a value for [game]")
		val mode = modeEntry.value?.lowercase()

		val doDiscord = mode == null || mode == "discord"
		val doDatabase = mode == null || mode == "db"

		if (!doDiscord && !doDatabase) return errorMessage(event,
			"Unknown mode. Try 'discord', 'db', or leave empty (both)")

		val referenced = event.message.referencedMessage ?: return

		Channels.messageStream(referenced).thenAccept { stream ->
			when (val r = Summary.readSummary(stream)) {
				is Good -> {
					if (doDiscord) {
						bot.SummaryManager.sendFinalSummary(season, game, r.value, event)
					}

					if (doDatabase) {

					}
				}
				is Bad -> errorMessage(event, r.error)
			}

			stream.close()

		}.exceptionally { ex ->
			errorMessage(event, ex.message).void()
		}
	}
}

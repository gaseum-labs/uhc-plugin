package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.stats.Summary
import com.codeland.uhc.discord.Channels
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.storage.*
import com.codeland.uhc.util.Bad
import com.codeland.uhc.util.Good
import com.codeland.uhc.util.Util.void
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
						val connection = UHC.dataManager.connection
						if (connection != null) {
							r.value.pushToDatabase(connection, season, game).exceptionally { ex ->
								errorMessage(event, ex.message)
							}
						} else {
							errorMessage(event, "Summary was not pushed to the database | No connection")
						}
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

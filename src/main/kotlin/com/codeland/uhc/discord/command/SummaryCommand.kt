package com.codeland.uhc.discord.command

import com.codeland.uhc.core.Ledger
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MixerCommand
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.io.BufferedReader
import java.io.InputStreamReader

class SummaryCommand : MixerCommand(false) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return content.startsWith("${prefix(bot.production)}summary")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val member = event.member ?: return
		val attachments = event.message.attachments

		if (attachments.size != 1 || attachments[0].isImage || attachments[0].fileExtension != "txt")
			return errorMessage(event, "Please attach a summary .txt file")

		val parts = Ledger.inverseFilename(attachments[0].fileName)
			?: return errorMessage(event, "Summary filename is incorrectly formatted")

		val (year, month, day, number) = parts

		attachments[0].retrieveInputStream().thenAccept{ stream ->
			val winningPlayers = ArrayList<MixerBot.SummaryEntry>()
			val losingPlayers = ArrayList<MixerBot.SummaryEntry>()
			var matchTime = 0

			val reader = BufferedReader(InputStreamReader(stream))

			val lines = reader.lineSequence()
			val readerErr = lines.any { line ->
				val parts = line.split(' ')

				if (parts.size != 4)
					true
				else {
					if (parts[0] == "1") {
						matchTime = parts[2].toIntOrNull() ?: return@any true
						winningPlayers
					} else {
						losingPlayers
					}.add(MixerBot.SummaryEntry(parts[0], parts[1], parts[3]))

					false
				}
			}

			reader.close()

			if (readerErr) {
				errorMessage(event, "Error reading summary file")
			} else {
				event.message.delete().submit().thenAccept {
					bot.sendGameSummary(event.message.channel, number, day, month, year, matchTime, winningPlayers, losingPlayers)
				}.exceptionally { err ->
					errorMessage(event, "Unknown error").void()
				}
			}

		}.exceptionally {
			errorMessage(event, "Something went wrong with the connection").void()
		}
	}
}

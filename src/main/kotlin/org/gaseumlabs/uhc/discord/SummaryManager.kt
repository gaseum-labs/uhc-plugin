package org.gaseumlabs.uhc.discord

import org.gaseumlabs.uhc.core.stats.Summary
import org.gaseumlabs.uhc.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.net.URI
import java.net.http.*
import java.util.*
import java.util.concurrent.*

class SummaryManager(val bot: MixerBot) {
	fun getSummariesChannel(): CompletableFuture<TextChannel> {
		return Channels.getCategoryChannel(bot.guild, Channels.SUMMARIES_CATEGORY_NAME, Channels.SUMMARIES_CHANNEL_NAME)
			.thenApply { (_, channel) -> channel }
	}

	fun getStagingChannel(): CompletableFuture<TextChannel> {
		return Channels.getCategoryChannel(bot.guild,
			Channels.DATA_CATEGORY_NAME,
			Channels.SUMMARY_STAGING_CHANNEL_NAME)
			.thenApply { (_, channel) -> channel }
	}

	fun stageSummary(summary: Summary) {
		getStagingChannel().thenAccept { staging ->
			staging.sendFile(summary.write(true).toByteArray(), "summary_${UUID.randomUUID()}.json").queue()
		}
	}

	fun sendFinalSummary(season: Int, game: Int, summary: Summary, event: MessageReceivedEvent) {
		val iconUrl = event.guild.iconUrl
		val icon = if (iconUrl != null) {
			val request = HttpRequest.newBuilder(URI(iconUrl)).GET().build()
			val client = HttpClient.newHttpClient()
			client.send(request, HttpResponse.BodyHandlers.ofByteArray()).body()

		} else {
			null
		}

		getSummariesChannel().thenAccept { summaries ->
			if (icon != null) {
				summaries.sendFile(icon, "logo.png").setEmbeds(summaryToEmbed(season, game, summary)).queue()
			} else {
				summaries.sendMessage(
					MessageBuilder().setEmbeds(summaryToEmbed(season, game, summary)).build()
				).queue()
			}
		}
	}

	fun summaryToEmbed(season: Int, game: Int, summary: Summary): MessageEmbed {
		val builder = EmbedBuilder()

		builder.setColor(summary.gameType.color)
		builder.setThumbnail("attachment://logo.png")
		builder.setTimestamp(summary.date)

		builder.setTitle("${summary.gameType} Season $season Game $game")
		builder.setDescription("Match time: ${Util.timeString(summary.gameLength / 20)}")

		val nameMap = summary.nameMap()

		fun formatName(uuid: UUID, name: String): String {
			val team = summary.playersTeam(uuid)
			return if (team == null) {
				name
			} else {
				"$name *[${team.name}]*"
			}
		}

		summary.players.forEach { (place, uuid, name, _, killedBy) ->
			val kills = summary.numKills(uuid)

			builder.addField(
				"$place. ${
					formatName(uuid,
						name)
				} ${if (kills == 0) "" else "($kills kill${if ((kills) == 1) "" else "s"})"}",
				if (place == 1) {
					"Winner"
				} else {
					"killed by ${if (killedBy == null) "environment" else "**${nameMap[killedBy] ?: "Unknown"}**"}"
				},
				false
			)
		}

		return builder.build()
	}
}
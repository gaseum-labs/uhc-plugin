package com.codeland.uhc.discord

import com.codeland.uhc.core.UHC
import com.codeland.uhc.core.stats.Summary
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.storage.Channels
import com.codeland.uhc.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import java.util.concurrent.CompletableFuture

class SummaryManager(val bot: MixerBot) {
	fun getSummariesChannel(): CompletableFuture<TextChannel> {
		return Channels.getCategoryChannel(bot.guild, Channels.SUMMARIES_CATEGORY_NAME, Channels.SUMMARIES_CHANNEL_NAME)
			.thenApply { (_, channel) -> channel }
	}

	fun getStagingChannel(): CompletableFuture<TextChannel> {
		return Channels.getCategoryChannel(bot.guild, Channels.DATA_CATEGORY_NAME, Channels.SUMMARY_STAGING_CHANNEL_NAME)
			.thenApply { (_, channel) -> channel }
	}

	fun stageSummary(summary: Summary) {
		getStagingChannel().thenAccept { staging ->
			staging.sendFile(summary.write(true).toByteArray(), "summary_${UUID.randomUUID()}.json").queue()
		}
	}

	fun sendFinalSummary(season: Int, game: Int, summary: Summary, event: GuildMessageReceivedEvent) {
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
				summaries.sendFile(icon, "logo.png").embed(summaryToEmbed(season, game, summary)).queue()
			} else {
				summaries.sendMessage(summaryToEmbed(season, game, summary)).queue()
			}

			event.message.delete().queue()
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

		summary.players.forEach { (place, uuid, name, _, killedBy) ->
			val kills = summary.numKills(uuid)

			builder.addField(
				"$place. $name${if (killedBy == null) "" else " ⚔️ ${nameMap[killedBy]}"}",
				"$kills kill${if (kills == 1) "" else "s"}",
				false
			)
		}

		return builder.build()
	}
}

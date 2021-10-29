package com.codeland.uhc.discord

import com.codeland.uhc.core.stats.Summary
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
import com.codeland.uhc.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.util.*

class SummaryManager(val bot: MixerBot) {
	fun getSummariesChannel(): TextChannel? {
		val guild = bot.guild() ?: return null

		val channel = guild.getGuildChannelById(DataManager.ids.summariesChannelId)

		return if (channel == null) {
			val createdChannel = guild.createTextChannel("summaries").complete()

			/* update the summary channel id with the created channel's id */
			DataManager.ids.summariesChannelId = createdChannel.idLong
			DiscordFilesystem.idsFile.save(guild, DataManager.ids)

			createdChannel

		} else {
			channel as? TextChannel
		}
	}

	fun getStagingChannel(): TextChannel? {
		val guild = bot.guild() ?: return null

		val channel = guild.getGuildChannelById(DataManager.ids.summaryStagingChannelId)

		return if (channel == null) {
			val category = DiscordFilesystem.getBotCategory(guild) ?: return null
			val createdChannel = category.createTextChannel("summary-staging").complete()

			/* update the staging channel id with the created channel's id */
			DataManager.ids.summaryStagingChannelId = createdChannel.idLong
			DiscordFilesystem.idsFile.save(guild, DataManager.ids)

			createdChannel

		} else {
			channel as? TextChannel
		}
	}

	fun stageSummary(summary: Summary) {
		getStagingChannel()?.sendFile(summary.write().toByteArray(), "summary_${UUID.randomUUID()}.json")?.queue()
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

		val summaryChannel = getSummariesChannel()
			?: return MixerCommand.errorMessage(event, "No summaries channel found")

		if (icon != null) {
			summaryChannel.sendFile(icon, "logo.png").embed(summaryToEmbed(season, game, summary)).queue()
		} else {
			summaryChannel.sendMessage(summaryToEmbed(season, game, summary)).queue()
		}

		event.message.delete().queue()
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

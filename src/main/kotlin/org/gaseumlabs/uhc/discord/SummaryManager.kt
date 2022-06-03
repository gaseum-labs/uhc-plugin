package org.gaseumlabs.uhc.discord

import org.gaseumlabs.uhc.database.summary.Summary
import org.gaseumlabs.uhc.util.Util
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.net.URI
import java.net.http.*
import java.util.*
import java.util.concurrent.*

//TODO REMOVE THIS CLASS
/* we need it for now tho for reference */
class SummaryManager(val bot: MixerBot) {
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

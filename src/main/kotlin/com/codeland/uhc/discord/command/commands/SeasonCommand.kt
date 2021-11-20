package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.Channels
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.storage.StorageEntry
import com.codeland.uhc.discord.storage.StorageEntryHex
import com.codeland.uhc.discord.storage.StorageEntryInt
import com.codeland.uhc.discord.storage.StorageEntryUuid
import com.codeland.uhc.util.Util.void
import com.codeland.uhc.util.extensions.ResultSetExtensions.setIntNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.util.concurrent.CompletableFuture

class SeasonCommand : MixerCommand(true) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, "season")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		CompletableFuture.supplyAsync {
			val connection = UHC.dataManager.connection ?: return@supplyAsync errorMessage(event, "Not connected to the database")

			val seasonNumberEntry = StorageEntryInt("number")
			val colorEntry = StorageEntryHex("color")
			val championColorEntry = StorageEntryHex("championColor")
			val championEntry = StorageEntryUuid("champion")

			val errors = StorageEntry.massAssign(
				arrayListOf(
					seasonNumberEntry,
					colorEntry,
					championColorEntry,
					championEntry
				),
				StorageEntry.getParams(afterKeyword(content, "season"))
			)

			if (errors.isNotEmpty()) return@supplyAsync errors.forEach { error ->
				event.channel.sendMessage(error).queue()
			}

			val seasonNumber = seasonNumberEntry.value ?: return@supplyAsync errorMessage(event, "Need a season number")
			val statement = connection.prepareCall("EXECUTE updateSeason ?, ?, ?, ?, ?;")

			Channels.messageStreamOptional(event.message).thenAccept { stream ->
				statement.setInt(1, seasonNumber)
				statement.setBinaryStream(2, stream)
				statement.setIntNull(3, colorEntry.value)
				statement.setIntNull(4, championColorEntry.value)
				statement.setNString(5, championEntry.value?.toString())

				val seasonResults = statement.executeQuery()
				stream?.close()

				if (!seasonResults.next()) return@thenAccept errorMessage(event, "Nothing returned from the database")

				val season = seasonResults.getInt(1)
				val logo = seasonResults.getObject(2) as ByteArray
				val color = seasonResults.getInt(3)
				val championColor = seasonResults.getInt(4)
				val championName = seasonResults.getNString(5)

				event.channel.sendFile(logo, "logo.png").embed(
					EmbedBuilder()
						.setTitle("Season $season")
						.setImage("attachment://logo.png")
						.setColor(color)
						.addField("Color", '#' + color.toString(16), true)
						.addField("Champion Color", '#' + championColor.toString(16), true)
						.addField("Champion", championName ?: "Not Set", true)
						.build()
				).queue()

				seasonResults.close()
				statement.close()

			}.exceptionally { ex ->
				errorMessage(event, ex.message).void()
			}
		}
	}
}

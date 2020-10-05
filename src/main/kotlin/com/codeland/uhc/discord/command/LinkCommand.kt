package com.codeland.uhc.discord.command

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MojangAPI
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class LinkCommand : MixerCommand(false) {
	override fun isCommand(content: String): Boolean {
		return content.startsWith("%link ")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val username = content.substring(1 + content.lastIndexOf(' '))
		val uuidResponse = MojangAPI.getUUIDFromUsername(username)

		if (uuidResponse == null) {
			event.channel.sendMessage("Something went wrong with the connection").queue()

		} else if (!uuidResponse.isValid()) {
			event.channel.sendMessage("That user does not exist!").queue()

		} else {
			val discordID = event.author.id
			val userIndex = bot.getDiscordUserIndex(discordID)

			if (userIndex == -1) {
				bot.discordIDs.add(discordID)
				bot.minecraftIDs.add(uuidResponse.uuid)
			} else {
				bot.minecraftIDs[userIndex] = uuidResponse.uuid
			}

			event.channel.sendMessage("Successfully linked as ${uuidResponse.name}").queue()

			bot.saveLinkData()
		}
	}
}

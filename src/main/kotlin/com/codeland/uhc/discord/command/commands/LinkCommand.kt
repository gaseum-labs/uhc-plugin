package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MojangAPI
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.DataManager
import com.codeland.uhc.discord.database.file.LinkDataFile
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class LinkCommand : MixerCommand(false) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, bot, "link")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val username = content.substring(1 + content.lastIndexOf(' '))
		val uuidResponse = MojangAPI.getUUIDFromUsername(username)

		if (uuidResponse == null) {
			event.channel.sendMessage("Something went wrong with the connection").queue()

		} else {
			val discordId = event.author.idLong
			val uuid = uuidResponse.convertUuid() ?: return event.channel.sendMessage("That Minecraft username does not exist!").queue()

			val linkData = bot.dataManager.linkData

			linkData.minecraftToDiscord[uuid] = discordId
			linkData.discordToMinecraft[discordId] = uuid

			val connection = bot.connection
			if (connection != null) {
				DataManager.linkDataFile.push(connection, LinkDataFile.LinkEntry(uuid, uuidResponse.name, discordId))
			}

			event.channel.sendMessage("Successfully linked as ${uuidResponse.name}").queue()
		}
	}
}

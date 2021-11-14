package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.core.UHC
import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MojangAPI
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.database.DataManager
import com.codeland.uhc.database.file.LinkDataFile
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent

class LinkCommand : MixerCommand(false) {
	override fun isCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot): Boolean {
		return keywordFilter(content, "link")
	}

	override fun onCommand(content: String, event: GuildMessageReceivedEvent, bot: MixerBot) {
		val inputUsername = content.substring(1 + content.lastIndexOf(' '))
		val uuidResponse = MojangAPI.getUUIDFromUsername(inputUsername)

		if (uuidResponse == null) {
			event.channel.sendMessage("Something went wrong with the connection").queue()

		} else {
			val discordId = event.author.idLong
			val uuid = uuidResponse.convertUuid() ?: return event.channel.sendMessage("That Minecraft username does not exist!").queue()
			val name = uuidResponse.name

			val linkData = UHC.dataManager.linkData

			if (linkData.addLink(uuid, discordId)) {
				val connection = UHC.dataManager.connection
				if (connection != null) {
					DataManager.linkDataFile.push(connection, LinkDataFile.LinkEntry(uuid, name, discordId))
				}

				event.channel.sendMessage("Successfully linked as $name").queue()

			} else {
				event.channel.sendMessage("Someone else has already linked as $name").queue()
			}
		}
	}
}

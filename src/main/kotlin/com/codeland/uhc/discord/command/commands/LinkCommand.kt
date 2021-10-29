package com.codeland.uhc.discord.command.commands

import com.codeland.uhc.discord.MixerBot
import com.codeland.uhc.discord.MojangAPI
import com.codeland.uhc.discord.command.MixerCommand
import com.codeland.uhc.discord.filesystem.DataManager
import com.codeland.uhc.discord.filesystem.DiscordFilesystem
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
			val uuid = uuidResponse.convertUuid() ?: return event.channel.sendMessage("That Minecraft username does not exist!").queue()

			/* save link data for this user */
			val discordId = event.author.idLong
			val userIndex = bot.getDiscordUserIndex(discordId)

			try {
				val linkData = DataManager.linkData

				if (userIndex == -1) {
					linkData.discordIds.add(discordId)
					linkData.minecraftIds.add(uuid)
				} else {
					linkData.minecraftIds[userIndex] = uuid
					println(uuid)
				}

				DiscordFilesystem.linkDataFile.save(event.guild, linkData)

				event.channel.sendMessage("Successfully linked as ${uuidResponse.name}").queue()
			} catch (ex: Exception) {
				ex.printStackTrace()
			}
		}
	}
}
